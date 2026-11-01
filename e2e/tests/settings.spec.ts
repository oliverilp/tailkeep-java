import type { Page } from '@playwright/test';
import { expect, test } from '../support/test';

const newPassword = 'NewPassword123';

async function submitPasswords(
  page: Page,
  oldPassword: string,
  password: string,
  confirmation: string
): Promise<void> {
  await page.getByLabel('Old password').fill(oldPassword);
  await page.getByLabel('New password', { exact: true }).fill(password);
  await page.getByLabel('Confirm new password').fill(confirmation);
  await page.getByRole('button', { name: 'Save' }).click();
}

test.beforeEach(async ({ page }) => {
  await page.goto('/dashboard/settings');
});

test('changes the password', async ({ page, api }) => {
  const { password } = api.credentials;

  await submitPasswords(page, password, newPassword, newPassword);

  await expect(page.getByText('Password changed successfully')).toBeVisible();
  await expect(page.getByLabel('Old password')).toHaveValue('');
  await expect(page.getByLabel('New password', { exact: true })).toHaveValue('');
  await expect(page.getByLabel('Confirm new password')).toHaveValue('');

  const [request] = api.requestsTo('PATCH', '/users');
  expect(request.body).toEqual({
    currentPassword: password,
    newPassword,
    confirmationPassword: newPassword
  });
});

test('rejects short passwords before calling the API', async ({
  page,
  api
}) => {
  await submitPasswords(page, 'short', 'short', 'short');

  await expect(
    page.getByText('Password must be at least 10 characters.')
  ).toHaveCount(3);
  expect(api.requestsTo('PATCH', '/users')).toHaveLength(0);
});

test('shows the API error for a wrong current password', async ({ page }) => {
  await submitPasswords(page, 'wrong-password', newPassword, newPassword);

  await expect(page.getByText('Current password is incorrect')).toBeVisible();
  await expect(page.getByText('Failed to change password')).toBeVisible();
});

test('shows the API error when the new passwords differ', async ({
  page,
  api
}) => {
  const { password } = api.credentials;

  await submitPasswords(page, password, newPassword, 'DifferentPassword123');

  await expect(page.getByText('New passwords do not match')).toBeVisible();
  await expect(page.getByText('Failed to change password')).toBeVisible();
});
