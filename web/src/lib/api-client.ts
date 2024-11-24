'use client';

import axios, {
  AxiosError,
  AxiosInstance,
  AxiosRequestConfig,
  AxiosResponse,
  InternalAxiosRequestConfig
} from 'axios';
import { getRuntimeConfig } from './config';

interface RetryableRequest extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

let apiClientInstance: any = null;

export function getApiClient(): AxiosInstance {
  const { apiUrl: host } = getRuntimeConfig();
  const API_URL = `${host}/api/v1`;

  console.log('API_URL', API_URL);

  apiClientInstance = axios.create({
    baseURL: API_URL,
    headers: {
      'Content-Type': 'application/json'
    },
    withCredentials: true
  });

  apiClientInstance.interceptors.request.use((config: AxiosRequestConfig) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      if (!config.headers) {
        config.headers = {};
      }
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  });

  apiClientInstance.interceptors.response.use(
    (response: AxiosResponse) => response,
    async (error: AxiosError) => {
      const originalRequest = error.config as RetryableRequest;
      if (!originalRequest) {
        return Promise.reject(error);
      }

      const refreshToken = localStorage.getItem('refreshToken');

      // Only attempt token refresh if:
      // 1. It's a 401 error
      // 2. We haven't tried refreshing already
      // 3. The request URL is not the authentication endpoint
      // 4. We have a refresh token
      if (
        error.response?.status === 401 &&
        !originalRequest._retry &&
        !originalRequest.url?.includes('/auth/authenticate') &&
        refreshToken
      ) {
        originalRequest._retry = true;

        try {
          const response = await apiClientInstance.post(
            '/auth/refresh-token',
            null,
            {
              headers: {
                Authorization: `Bearer ${refreshToken}`
              }
            }
          );

          const { access_token } = response.data;
          if (!access_token) {
            return Promise.reject(error);
          }

          localStorage.setItem('accessToken', access_token);
          originalRequest.headers.Authorization = `Bearer ${access_token}`;
          return apiClientInstance(originalRequest);
        } catch (error) {
          // Clear tokens on refresh failure
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          console.error('Token refresh failed:', error);
        }
      }

      return Promise.reject(error);
    }
  );

  return apiClientInstance;
}

export const apiClient = getApiClient();
