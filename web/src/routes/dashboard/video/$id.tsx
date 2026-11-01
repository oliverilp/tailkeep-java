import React from 'react';
import { createFileRoute } from '@tanstack/react-router';
import { useQuery } from '@tanstack/react-query';
import { z } from 'zod';
import { getVideoById } from '@/api/videos';
import Player from '@/features/videos/player';

const idSchema = z.string();

function VideoDetails() {
  const params = Route.useParams();
  const parsed = idSchema.safeParse(params.id);

  if (!parsed.success) {
    return <div>404 - cannot be found</div>;
  }

  const { data: video, isLoading } = useQuery({
    queryKey: ['video', parsed.data],
    queryFn: () => getVideoById(parsed.data)
  });

  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (!video) {
    return <div>404 - cannot be found</div>;
  }

  return (
    <div className="flex max-w-5xl flex-col gap-4">
      <div className="h-full w-full">
        {video.doneDownloading ? (
          <Player video={video} />
        ) : (
          <figure className="relative">
            <div className="w-full overflow-hidden rounded-xl">
              <img
                alt="Video thumbnail"
                className="aspect-video w-full scale-105 text-transparent blur-md brightness-50 filter"
                decoding="async"
                height="1280"
                loading="lazy"
                src={video.thumbnailUrl}
                width="720"
              />
            </div>
            <figcaption className="absolute left-1/2 top-1/2 z-10 -translate-x-1/2 -translate-y-1/2 transform text-center text-2xl font-semibold text-white sm:text-4xl">
              This video is still downloading.
            </figcaption>
          </figure>
        )}
      </div>
      <h4 className="scroll-m-20 pb-2 text-3xl font-semibold tracking-tight first:mt-0">
        {video.title}
      </h4>
      <div className="font-semibold">{video.channel.name}</div>
      <div className="pb-12 pt-6">
        {video.description.split('\n').map((line: string, index: number) => (
          <p key={index}>{line}</p>
        ))}
      </div>
    </div>
  );
}

export const Route = createFileRoute('/dashboard/video/$id')({
  component: VideoDetails
});
