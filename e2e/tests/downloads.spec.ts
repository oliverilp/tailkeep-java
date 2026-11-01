import type { Locator, Page } from '@playwright/test';
import {
  aFinishedDownload,
  aVideo,
  anActiveDownload
} from '../support/factories';
import type { MockApi } from '../support/mock-api';
import { expect, test } from '../support/test';

const downloadsPath = '/dashboard/download';

function row(page: Page, title: string): Locator {
  return page.getByRole('row', { name: title });
}

function urlInput(page: Page): Locator {
  return page.getByPlaceholder('https://www.youtube.com');
}

async function expectQueueCount(
  page: Page,
  card: 'queue' | 'active' | 'finished',
  count: number
): Promise<void> {
  await expect(
    page
      .getByTestId(`queue-card-${card}`)
      .getByText(String(count), { exact: true })
  ).toBeVisible();
}

async function openDeleteDialog(page: Page, title: string): Promise<Locator> {
  await row(page, title).getByRole('button', { name: 'Toggle menu' }).click();
  await page.getByRole('menuitem', { name: 'Delete' }).click();

  return page.getByRole('alertdialog');
}

function seedPages(api: MockApi, count: number): void {
  const downloads = Array.from({ length: count }, (_, index) => {
    const title = `Video ${String(index + 1).padStart(2, '0')}`;
    return aFinishedDownload({ video: aVideo({ title }) });
  });

  api.seedDownloads(...downloads);
}

test.describe('dashboard', () => {
  test('shows queue counts and download details', async ({ page, api }) => {
    api.queued = 2;
    api.seedDownloads(
      anActiveDownload({
        video: aVideo({ title: 'Active video' }),
        progress: 42.5,
        speed: '4.20MiB/s',
        eta: '00:20',
        size: '120.00MiB'
      }),
      aFinishedDownload({ video: aVideo({ title: 'Finished video' }) })
    );

    await page.goto(downloadsPath);

    await expectQueueCount(page, 'queue', 2);
    await expectQueueCount(page, 'active', 1);
    await expectQueueCount(page, 'finished', 1);

    const active = row(page, 'Active video');
    await expect(active).toContainText('downloading');
    await expect(active).toContainText('42.5%');
    await expect(active).toContainText('4.20MiB/s');
    await expect(active).toContainText('00:20');
    await expect(active).toContainText('120.00MiB');
    await expect(active).toContainText('Nov 1, 2024');

    const finished = row(page, 'Finished video');
    await expect(finished).toContainText('finished');
    await expect(finished).toContainText('100%');
  });

  test('shows an empty state', async ({ page }) => {
    await page.goto(downloadsPath);

    await expect(page.getByText('No downloads.')).toBeVisible();
  });

  test('updates progress without any interaction', async ({ page, api }) => {
    const download = anActiveDownload({
      video: aVideo({ title: 'Live video' }),
      progress: 30
    });
    api.seedDownloads(download);

    await page.goto(downloadsPath);
    await expect(row(page, 'Live video')).toContainText('30%');
    await expectQueueCount(page, 'active', 1);

    api.updateDownload(download.id, { progress: 75 });
    await expect(row(page, 'Live video')).toContainText('75%');

    api.updateDownload(download.id, {
      progress: 100,
      status: 'finished',
      hasEnded: true
    });
    await expect(row(page, 'Live video')).toContainText('100%');
    await expect(row(page, 'Live video')).toContainText('finished');
    await expectQueueCount(page, 'active', 0);
    await expectQueueCount(page, 'finished', 1);
  });

  test('appears once a failing API recovers', async ({ page, api }) => {
    const recover = api.fail('GET', '/downloads/dashboard', 500);

    await page.goto(downloadsPath);
    await expect
      .poll(() => api.requestsTo('GET', '/downloads/dashboard').length)
      .toBeGreaterThan(1);
    await expect(page.getByText('Add Video')).toBeHidden();

    recover();

    await expect(page.getByText('Add Video')).toBeVisible({ timeout: 15_000 });
  });
});

