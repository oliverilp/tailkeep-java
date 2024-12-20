import React from 'react';
import Settings from './settings';

function SettingsPage() {
  const isDemo = process.env.DEMO_MODE === 'true';

  return <Settings isDemo={isDemo} />;
}

export default SettingsPage;
