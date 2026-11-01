import React from 'react';
import { Link } from '@tanstack/react-router';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { capitalize } from '@/lib/utils';
import DownloadsCard from './table-card';
import { DownloadsPageResponse } from '@/schemas/downloads-dashboard';
import type { DownloadsTab } from '../downloads';

interface DownloadsTableLayoutProps {
  downloads: DownloadsPageResponse;
  progress: DownloadsTab;
}

function DownloadsTableLayout({
  downloads,
  progress
}: DownloadsTableLayoutProps) {
  const tabs = ['all', 'active', 'done'] as const;

  return (
    <Tabs defaultValue={progress}>
      <div className="mb-2 flex items-center">
        <TabsList>
          {tabs.map((tab) => (
            <Link
              to="/dashboard/download"
              search={{ progress: tab, page: 1 }}
              key={tab}
              className="cursor-pointer"
            >
              <TabsTrigger value={tab}>{capitalize(tab)}</TabsTrigger>
            </Link>
          ))}
        </TabsList>
      </div>
      <DownloadsCard downloads={downloads} progress={progress} />
    </Tabs>
  );
}

export default DownloadsTableLayout;