test.describe('adding a video', () => {
  const videoUrl = 'https://www.youtube.com/watch?v=dQw4w9WgXcQ';

  test('queues the URL and shows the new download', async ({ page, api }) => {
    await page.goto(downloadsPath);

    await urlInput(page).fill(videoUrl);
    await urlInput(page).press('Enter');

    await expect(
      page.getByText('Download added to queue successfully')
    ).toBeVisible();
    await expect(urlInput(page)).toHaveValue('');
    await expect(row(page, api.nextDownloadTitle)).toBeVisible();

    const [request] = api.requestsTo('POST', '/downloads');
    expect(request.body).toEqual({ url: videoUrl });
  });

  test('rejects an invalid URL before calling the API', async ({
    page,
    api
  }) => {
    await page.goto(downloadsPath);

    await urlInput(page).fill('not a url');
    await urlInput(page).press('Enter');

    await expect(
      page.getByText('Must be a valid YouTube video URL.')
    ).toBeVisible();
    expect(api.requestsTo('POST', '/downloads')).toHaveLength(0);
  });

  test('shows the API error when the URL is rejected', async ({
    page,
    api
  }) => {
    const message = 'URL returned error status: 404';
    api.fail('POST', '/downloads', 400, message);
    await page.goto(downloadsPath);

    await urlInput(page).fill(videoUrl);
    await urlInput(page).press('Enter');

    await expect(
      page.getByText('Failed to add download to queue')
    ).toBeVisible();
    await expect(page.getByText(message)).toBeVisible();
  });

  test('shows the API error when the user lacks permission', async ({
    page,
    api
  }) => {
    api.role = 'USER';
    await page.goto(downloadsPath);

    await urlInput(page).fill(videoUrl);
    await urlInput(page).press('Enter');

    await expect(
      page.getByText("You don't have permission to access this resource")
    ).toBeVisible();
    await expect(page.getByText('No downloads.')).toBeVisible();
  });
});

test.describe('tabs', () => {
  test('filter downloads by progress', async ({ page, api }) => {
    api.seedDownloads(
      anActiveDownload({ video: aVideo({ title: 'Active video' }) }),
      aFinishedDownload({ video: aVideo({ title: 'Finished video' }) })
    );

    await page.goto(downloadsPath);
    await expect(row(page, 'Active video')).toBeVisible();
    await expect(row(page, 'Finished video')).toBeVisible();

    await page.getByRole('tab', { name: 'Active' }).click();
    await expect(page).toHaveURL(`${downloadsPath}?progress=active&page=1`);
    await expect(row(page, 'Active video')).toBeVisible();
    await expect(row(page, 'Finished video')).toBeHidden();

    await page.getByRole('tab', { name: 'Done' }).click();
    await expect(page).toHaveURL(`${downloadsPath}?progress=done&page=1`);
    await expect(row(page, 'Finished video')).toBeVisible();
    await expect(row(page, 'Active video')).toBeHidden();
    await expect(
      page.getByRole('tab', { name: 'Done', selected: true })
    ).toBeVisible();

    await page.getByRole('tab', { name: 'All' }).click();
    await expect(page).toHaveURL(`${downloadsPath}?progress=all&page=1`);
    await expect(row(page, 'Active video')).toBeVisible();
    await expect(row(page, 'Finished video')).toBeVisible();
  });

  test('open from a deep link', async ({ page, api }) => {
    api.seedDownloads(
      anActiveDownload({ video: aVideo({ title: 'Active video' }) }),
      aFinishedDownload({ video: aVideo({ title: 'Finished video' }) })
    );

    await page.goto(`${downloadsPath}?progress=done&page=1`);

    await expect(row(page, 'Finished video')).toBeVisible();
    await expect(row(page, 'Active video')).toBeHidden();
    await expect(
      page.getByRole('tab', { name: 'Done', selected: true })
    ).toBeVisible();
  });
});

