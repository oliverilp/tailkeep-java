import { randomUUID } from 'node:crypto';
import { storedTokens } from '../support/auth';
import {
  aFinishedDownload,
  aVideo,
  anActiveDownload
} from '../support/factories';
import { expect, test } from '../support/test';

test.describe('video list', () => {
  test('opens a video from its card', async ({ page, api }) => {
    const video = aVideo({ title: 'First video' });
    api.seedVideos(video, aVideo({ title: 'Second video' }));

    await page.goto('/dashboard/video');

    await expect(page.getByRole('link', { name: 'Second video' })).toBeVisible();
    await page.getByRole('link', { name: 'First video' }).click();

    await expect(page).toHaveURL(`/dashboard/video/${video.id}`);
    await expect(
      page.getByRole('heading', { name: 'First video' })
    ).toBeVisible();
  });

  test('opens a video from its download row', async ({ page, api }) => {
    const video = aVideo({ title: 'Downloaded video' });
    api.seedDownloads(aFinishedDownload({ video }));

    await page.goto('/dashboard/download');
    await page
      .getByRole('row', { name: 'Downloaded video' })
      .getByRole('link')
      .click();

    await expect(page).toHaveURL(`/dashboard/video/${video.id}`);
    await expect(
      page.getByRole('heading', { name: 'Downloaded video' })
    ).toBeVisible();
  });

  test('offers a way to download when there are no videos', async ({
    page
  }) => {
    await page.goto('/dashboard/video');

    await expect(page.getByText('No videos')).toBeVisible();
    await page.getByRole('button', { name: 'Download Video' }).click();

    await expect(page).toHaveURL(/\/dashboard\/download/);
  });
});

test.describe('video details', () => {
  test('plays a finished video', async ({ page, api }) => {
    const video = aVideo({
      title: 'Finished video',
      description: 'First line\nSecond line'
    });
    api.seedDownloads(aFinishedDownload({ video }));

    await page.goto(`/dashboard/video/${video.id}`);

    await expect(
      page.getByRole('heading', { name: 'Finished video' })
    ).toBeVisible();
    await expect(page.getByText(video.channel.name)).toBeVisible();
    await expect(page.getByText('First line')).toBeVisible();
    await expect(page.getByText('Second line')).toBeVisible();
    await expect(page.locator('video')).toBeAttached();
    await expect(page.getByAltText('Video thumbnail')).toBeAttached();

    await expect.poll(() => api.mediaRequests.length).toBeGreaterThan(0);
    const [media] = api.mediaRequests;
    const { accessToken } = await storedTokens(page);
    expect(media.pathname).toBe(`/media/${video.id}`);
    expect(media.searchParams.get('token')).toBe(accessToken);
  });

  test('shows a notice while the video is downloading', async ({
    page,
    api
  }) => {
    const video = aVideo({ title: 'Unfinished video' });
    api.seedDownloads(anActiveDownload({ video }));

    await page.goto(`/dashboard/video/${video.id}`);

    await expect(
      page.getByText('This video is still downloading.')
    ).toBeVisible();
    await expect(
      page.getByRole('heading', { name: 'Unfinished video' })
    ).toBeVisible();
    await expect(page.locator('video')).toHaveCount(0);
    expect(api.mediaRequests).toHaveLength(0);
  });

  test('shows not found for an unknown video', async ({ page }) => {
    await page.goto(`/dashboard/video/${randomUUID()}`);

    await expect(page.getByText('404 - cannot be found')).toBeVisible({
      timeout: 15_000
    });
  });
});
