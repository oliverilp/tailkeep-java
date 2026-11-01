import { RuntimeConfig, runtimeConfigSchema } from '@/schemas/runtime-config';

// Uses fetch rather than apiClient: this endpoint is public, so the request must
// not carry a token or trigger the 401 interceptor.
export async function loadRuntimeConfig(): Promise<RuntimeConfig> {
  try {
    const response = await fetch('/api/v1/config');
    if (response.ok) {
      return runtimeConfigSchema.parse(await response.json());
    }
    console.error('Failed to load config:', response.status);
  } catch (error) {
    console.error('Failed to load config:', error);
  }

  return { demoMode: false };
}
