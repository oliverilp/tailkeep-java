import { createFileRoute, redirect } from '@tanstack/react-router';
import { readStoredUser } from '@/lib/auth-context';

export const Route = createFileRoute('/')({
  beforeLoad: () => {
    throw redirect({ to: readStoredUser() ? '/dashboard' : '/login' });
  }
});
