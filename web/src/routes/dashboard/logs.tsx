import { createFileRoute } from '@tanstack/react-router';
import { FileClock } from 'lucide-react';
import React from 'react';

function Logs() {
  return (
    <main className="grid h-full w-full place-items-center">
      <div className="flex w-full flex-col items-center">
        <FileClock className="mb-2 h-12 w-12 text-muted-foreground" />
        <div className="font-semibold">Logs page</div>
        <div className="text-muted-foreground">
          This feature is coming soon™.
        </div>
      </div>
    </main>
  );
}

export const Route = createFileRoute('/dashboard/logs')({
  component: Logs
});
