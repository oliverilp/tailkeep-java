import { randomUUID } from 'node:crypto';
import type {
  ApiError,
  ChannelDto,
  DownloadProgressDto,
  VideoDto
} from './contract';

const timestamp = '2024-11-01T12:00:00.123456';

export function aChannel(overrides: Partial<ChannelDto> = {}): ChannelDto {
  return {
    id: randomUUID(),
    name: 'Test Channel',
    youtubeId: 'UCtestchannel',
    channelUrl: 'https://www.youtube.com/channel/UCtestchannel',
    createdAt: timestamp,
    updatedAt: timestamp,
    ...overrides
  };
}

export function aVideo(overrides: Partial<VideoDto> = {}): VideoDto {
  const youtubeId = overrides.youtubeId ?? randomUUID().slice(0, 11);

  return {
    id: randomUUID(),
    youtubeId,
    channel: aChannel(),
    url: `https://www.youtube.com/watch?v=${youtubeId}`,
    title: 'Test video',
    durationString: '3:32',
    duration: 212,
    // The current app only allows this host for thumbnails.
    thumbnailUrl: `https://i.ytimg.com/vi/${youtubeId}/maxresdefault.jpg`,
    description: 'A test video description.',
    viewCount: 1234,
    commentCount: 56,
    filename: `${youtubeId}.mp4`,
    createdAt: timestamp,
    updatedAt: timestamp,
    ...overrides
  };
}

export function anActiveDownload(
  overrides: Partial<DownloadProgressDto> = {}
): DownloadProgressDto {
  return {
    id: randomUUID(),
    video: aVideo(),
    status: 'downloading',
    hasEnded: false,
    progress: 30,
    size: '120.00MiB',
    speed: '4.20MiB/s',
    eta: '00:20',
    createdAt: timestamp,
    updatedAt: timestamp,
    completedAt: null,
    ...overrides
  };
}

export function aFinishedDownload(
  overrides: Partial<DownloadProgressDto> = {}
): DownloadProgressDto {
  return anActiveDownload({
    status: 'finished',
    hasEnded: true,
    progress: 100,
    speed: null,
    eta: null,
    completedAt: timestamp,
    ...overrides
  });
}

export function anApiError(
  path: string,
  message: string,
  statusCode: number
): ApiError {
  return { path, message, statusCode, timestamp };
}