test.describe('pagination', () => {
  test('moves between pages', async ({ page, api }) => {
    seedPages(api, 25);
    await page.goto(downloadsPath);

    await expect(page.getByText('Showing')).toHaveText(
      'Showing 1-10 of 25 downloads'
    );
    await expect(row(page, 'Video 01')).toBeVisible();

    await page.getByRole('link', { name: 'Go to next page' }).click();
    await expect(page).toHaveURL(`${downloadsPath}?progress=all&page=2`);
    await expect(page.getByText('Showing')).toHaveText(
      'Showing 11-20 of 25 downloads'
    );
    await expect(row(page, 'Video 11')).toBeVisible();
    await expect(row(page, 'Video 01')).toBeHidden();

    await page.getByRole('link', { name: '3', exact: true }).click();
    await expect(page).toHaveURL(`${downloadsPath}?progress=all&page=3`);
    await expect(page.getByText('Showing')).toHaveText(
      'Showing 21-25 of 25 downloads'
    );
    await expect(row(page, 'Video 25')).toBeVisible();

    await page.getByRole('link', { name: 'Go to previous page' }).click();
    await expect(page).toHaveURL(`${downloadsPath}?progress=all&page=2`);
    await expect(row(page, 'Video 11')).toBeVisible();
  });

  test('opens from a deep link', async ({ page, api }) => {
    seedPages(api, 25);

    await page.goto(`${downloadsPath}?progress=all&page=2`);

    await expect(row(page, 'Video 11')).toBeVisible();
    await expect(
      page.getByRole('link', { name: '2', exact: true })
    ).toHaveAttribute('aria-current', 'page');
  });

  test('is hidden when everything fits on one page', async ({ page, api }) => {
    seedPages(api, 10);

    await page.goto(downloadsPath);

    await expect(row(page, 'Video 10')).toBeVisible();
    await expect(
      page.getByRole('link', { name: 'Go to next page' })
    ).toBeHidden();
  });
});

test.describe('deleting a download', () => {
  const title = 'Video to delete';

  test('removes the row once confirmed', async ({ page, api }) => {
    const download = aFinishedDownload({ video: aVideo({ title }) });
    api.seedDownloads(download);
    await page.goto(downloadsPath);

    const dialog = await openDeleteDialog(page, title);
    await expect(dialog).toContainText(title);
    await dialog.getByRole('button', { name: 'Delete' }).click();

    await expect(
      page.getByText('Download history deleted successfully')
    ).toBeVisible();
    await expect(dialog).toBeHidden();
    await expect(row(page, title)).toBeHidden();
    expect(api.requestsTo('DELETE', `/downloads/${download.id}`)).toHaveLength(
      1
    );
  });

  test('keeps the row when cancelled', async ({ page, api }) => {
    const download = aFinishedDownload({ video: aVideo({ title }) });
    api.seedDownloads(download);
    await page.goto(downloadsPath);

    const dialog = await openDeleteDialog(page, title);
    await dialog.getByRole('button', { name: 'Cancel' }).click();

    await expect(dialog).toBeHidden();
    await expect(row(page, title)).toBeVisible();
    expect(api.requestsTo('DELETE', `/downloads/${download.id}`)).toHaveLength(
      0
    );
  });

  test('shows the API error and keeps the dialog open', async ({
    page,
    api
  }) => {
    const message = 'Could not delete this download';
    const download = aFinishedDownload({ video: aVideo({ title }) });
    api.seedDownloads(download);
    api.fail('DELETE', `/downloads/${download.id}`, 500, message);
    await page.goto(downloadsPath);

    const dialog = await openDeleteDialog(page, title);
    await dialog.getByRole('button', { name: 'Delete' }).click();

    await expect(
      page.getByText('Failed to delete download history')
    ).toBeVisible();
    await expect(page.getByText(message)).toBeVisible();
    await expect(dialog).toBeVisible();
  });
});
