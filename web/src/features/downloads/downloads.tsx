import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { useSearch } from '@tanstack/react-router';
import { z } from 'zod';
import AddVideo from './add-video';
import DownloadsTableLayout from './table/table-layout';
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

export type DownloadsTab = z.infer<typeof tabSchema>;

export interface DownloadsSearch {
  progress?: DownloadsTab;
  page?: number;
}

function Downloads() {
  const search = useSearch({ from: '/dashboard/download' });

  const validationResult = paramsSchema.safeParse(search);
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
