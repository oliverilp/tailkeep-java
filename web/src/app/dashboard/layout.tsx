'use client';

import React from 'react';
// import { redirect } from 'next/navigation';
import Header from '@/components/header';
import Sidenav from '@/components/sidenav';
import { AuthProvider } from '@/lib/auth-context';
import { useAuth } from '@/lib/auth-context';

function DashboardLayout({
  children
}: Readonly<{
  children: React.ReactNode;
}>) {
  const { isLoading } = useAuth();

  if (isLoading) {
    return null;
  }

  // if (!user && !isLoading) {
  //   // return redirect('/login');
  // }

  return (
    <AuthProvider>
      <div className="flex min-h-screen w-full bg-muted/40">
        <Sidenav />
        <div className="flex grow flex-col sm:gap-4 sm:py-4">
          <Header />
          {children}
        </div>
      </div>
    </AuthProvider>
  );
}

export default DashboardLayout;
