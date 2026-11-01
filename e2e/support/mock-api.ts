import { join } from 'node:path';
import type { Page, Request, Route } from '@playwright/test';
import { fakeJwt } from './auth';
import type {
  AuthenticationResponseDto,
  ChangePasswordRequestDto,
  DownloadProgressDto,
  DownloadsDashboardDto,
  Role,
  VideoByIdDto,
  VideoDto
} from './contract';
import { aVideo, anActiveDownload, anApiError } from './factories';

const apiRoot = '/api/v1';
const pageSize = 10;
const minPasswordLength = 10;
const maxPasswordLength = 100;
const placeholderThumbnail = join(__dirname, 'assets/thumbnail.png');

type Method = 'GET' | 'POST' | 'PATCH' | 'DELETE';

export interface RecordedRequest {
  method: string;
  path: string;
  params: URLSearchParams;
  body: unknown;
  authorization: string | undefined;
}

interface Failure {
  status: number;
  message: string;
}

function isBearer(authorization: string | undefined): boolean {
  return authorization?.startsWith('Bearer ') ?? false;
}

function newestFirst(
  a: { createdAt: string },
  b: { createdAt: string }
): number {
  return b.createdAt.localeCompare(a.createdAt);
}

// An in-memory stand-in for the Java API, one instance per test. Behavior and
// messages mirror the controllers in backend/mod-adapter/web and the use cases in backend/mod-domain.
export class MockApi {
  readonly requests: RecordedRequest[] = [];
  readonly mediaRequests: URL[] = [];
  readonly unhandled: string[] = [];

  /** The role of the signed-in user. Writes to downloads need ADMIN. */
  role: Role = 'ADMIN';
  /** Reported as waiting for processing in the queue info. */
  queued = 0;
  /** The title given to a download created through the UI. */
  nextDownloadTitle = 'Newly queued video';

  private username = 'admin';
  private password = 'Admin1Admin1';
  private sessionRevoked = false;
  private videos: VideoDto[] = [];
  private downloads: DownloadProgressDto[] = [];
  private failures = new Map<string, Failure>();

  get credentials() {
    return { username: this.username, password: this.password };
  }

  async install(page: Page): Promise<void> {
    await page.route(`**${apiRoot}/**`, (route) => this.handle(route));
    await page.route(
      (url) => url.pathname.startsWith('/media/'),
      (route) => this.handleMedia(route)
    );
    await page.route('https://i.ytimg.com/**', (route) =>
      route.fulfill({ path: placeholderThumbnail })
    );
  }

  seedVideos(...videos: VideoDto[]): void {
    this.videos.push(...videos);
  }

  /** Seeds downloads along with their videos, as a real download creates both. */
  seedDownloads(...downloads: DownloadProgressDto[]): void {
    this.downloads.push(...downloads);
    this.videos.push(...downloads.map((download) => download.video));
  }

  updateDownload(id: string, changes: Partial<DownloadProgressDto>): void {
    this.downloads = this.downloads.map((download) =>
      download.id === id ? { ...download, ...changes } : download
    );
  }

  /** Makes an endpoint fail until the returned function is called. */
  fail(
    method: Method,
    path: string,
    status: number,
    message = 'Something went wrong while executing the operation.'
  ): () => void {
    const key = `${method} ${path}`;
    this.failures.set(key, { status, message });

    return () => this.failures.delete(key);
  }

  /** Rejects the current tokens, as if they were revoked by another login. */
  revokeSession(): void {
    this.sessionRevoked = true;
  }

  requestsTo(method: Method, path: string): RecordedRequest[] {
    return this.requests.filter(
      (request) => request.method === method && request.path === path
    );
  }

  private record(request: Request, path: string, url: URL): RecordedRequest {
    const recorded = {
      method: request.method(),
      path,
      params: url.searchParams,
      body: request.postDataJSON() as unknown,
      authorization: request.headers()['authorization']
    };
    this.requests.push(recorded);

    return recorded;
  }

  private async handle(route: Route): Promise<void> {
    const url = new URL(route.request().url());
    const path = url.pathname.slice(apiRoot.length);
    const recorded = this.record(route.request(), path, url);

    const failure = this.failures.get(`${recorded.method} ${path}`);
    if (failure) {
      return this.error(route, url, failure.status, failure.message);
    }

    if (path.startsWith('/auth/')) {
      return this.handleAuth(route, url, recorded);
    }

    // JwtAuthenticationFilter and CustomAuthenticationEntryPoint
    if (!isBearer(recorded.authorization) || this.sessionRevoked) {
      return this.error(
        route,
        url,
        401,
        'Authentication is required to access this resource'
      );
    }

    return this.handleResource(route, url, recorded);
  }

  private async handleAuth(
    route: Route,
    url: URL,
    { method, path, body, authorization }: RecordedRequest
  ): Promise<void> {
    if (method === 'POST' && path === '/auth/authenticate') {
      const { username, password } = body as Record<string, string>;
      if (username !== this.username || password !== this.password) {
        return this.error(route, url, 401, 'Invalid credentials provided');
      }
      return this.issueTokens(route);
    }

    if (method === 'POST' && path === '/auth/refresh-token') {
      // AuthenticationController takes a required JSON body, and
      // GlobalExceptionHandler answers 400 when it is missing.
      if (!body) {
        return this.error(
          route,
          url,
          400,
          'Invalid request body: JSON is required'
        );
      }
      if (!(body as Record<string, string>).refreshToken) {
        return this.error(route, url, 401, 'Missing refresh token');
      }
      this.sessionRevoked = false;
      return this.issueTokens(route);
    }

    if (method === 'POST' && path === '/auth/logout') {
      return route.fulfill({ status: isBearer(authorization) ? 200 : 401 });
    }

    return this.unhandledRequest(route, method, path);
  }

