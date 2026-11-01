import { useMutation, useQueryClient } from '@tanstack/react-query';
import { LoginCredentials, AuthResponse } from './auth';
import { useAuth } from './auth-context';
import { useNavigate } from '@tanstack/react-router';
import { jwtDecode } from 'jwt-decode';
import { toast } from 'sonner';
import { apiClient } from '@/lib/api-client';

interface DecodedToken {
  sub: string;
  role: string;
  exp: number;
}

export function useLogin() {
  const { setUser } = useAuth();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (credentials: LoginCredentials) => {
      if (!apiClient) throw new Error('API client not initialized');

      const { data } = await apiClient.post<AuthResponse>(
        '/auth/authenticate',
        credentials
      );
      return data;
    },
    onError: (error) => {
      toast.error('Failed to login');
      console.error('Login error:', error);

      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      setUser(null);
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
      void navigate({ to: '/dashboard' });
    }
  });
}

export function useLogout() {
  const { setUser } = useAuth();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async () => {
      if (!apiClient) throw new Error('API client not initialized');

      try {
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
      void navigate({ to: '/login' });
    }
  });
}
