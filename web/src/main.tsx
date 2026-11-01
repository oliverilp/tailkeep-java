import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { RouterProvider, createRouter } from '@tanstack/react-router';
import { RouteError } from '@/components/route-fallbacks';
import { loadRuntimeConfig } from '@/lib/config';
import { routeTree } from './routeTree.gen';
import './globals.css';

// Internal links never end in a slash, so this only applies to typed URLs.
const { pathname, search, hash } = window.location;
if (pathname.length > 1 && pathname.endsWith('/')) {
  const trimmed = pathname.replace(/\/+$/, '');
  window.history.replaceState(null, '', `${trimmed}${search}${hash}`);
}

// Login and settings depend on demo mode, so nothing renders until it is known.
const config = await loadRuntimeConfig();

const router = createRouter({
  routeTree,
  context: { config },
  // An unknown URL under /dashboard renders without the dashboard layout.
  notFoundMode: 'root',
  defaultErrorComponent: RouteError
});

declare module '@tanstack/react-router' {
  interface Register {
    router: typeof router;
  }
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>
);
