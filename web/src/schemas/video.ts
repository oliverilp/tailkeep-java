import { z } from 'zod';
import { channelDtoSchema } from './channel';

export const videoSchema = z.object({
  youtubeId: z.string(),
  url: z.string(),
  title: z.string(),
  channel: channelDtoSchema,
  durationString: z.string(),
  duration: z.number(),
  thumbnailUrl: z.string(),
  description: z.string(),
  viewCount: z.number(),
  commentCount: z.number(),
  filename: z.string()
});

export type Video = z.infer<typeof videoSchema>;

export const videoDtoSchema = videoSchema.extend({
  id: z.string(),
  createdAt: z.coerce.date(),
  updatedAt: z.coerce.date()
});

export type VideoDto = z.infer<typeof videoDtoSchema>;
