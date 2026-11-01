import React from 'react';
import { createFileRoute } from '@tanstack/react-router';
import { useQuery } from '@tanstack/react-query';
import { getVideos } from '@/api/videos';
import VideoGrid from '@/features/videos/video-grid';

function Videos() {
  const { data: videos, isLoading } = useQuery({
    queryKey: ['videos'],
    queryFn: getVideos
  });

  if (isLoading) {
    return <div>Loading...</div>;
  }

  return <VideoGrid videos={videos} />;
}

export const Route = createFileRoute('/dashboard/video/')({
  component: Videos
});
