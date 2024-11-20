'use client';

import React from 'react';
import { useQuery } from '@tanstack/react-query';
import AddVideo from '@/app/dashboard/download/add-video';
import DownloadsTableLayout from '@/app/dashboard/download/table/table-layout';
import DownloadsInfo from './downloads-info';
import { getDownloadsDashboard } from '@/api/downloads';

function Downloads() {
  const { data: dashboardData } = useQuery({
    queryKey: ['downloads-dashboard'],
    queryFn: getDownloadsDashboard,
    refetchInterval: 1000 // Poll every second
  });

  if (!dashboardData) {
    return null;
  }

  return (
    <>
      <AddVideo />
      <DownloadsInfo queueInfo={dashboardData.queueInfo} />
      <DownloadsTableLayout items={dashboardData.downloads} />
    </>
  );
}

export default Downloads;
