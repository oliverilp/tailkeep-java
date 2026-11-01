import React from 'react';
import { Outlet, createFileRoute, redirect } from '@tanstack/react-router';
import Header from '@/components/header';
import Sidenav from '@/components/sidenav';
import { AuthProvider, readStoredUser, useAuth } from '@/lib/auth-context';

function DashboardLayout() {
  const { isLoading } = useAuth();

  if (isLoading) {
    return null;
  }

  return (
    <AuthProvider>
      <div className="flex min-h-screen w-full bg-muted/40">
        <Sidenav />
        <div className="flex grow flex-col sm:gap-4 sm:py-4">
          <Header />
          <Outlet />
        </div>
      </div>
    </AuthProvider>
  );
}

export const Route = createFileRoute('/dashboard')({
  // Only on entering the dashboard: a session that expires later ends through
  // the API client's 401 handling.
  beforeLoad: ({ cause }) => {
    if (cause === 'enter' && !readStoredUser()) {
      throw redirect({ to: '/login' });
    }
  },
  component: DashboardLayout
});
