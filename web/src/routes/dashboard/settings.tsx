import React from 'react';
import { createFileRoute } from '@tanstack/react-router';
import Settings from '@/features/settings/settings';

function SettingsPage() {
  const { config } = Route.useRouteContext();

  return <Settings isDemo={config.demoMode} />;
}

export const Route = createFileRoute('/dashboard/settings')({
  component: SettingsPage
});
