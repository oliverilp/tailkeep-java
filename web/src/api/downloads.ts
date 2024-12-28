import { downloadsDashboardSchema } from '@/schemas/downloads-dashboard';
import type { DownloadsDashboard } from '@/schemas/downloads-dashboard';
import { apiClient } from '@/lib/api-client';

export async function getDownloadsDashboard(
  page: number = 0,
  progress: 'all' | 'active' | 'done' = 'all'
): Promise<DownloadsDashboard> {
  const { data } = await apiClient.get('/downloads/dashboard', {
    params: { page, progress }
  });
  return downloadsDashboardSchema.parse(data);
}

export async function startDownload(url: string): Promise<void> {
  await apiClient.post('/downloads', { url });
}

export async function deleteDownload(id: string): Promise<void> {
  await apiClient.delete(`/downloads/${id}`);
}
