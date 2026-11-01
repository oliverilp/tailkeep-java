import {
  createContext,
  useContext,
  useEffect,
  useState,
  type ReactNode
} from 'react';
import { User } from './auth';
import { useNavigate } from '@tanstack/react-router';
import { jwtDecode } from 'jwt-decode';

interface AuthContextType {
  user: User | null;
  setUser: (user: User | null) => void;
  isLoading: boolean;
}

interface DecodedToken {
  sub: string;
  role: string;
  exp: number;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

// Reads the signed-in user from localStorage, clearing an expired or invalid token.
export function readStoredUser(): User | null {
  const token = localStorage.getItem('accessToken');
  if (!token) {
    return null;
  }

  try {
    const decoded = jwtDecode<DecodedToken>(token);
    const currentTime = Date.now() / 1000;

    if (decoded.exp < currentTime) {
      // Token is expired
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      return null;
    }

    // Valid token, set user
    return {
      username: decoded.sub,
      role: decoded.role,
      nickname: decoded.sub
    };
  } catch (error) {
    // Invalid token
    console.error('Error decoding token:', error);
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const initializeAuth = () => {
      setUser(readStoredUser());
      setIsLoading(false);
    };

    initializeAuth();
  }, []);

  useEffect(() => {
    if (!user && !isLoading) {
      console.log('No user and not loading, redirecting to login');
      void navigate({ to: '/login' });
    }
  }, [user, isLoading, navigate]);

  return (
    <AuthContext.Provider value={{ user, setUser, isLoading }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
