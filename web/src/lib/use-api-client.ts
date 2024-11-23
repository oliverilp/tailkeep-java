'use client';

import { useState, useEffect } from 'react';
import { getApiClient } from './api-client';
import type { AxiosInstance } from 'axios';

/**
 * Custom hook that manages the API client initialization
 * @returns AxiosInstance | null - Returns the API client instance or null while initializing
 */
export function useApiClient() {
  const [apiClient, setApiClient] = useState<AxiosInstance | null>(null);

  useEffect(() => {
    getApiClient().then((client) => setApiClient(client));
  }, []);

  return apiClient;
}
