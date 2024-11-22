import { VideoByIdDto } from '@/schemas/video-by-id';
import React from 'react';

interface PlayerProps {
  video: VideoByIdDto;
}

function Player({ video }: PlayerProps) {
  const token = localStorage.getItem('accessToken');
  const videoUrl = `http://localhost:5000/media/${video.id}?token=${token}`;

  return (
    <video
      className="aspect-video h-full w-full rounded-xl object-cover"
      poster={video.thumbnailUrl}
      controls
      autoPlay={false}
    >
      <source src={videoUrl} type="video/mp4" />
    </video>
  );
}

export default Player;
