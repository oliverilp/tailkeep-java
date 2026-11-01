import React from 'react';
import { Outlet, createFileRoute } from '@tanstack/react-router';

function VideoLayout() {
  return (
    <main className="mb-24 grid h-full w-full items-start gap-4 p-4 sm:px-8 sm:py-0 md:gap-8">
      <Outlet />
    </main>
  );
}

export const Route = createFileRoute('/dashboard/video')({
  component: VideoLayout
});