  private async handleResource(
    route: Route,
    url: URL,
    { method, path, params, body }: RecordedRequest
  ): Promise<void> {
    const [, resource, id] = path.split('/');

    if (method === 'GET' && path === '/downloads/dashboard') {
      return route.fulfill({ json: this.dashboard(params) });
    }

    if (method === 'POST' && path === '/downloads') {
      if (this.role !== 'ADMIN') {
        return this.forbidden(route, url);
      }
      const { url: videoUrl } = body as { url: string };
      this.seedDownloads(
        anActiveDownload({
          video: aVideo({ url: videoUrl, title: this.nextDownloadTitle })
        })
      );
      return route.fulfill({ status: 202 });
    }

    if (method === 'DELETE' && resource === 'downloads' && id) {
      if (this.role !== 'ADMIN') {
        return this.forbidden(route, url);
      }
      if (!this.downloads.some((download) => download.id === id)) {
        return this.notFound(route, url, 'Download progress', id);
      }
      this.downloads = this.downloads.filter(
        (download) => download.id !== id
      );
      return route.fulfill({ status: 204 });
    }

    if (method === 'GET' && path === '/videos') {
      return route.fulfill({ json: [...this.videos].sort(newestFirst) });
    }

    if (method === 'GET' && resource === 'videos' && id) {
      const video = this.videoById(id);
      return video
        ? route.fulfill({ json: video })
        : this.notFound(route, url, 'Video', id);
    }

    if (method === 'PATCH' && path === '/users') {
      return this.changePassword(route, url, body as ChangePasswordRequestDto);
    }

    return this.unhandledRequest(route, method, path);
  }

  // DownloadService.getDownloadsDashboard
  private dashboard(params: URLSearchParams): DownloadsDashboardDto {
    const page = Number(params.get('page') ?? 1);
    const progress = params.get('progress') ?? 'all';
    const finished = this.downloads.filter(({ hasEnded }) => hasEnded);
    const active = this.downloads.filter(({ hasEnded }) => !hasEnded);

    const matching =
      progress === 'active'
        ? active
        : progress === 'done'
          ? finished
          : this.downloads;
    const sorted = [...matching].sort(newestFirst);
    const totalPages = Math.ceil(sorted.length / pageSize);

    return {
      queueInfo: {
        queue: this.queued,
        active: active.length,
        finished: finished.length
      },
      downloads: {
        items: sorted.slice((page - 1) * pageSize, page * pageSize),
        totalPages,
        totalItems: sorted.length,
        currentPage: page,
        pageSize,
        hasNext: page < totalPages,
        hasPrevious: page > 1
      }
    };
  }

  // VideoService.getVideoById
  private videoById(id: string): VideoByIdDto | undefined {
    const video = this.videos.find((candidate) => candidate.id === id);
    if (!video) {
      return undefined;
    }

    const downloads = this.downloads.filter(
      (download) => download.video.id === id
    );

    return {
      ...video,
      doneDownloading:
        downloads.length > 0 && downloads.every(({ hasEnded }) => hasEnded)
    };
  }

  // UserService.changePassword
  private async changePassword(
    route: Route,
    url: URL,
    request: ChangePasswordRequestDto
  ): Promise<void> {
    const { currentPassword, newPassword, confirmationPassword } = request;
    const hasValidLength = [newPassword, confirmationPassword].every(
      ({ length }) =>
        length >= minPasswordLength && length <= maxPasswordLength
    );

    if (!hasValidLength) {
      const range = `${minPasswordLength} and ${maxPasswordLength}`;
      return this.error(
        route,
        url,
        400,
        `Password must be between ${range} characters long`
      );
    }
    if (currentPassword !== this.password) {
      return this.error(route, url, 400, 'Current password is incorrect');
    }
    if (newPassword !== confirmationPassword) {
      return this.error(route, url, 400, 'New passwords do not match');
    }

    this.password = newPassword;
    return route.fulfill({ status: 200 });
  }

  private async handleMedia(route: Route): Promise<void> {
    this.mediaRequests.push(new URL(route.request().url()));
    // No real file: tests check the request, not playback.
    return route.fulfill({ status: 200, contentType: 'video/mp4', body: '' });
  }

  private issueTokens(route: Route): Promise<void> {
    const json: AuthenticationResponseDto = {
      access_token: fakeJwt(this.username),
      refresh_token: fakeJwt(this.username, 7 * 24 * 3600)
    };

    return route.fulfill({ json });
  }

  private error(
    route: Route,
    url: URL,
    status: number,
    message: string
  ): Promise<void> {
    return route.fulfill({
      status,
      json: anApiError(url.pathname, message, status)
    });
  }

  // CustomAccessDeniedHandler
  private forbidden(route: Route, url: URL): Promise<void> {
    return this.error(
      route,
      url,
      403,
      "You don't have permission to access this resource"
    );
  }

  // ResourceNotFoundException
  private notFound(
    route: Route,
    url: URL,
    resource: string,
    id: string
  ): Promise<void> {
    return this.error(
      route,
      url,
      404,
      `${resource} not found with identifier: ${id}`
    );
  }

  private unhandledRequest(
    route: Route,
    method: string,
    path: string
  ): Promise<void> {
    this.unhandled.push(`${method} ${path}`);

    return route.fulfill({ status: 501 });
  }
}
