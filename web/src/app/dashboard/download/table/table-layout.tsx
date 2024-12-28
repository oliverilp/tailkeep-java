import React from 'react';
import Link from 'next/link';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { capitalize } from '@/lib/utils';
import DownloadsCard from './table-card';
import { DownloadsPageResponse } from '@/schemas/downloads-dashboard';

interface DownloadsTableLayoutProps {
  downloads: DownloadsPageResponse;
  progress: string;
}

function DownloadsTableLayout({
  downloads,
  progress
}: DownloadsTableLayoutProps) {
  const tabs = ['all', 'active', 'done'];

  return (
    <Tabs defaultValue={progress}>
      <div className="mb-2 flex items-center">
        <TabsList>
          {tabs.map((tab) => (
            <Link
              href={`?progress=${tab}&page=1`}
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
