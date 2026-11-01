// The ONLY framework-aware file in this suite. Swap the contents of this file
// during the TanStack migration; specs and the mock API must not change.
import { join } from 'node:path';
import type { Page } from '@playwright/test';

const placeholderImage = join(__dirname, 'assets/thumbnail.png');

export const baseURL = process.env.BASE_URL ?? 'http://localhost:3100';

// Next reads DEMO_MODE from the server's environment, so demo tests need their
// own server. A client-only app can drop this and use applyDemoMode() instead.
export const demoBaseURL =
  process.env.DEMO_BASE_URL ??
  (process.env.BASE_URL ? baseURL : 'http://localhost:3101');

function nextServer(url: string, demoMode: boolean) {
  const { port } = new URL(url);

  return {
    command: `pnpm --dir ../web exec next start -p ${port}`,
    url: `${url}/login`,
    env: { DEMO_MODE: String(demoMode) },
    reuseExistingServer: !process.env.CI
  };
}

// Setting BASE_URL means something is already serving the app.
export const webServers = process.env.BASE_URL
  ? undefined
  : [nextServer(baseURL, false), nextServer(demoBaseURL, true)];

// next/image proxies thumbnails through the Next server, which would otherwise
// fetch them from YouTube.
export async function routeImages(page: Page): Promise<void> {
  await page.route('**/_next/image?**', (route) =>
    route.fulfill({ path: placeholderImage })
  );
}

// No-op on Next: the demo server already has DEMO_MODE set. In a client-only
// app, mock its runtime config here.
export async function applyDemoMode(_page: Page): Promise<void> {}
