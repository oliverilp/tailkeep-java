'use client';

import React from 'react';
// import { validateRequest } from '@/lib/auth';
// import { redirect } from 'next/navigation';
import { useAuth } from '@/lib/auth-context';
import Login from './login';

function LoginPage() {
  // const { user } = await validateRequest();
  // if (user) {
  //   return redirect('/dashboard');
  // }

  // const { user } = useAuth();

  // if (user) {
  //   return redirect('/dashboard');
  // }

  const isDemo = process.env.DEMO_MODE === 'true';
  console.log('isDemo', isDemo);

  return (
    <div className="h-full w-full">
      <Login isDemo={isDemo} />
    </div>
  );
}

export default LoginPage;
