// The ONLY framework-aware file in this suite. Specs and the mock API must not
// change.
import { join } from 'node:path';
import type { Page } from '@playwright/test';

const placeholderImage = join(__dirname, 'assets/thumbnail.png');

export const baseURL = process.env.BASE_URL ?? 'http://localhost:3100';

// The SPA has one build, so demo tests use applyDemoMode() on the same server.
export const demoBaseURL = process.env.DEMO_BASE_URL ?? baseURL;

function spaServer(url: string) {
  const { port } = new URL(url);

  return {
    command: `pnpm --dir ../web exec vite preview --port ${port} --strictPort`,
    url: `${url}/login`,
    reuseExistingServer: !process.env.CI
  };
}

// Setting BASE_URL means something is already serving the app.
export const webServers = process.env.BASE_URL
  ? undefined
  : [spaServer(baseURL)];

// The browser loads thumbnails straight from YouTube.
export async function routeImages(page: Page): Promise<void> {
  await page.route('https://i.ytimg.com/**', (route) =>
    route.fulfill({ path: placeholderImage })
  );
}

// Registered after the mock API, so it takes precedence for this one endpoint.
// Elsewhere the mock answers it with a 401, which the app treats as non-demo.
export async function applyDemoMode(page: Page): Promise<void> {
  await page.route('**/api/v1/config', (route) =>
    route.fulfill({ json: { demoMode: true } })
  );
}
