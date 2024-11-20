'use client';

import axios from 'axios';

const API_URL = 'http://localhost:8080/api/v1';

export const apiClient = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: true
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
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
        const response = await apiClient.post('/auth/refresh-token', null, {
          headers: {
            Authorization: `Bearer ${refreshToken}`
          }
        });

        const { access_token } = response.data;
        if (!access_token) {
          return Promise.reject(error);
        }

        localStorage.setItem('accessToken', access_token);
        originalRequest.headers.Authorization = `Bearer ${access_token}`;
        return apiClient(originalRequest);
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
