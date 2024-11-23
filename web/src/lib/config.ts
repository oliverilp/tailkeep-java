import { RuntimeConfig, runtimeConfigSchema } from '@/schemas/runtime-config';
import axios from 'axios';

let cachedConfig: RuntimeConfig | null = null;

export async function getRuntimeConfig(): Promise<RuntimeConfig> {
  if (typeof window === 'undefined') {
    // Server-side: return directly from process.env
    return runtimeConfigSchema.parse({
      mediaUrl: process.env.MEDIA_URL ?? '',
      apiUrl: process.env.API_URL ?? ''
    });
  }

  // Client-side: fetch from API and cache
  if (!cachedConfig) {
    const { data } = await axios.get('/api/config');
    cachedConfig = runtimeConfigSchema.parse(data);
  }

  return cachedConfig;
}
