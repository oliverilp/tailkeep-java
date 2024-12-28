import { z } from 'zod';
import { queueInfoSchema } from './queue-info';
import { downloadProgressDtoSchema } from './progress';
import { createPageResponseSchema } from './page-response';

export const downloadsPageResponseSchema = createPageResponseSchema(
  downloadProgressDtoSchema
);

export type DownloadsPageResponse = z.infer<typeof downloadsPageResponseSchema>;

export const downloadsDashboardSchema = z.object({
  queueInfo: queueInfoSchema,
  downloads: downloadsPageResponseSchema
});

export type DownloadsDashboard = z.infer<typeof downloadsDashboardSchema>;
