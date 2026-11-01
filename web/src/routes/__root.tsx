import React from 'react';
import { Outlet, createRootRouteWithContext } from '@tanstack/react-router';
import { Providers } from '@/components/providers';
import { NotFound } from '@/components/route-fallbacks';
import type { RuntimeConfig } from '@/schemas/runtime-config';
import { Toaster } from 'sonner';

interface RouterContext {
  config: RuntimeConfig;
}

function RootLayout(): React.JSX.Element {
  return (
    <>
      <Providers>
        <Outlet />
      </Providers>
      <Toaster offset={32} mobileOffset={20} />
    </>
  );
}

export const Route = createRootRouteWithContext<RouterContext>()({
  component: RootLayout,
  notFoundComponent: NotFound
});
