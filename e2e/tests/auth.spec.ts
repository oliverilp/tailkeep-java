import { noTokens, storedTokens } from '../support/auth';
import { expect, gotoLogin, test } from '../support/test';

test.describe('without a session', () => {
  test.use({ session: 'none' });

  for (const path of [
    '/',
    '/dashboard',
    '/dashboard/download',
    '/dashboard/video',
    '/dashboard/subscriptions',
    '/dashboard/logs',
    '/dashboard/settings'
  ]) {
    test(`${path} redirects to login`, async ({ page }) => {
      await page.goto(path);

      await expect(page).toHaveURL('/login');
      await expect(page.getByRole('button', { name: 'Sign in' })).toBeVisible();
    });
  }

  test('signing in stores tokens and opens downloads', async ({
    page,
    api
  }) => {
    const { username, password } = api.credentials;
    await gotoLogin(page);

    await page.getByLabel('Username').fill(username);
    await page.getByLabel('Password').fill(password);
    await page.getByRole('button', { name: 'Sign in' }).click();

    await expect(page).toHaveURL(/\/dashboard\/download/);
    await expect(page.getByText('Add Video')).toBeVisible();

    const [login] = api.requestsTo('POST', '/auth/authenticate');
    expect(login.body).toEqual({ username, password });

    const { accessToken, refreshToken } = await storedTokens(page);
    expect(accessToken).toBeTruthy();
    expect(refreshToken).toBeTruthy();

    const [dashboard] = api.requestsTo('GET', '/downloads/dashboard');
    expect(dashboard.authorization).toBe(`Bearer ${accessToken}`);
  });

  test('invalid input is rejected before calling the API', async ({
    page,
    api
  }) => {
    await gotoLogin(page);

    await page.getByLabel('Username').fill('a');
    await page.getByLabel('Password').fill('short');
    await page.getByRole('button', { name: 'Sign in' }).click();

    await expect(
      page.getByText('Username must be at least 2 characters.')
    ).toBeVisible();
    await expect(
      page.getByText('Password must be at least 10 characters.')
    ).toBeVisible();
    expect(api.requestsTo('POST', '/auth/authenticate')).toHaveLength(0);
  });

  test('wrong credentials show the API error and stay on login', async ({
    page,
    api
  }) => {
    await gotoLogin(page);

    await page.getByLabel('Username').fill('admin');
    await page.getByLabel('Password').fill('wrong-password');
    await page.getByRole('button', { name: 'Sign in' }).click();

    await expect(page.getByText('Invalid credentials provided')).toBeVisible();
    await expect(page.getByText('Failed to login')).toBeVisible();
    await expect(page).toHaveURL('/login');
    expect(await storedTokens(page)).toEqual(noTokens);
    expect(api.requestsTo('POST', '/auth/refresh-token')).toHaveLength(0);
  });
});

test.describe('with a valid session', () => {
  test('/ opens downloads', async ({ page }) => {
    await page.goto('/');

    await expect(page).toHaveURL(/\/dashboard\/download/);
    await expect(page.getByText('Add Video')).toBeVisible();
  });

  test('logging out clears the session', async ({ page, api }) => {
    await page.goto('/dashboard/download');
    const { accessToken } = await storedTokens(page);

    await page.getByRole('button', { name: 'Toggle user menu' }).click();
    await page.getByRole('menuitem', { name: 'Logout' }).click();

    await expect(page).toHaveURL('/login');
    expect(await storedTokens(page)).toEqual(noTokens);

    const [logout] = api.requestsTo('POST', '/auth/logout');
    expect(logout.authorization).toBe(`Bearer ${accessToken}`);

    await page.goto('/dashboard/download');
    await expect(page).toHaveURL('/login');
  });

  test('logging out still clears the session when the API fails', async ({
    page,
    api
  }) => {
    api.fail('POST', '/auth/logout', 500);
    await page.goto('/dashboard/download');

    await page.getByRole('button', { name: 'Toggle user menu' }).click();
    await page.getByRole('menuitem', { name: 'Logout' }).click();

    await expect(page).toHaveURL('/login');
    expect(await storedTokens(page)).toEqual(noTokens);
  });

  test('a revoked session ends at login', async ({ page, api }) => {
    await page.goto('/dashboard/download');
    await expect(page.getByText('Add Video')).toBeVisible();

    api.revokeSession();

    await expect(page).toHaveURL('/login');
    expect(await storedTokens(page)).toEqual(noTokens);
  });

  // The app can't refresh a session against the real API yet: it posts no body,
  // the request interceptor overwrites its Authorization header, and it ignores
  // the rotated refresh token. See "Known gaps" in the README.
  test.fixme('a revoked session is refreshed and the request retried', async ({
    page,
    api
  }) => {
    await page.goto('/dashboard/download');
    await expect(page.getByText('Add Video')).toBeVisible();

    api.revokeSession();

    await expect
      .poll(() => api.requestsTo('POST', '/auth/refresh-token').length)
      .toBeGreaterThan(0);
    await expect(page).toHaveURL(/\/dashboard\/download/);
    await expect(page.getByText('Add Video')).toBeVisible();
  });
});

for (const session of ['expired', 'malformed'] as const) {
  test.describe(`with a stored token that is ${session}`, () => {
    test.use({ session });

    test('the session is cleared and the token is never sent', async ({
      page,
      api
    }) => {
      await page.goto('/dashboard/download');

      await expect(page).toHaveURL('/login');
      expect(await storedTokens(page)).toEqual(noTokens);
      expect(api.requests.filter((r) => r.authorization)).toHaveLength(0);
      expect(api.requestsTo('POST', '/auth/refresh-token')).toHaveLength(0);
    });
  });
}
