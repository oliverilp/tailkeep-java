import { z } from 'zod';

export const runtimeConfigSchema = z.object({
  demoMode: z.boolean()
});

export type RuntimeConfig = z.infer<typeof runtimeConfigSchema>;
