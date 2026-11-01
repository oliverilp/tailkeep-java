import { defineConfig, devices } from '@playwright/test';
import { baseURL, webServers } from './support/target';

const isCI = Boolean(process.env.CI);

export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  forbidOnly: isCI,
  retries: isCI ? 2 : 0,
  reporter: [[isCI ? 'github' : 'list'], ['html', { open: 'never' }]],
  use: {
    baseURL,
    trace: 'on-first-retry',
    // A service worker would bypass page.route.
    serviceWorkers: 'block'
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: webServers
});
