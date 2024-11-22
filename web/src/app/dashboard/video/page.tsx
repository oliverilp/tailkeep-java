'use client';

import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { getVideos } from '@/api/videos';
import VideoGrid from './video-grid';

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

export default Videos;
