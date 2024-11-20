'use client';

import React from 'react';
// import { getVideos } from '@/server/data/get-videos';
import { VideoDto } from '@/schemas/video';
import VideoGrid from './video-grid';

function Videos() {
  // const videos: VideoDto[] = await getVideos();

  return <VideoGrid videos={[]} />;
}

export default Videos;
