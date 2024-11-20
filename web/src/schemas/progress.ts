import { z } from 'zod';
import { videoDtoSchema } from '@/schemas/video';

export const downloadProgressSchema = z.object({
  status: z.string().nullable(),
  hasEnded: z.boolean(),
  progress: z.number(),
  size: z.string().nullable(),
  speed: z.string().nullable(),
  eta: z.string().nullable()
});

export type DownloadProgress = z.infer<typeof downloadProgressSchema>;

export const downloadProgressDtoSchema = downloadProgressSchema.extend({
  id: z.string(),
  createdAt: z.coerce.date(),
  updatedAt: z.coerce.date(),
  completedAt: z.coerce.date().nullable(),
  video: videoDtoSchema
});

export type DownloadProgressDto = z.infer<typeof downloadProgressDtoSchema>;
