import { test as base, expect } from '@playwright/test';
import type { Page } from '@playwright/test';
import { storageStateFor } from './auth';
import type { Session } from './auth';
import { MockApi } from './mock-api';
import { applyDemoMode, demoBaseURL, routeImages } from './target';

interface Options {
  session: Session;
  demoMode: boolean;
}

interface Fixtures {
  api: MockApi;
}

export const test = base.extend<Options & Fixtures>({
  session: ['valid', { option: true }],
  demoMode: [false, { option: true }],

  baseURL: async ({ baseURL, demoMode }, use) => {
    await use(demoMode ? demoBaseURL : baseURL);
  },

  storageState: async ({ session, baseURL }, use) => {
    await use(storageStateFor(session, baseURL!));
  },

  api: async ({}, use) => {
    await use(new MockApi());
  },

  page: async ({ page, api, baseURL, demoMode }, use) => {
    const { origin } = new URL(baseURL!);
    const externalRequests: string[] = [];

    // Registered first so it runs last: only requests no mock handled get here.
    await page.route(
      (url) => url.protocol.startsWith('http') && url.origin !== origin,
      (route) => {
        externalRequests.push(route.request().url());
        return route.abort();
      }
    );
    await routeImages(page);
    await api.install(page);
    if (demoMode) {
      await applyDemoMode(page);
    }

    await use(page);

    // Leave the app before removing the mocks: it polls and lazy-loads images,
    // and anything sent after this point would reach the real servers.
    await page.goto('about:blank');
    await page.unrouteAll({ behavior: 'ignoreErrors' });
    expect(api.unhandled, 'unmocked API calls').toEqual([]);
    expect(externalRequests, 'requests to other hosts').toEqual([]);
  }
});

// Clicking before hydration would submit the form natively, and only /login can
// be reached that early. It doesn't poll, so the network does go idle.
export async function gotoLogin(page: Page): Promise<void> {
  await page.goto('/login', { waitUntil: 'networkidle' });
}

export { expect };
