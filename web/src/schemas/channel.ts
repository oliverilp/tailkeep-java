import { z } from 'zod';

export const channelDtoSchema = z.object({
  id: z.string(),
  name: z.string(),
  youtubeId: z.string(),
  channelUrl: z.string(),
  createdAt: z.coerce.date(),
  updatedAt: z.coerce.date()
});

export type ChannelDto = z.infer<typeof channelDtoSchema>;
