import { expect, gotoLogin, test } from '../support/test';

const demoNotice = 'App is running in demo environment!';

test.describe('in demo mode', () => {
  test.use({ demoMode: true });

  test.describe('login', () => {
    test.use({ session: 'none' });

    test('shows the demo credentials', async ({ page }) => {
      await gotoLogin(page);

      await expect(page.getByText(demoNotice)).toBeVisible();
      await expect(page.getByText('Username is demo')).toBeVisible();
      await expect(page.getByText('Password is Demo1Demo1')).toBeVisible();
    });
  });

  test('settings can not be edited', async ({ page }) => {
    await page.goto('/dashboard/settings');

    await expect(page.getByText(demoNotice)).toBeVisible();
    await expect(
      page.getByText('Editing account password is disabled.')
    ).toBeVisible();
    await expect(page.getByLabel('Old password')).toBeDisabled();
    await expect(page.getByLabel('New password', { exact: true })).toBeDisabled();
    await expect(page.getByLabel('Confirm new password')).toBeDisabled();
    await expect(page.getByRole('button', { name: 'Save' })).toBeDisabled();
  });
});

test.describe('outside demo mode', () => {
  test.describe('login', () => {
    test.use({ session: 'none' });

    test('has no demo notice', async ({ page }) => {
      await gotoLogin(page);

      await expect(page.getByRole('button', { name: 'Sign in' })).toBeVisible();
      await expect(page.getByText(demoNotice)).toBeHidden();
    });
  });

  test('settings can be edited', async ({ page }) => {
    await page.goto('/dashboard/settings');

    await expect(page.getByLabel('Old password')).toBeEnabled();
    await expect(page.getByRole('button', { name: 'Save' })).toBeEnabled();
    await expect(page.getByText(demoNotice)).toBeHidden();
  });
});
