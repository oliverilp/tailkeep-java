'use client';

import { useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from './api-client';
import { LoginCredentials, AuthResponse } from './auth';
import { useAuth } from './auth-context';
import { useRouter } from 'next/navigation';
import { jwtDecode } from 'jwt-decode';
import { toast } from 'sonner';

interface DecodedToken {
  sub: string;
  role: string;
  exp: number;
}

export function useLogin() {
  const { setUser } = useAuth(); // Remove user from destructuring as it's not needed
  const router = useRouter();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (credentials: LoginCredentials) => {
      const { data } = await apiClient.post<AuthResponse>(
        '/auth/authenticate',
        credentials
      );
      return data;
    },
    onError: () => {
      toast.error('Failed to change password');
    },
    onSuccess: (data) => {
      localStorage.setItem('accessToken', data.access_token);
      localStorage.setItem('refreshToken', data.refresh_token);

      const decoded = jwtDecode<DecodedToken>(data.access_token);
      const user = {
        username: decoded.sub,
        role: decoded.role,
        nickname: decoded.sub
      };

      setUser(user);
      queryClient.clear();
      router.push('/dashboard');
    }
  });
}

export function useLogout() {
  const { setUser } = useAuth();
  const router = useRouter();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async () => {
      try {
        // Optional: Call logout endpoint if your API has one
        await apiClient.post('/auth/logout');
      } catch (error) {
        console.error('Logout error:', error);
      }
    },
    onSuccess: () => {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      setUser(null);
      queryClient.clear();
      router.push('/login');
    }
  });
}
