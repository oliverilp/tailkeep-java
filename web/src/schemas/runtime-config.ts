import { z } from 'zod';

export const runtimeConfigSchema = z.object({
  mediaUrl: z.string(),
  apiUrl: z.string()
});

export type RuntimeConfig = z.infer<typeof runtimeConfigSchema>;
