import { getApiClient } from '@/lib/api-client';
import { downloadsDashboardSchema } from '@/schemas/downloads-dashboard';
import type { DownloadsDashboard } from '@/schemas/downloads-dashboard';
import { apiClient } from '@/lib/api-client';

export async function getDownloadsDashboard(): Promise<DownloadsDashboard> {
  const { data } = await apiClient.get('/downloads/dashboard');
  return downloadsDashboardSchema.parse(data);
}

export async function startDownload(url: string): Promise<void> {
  await apiClient.post('/downloads', { url });
}
