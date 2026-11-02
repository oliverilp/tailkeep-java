// The Java API contract, kept by hand. Paths are relative to
// backend/mod-adapter/web/src/main/java/org/tailkeep/adapter/web, unless they start with backend/.
// Java LocalDateTime values are serialized as ISO strings with no zone, so they are plain strings here.

// channel/ChannelResponse.java
export interface ChannelDto {
  id: string;
  name: string;
  youtubeId: string;
  channelUrl: string;
  createdAt: string;
  updatedAt: string;
}

// video/VideoResponse.java
export interface VideoDto {
  id: string;
  youtubeId: string;
  channel: ChannelDto;
  url: string;
  title: string;
  durationString: string;
  duration: number;
  thumbnailUrl: string;
  description: string;
  viewCount: number;
  commentCount: number;
  filename: string;
  createdAt: string;
  updatedAt: string;
}

// video/VideoDetailResponse.java
export interface VideoByIdDto extends VideoDto {
  doneDownloading: boolean;
}

// download/DownloadResponse.java
export interface DownloadProgressDto {
  id: string;
  video: VideoDto;
  status: string | null;
  hasEnded: boolean;
  progress: number;
  size: string | null;
  speed: string | null;
  eta: string | null;
  createdAt: string;
  updatedAt: string;
  completedAt: string | null;
}

// shared/PageResponse.java
export interface PageResponseDto<T> {
  items: T[];
  totalPages: number;
  totalItems: number;
  currentPage: number;
  pageSize: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

// download/QueueInfoResponse.java
export interface QueueInfoDto {
  queue: number;
  active: number;
  finished: number;
}

// download/DashboardResponse.java
export interface DownloadsDashboardDto {
  queueInfo: QueueInfoDto;
  downloads: PageResponseDto<DownloadProgressDto>;
}

// auth/AuthenticationResponse.java
export interface AuthenticationResponseDto {
  access_token: string;
  refresh_token: string;
}

// user/ChangePasswordRequest.java
export interface ChangePasswordRequestDto {
  currentPassword: string;
  newPassword: string;
  confirmationPassword: string;
}

// shared/ApiError.java
export interface ApiError {
  path: string;
  message: string;
  statusCode: number;
  timestamp: string;
}

// backend/mod-domain/src/main/java/org/tailkeep/domain/user/model/Role.java
export type Role = 'ADMIN' | 'USER';
