import { getApiClient } from '@/lib/api-client';
import { downloadsDashboardSchema } from '@/schemas/downloads-dashboard';
import type { DownloadsDashboard } from '@/schemas/downloads-dashboard';

export async function getDownloadsDashboard(): Promise<DownloadsDashboard> {
  const apiClient = await getApiClient();
  const { data } = await apiClient.get('/downloads/dashboard');
  return downloadsDashboardSchema.parse(data);
}

export async function startDownload(url: string): Promise<void> {
  const apiClient = await getApiClient();
  await apiClient.post('/downloads', { url });
}
