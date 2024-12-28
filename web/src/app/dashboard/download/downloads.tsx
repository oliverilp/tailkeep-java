'use client';

import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { useSearchParams } from 'next/navigation';
import { z } from 'zod';
import AddVideo from '@/app/dashboard/download/add-video';
import DownloadsTableLayout from '@/app/dashboard/download/table/table-layout';
import DownloadsInfo from './downloads-info';
import { getDownloadsDashboard } from '@/api/downloads';

const tabSchema = z.union([
  z.literal('all'),
  z.literal('active'),
  z.literal('done')
]);

const paramsSchema = z.object({
  progress: tabSchema.default('all'),
  page: z.coerce.number().positive().int().default(1)
});

function Downloads() {
  const searchParams = useSearchParams();
  const queryEntries = Object.fromEntries(searchParams.entries());

  const validationResult = paramsSchema.safeParse(queryEntries);
  const { progress, page } = validationResult.success
    ? validationResult.data
    : { progress: 'all' as const, page: 1 };

  const { data: dashboardData } = useQuery({
    queryKey: ['downloads-dashboard', page, progress],
    queryFn: () => getDownloadsDashboard(page, progress),
    refetchInterval: 1000
  });

  if (!dashboardData) {
    return <div>Loading...</div>;
  }

  return (
    <>
      <AddVideo />
      <DownloadsInfo queueInfo={dashboardData.queueInfo} />
      <DownloadsTableLayout
        downloads={dashboardData.downloads}
        progress={progress}
      />
    </>
  );
}

export default Downloads;
