import { expect, test } from '../support/test';

test('the side navigation reaches every section', async ({ page }) => {
  await page.goto('/dashboard/download');

  await page.getByRole('link', { name: 'Videos', exact: true }).click();
  await expect(page).toHaveURL('/dashboard/video');
  await expect(page.getByText('No videos')).toBeVisible();

  await page.getByRole('link', { name: 'Subscriptions', exact: true }).click();
  await expect(page).toHaveURL('/dashboard/subscriptions');
  await expect(page.getByText('Subscriptions page')).toBeVisible();

  await page.getByRole('link', { name: 'Logs', exact: true }).click();
  await expect(page).toHaveURL('/dashboard/logs');
  await expect(page.getByText('Logs page')).toBeVisible();

  await page.getByRole('link', { name: 'Settings', exact: true }).click();
  await expect(page).toHaveURL('/dashboard/settings');
  await expect(page.getByRole('heading', { name: 'Settings' })).toBeVisible();

  await page.getByRole('link', { name: 'Downloads', exact: true }).click();
  await expect(page).toHaveURL('/dashboard/download');
  await expect(page.getByText('Add Video')).toBeVisible();
});

test('the user menu opens settings', async ({ page }) => {
  await page.goto('/dashboard/download');

  await page.getByRole('button', { name: 'Toggle user menu' }).click();
  await page.getByRole('menuitem', { name: 'Settings' }).click();

  await expect(page).toHaveURL('/dashboard/settings');
});

test.describe('on a phone', () => {
  test.use({ viewport: { width: 375, height: 812 } });

  test('the menu reaches other sections', async ({ page }) => {
    await page.goto('/dashboard/download');

    await page.getByRole('button', { name: 'Toggle Menu' }).click();
    await page.getByRole('link', { name: 'Videos', exact: true }).click();

    await expect(page).toHaveURL('/dashboard/video');
    await expect(page.getByText('No videos')).toBeVisible();
    await expect(page.getByRole('dialog')).toBeHidden();
  });
});
