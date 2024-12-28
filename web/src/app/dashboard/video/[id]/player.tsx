// import { useApiClient } from '@/lib/use-api-client';
// import { useConfig } from '@/lib/use-config';
import '@vidstack/react/player/styles/default/theme.css';
import '@vidstack/react/player/styles/default/layouts/video.css';
import '@vidstack/react/player/styles/default/theme.css';
import '@vidstack/react/player/styles/default/layouts/video.css';
import { MediaPlayer, MediaProvider } from '@vidstack/react';
import {
  defaultLayoutIcons,
  DefaultVideoLayout
} from '@vidstack/react/player/layouts/default';
import { Poster } from '@vidstack/react';
import Image from 'next/image';

import { getRuntimeConfig } from '@/lib/config';
import { VideoByIdDto } from '@/schemas/video-by-id';
import React from 'react';

interface PlayerProps {
  video: VideoByIdDto;
}

function Player({ video }: PlayerProps) {
  const token = localStorage.getItem('accessToken');
  const config = getRuntimeConfig();
  if (!config) {
    return null;
  }

  const { mediaUrl } = config;
  const videoUrl = `${mediaUrl}/media/${video.id}?token=${token}`;

  return (
    <div className="aspect-video w-full overflow-hidden rounded-xl border bg-background font-sans text-white media-fullscreen:rounded-none">
      <MediaPlayer
        className="vds-video-layout"
        // title={video.title}
        src={{ src: videoUrl, type: 'video/mp4' }}
      >
        <MediaProvider className="bg-black media-fullscreen:bg-black">
          <Poster asChild>
            <Image
              alt="Video thumbnail"
              className="absolute inset-0 block h-full w-full rounded-xl object-cover opacity-0 transition-opacity data-[visible]:opacity-100 media-fullscreen:rounded-none"
              fill
              priority
              src={video.thumbnailUrl}
            />
          </Poster>
        </MediaProvider>
        <DefaultVideoLayout
          // thumbnails="https://files.vidstack.io/sprite-fight/thumbnails.vtt"
          icons={defaultLayoutIcons}
        />
      </MediaPlayer>
    </div>
  );
}

export default Player;
