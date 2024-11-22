import { apiClient } from '@/lib/api-client';
import { VideoDto, videoDtoSchema } from '@/schemas/video';
import { VideoByIdDto, videoByIdDtoSchema } from '@/schemas/video-by-id';

export async function getVideos(): Promise<VideoDto[]> {
  const { data } = await apiClient.get('/videos');
  return videoDtoSchema.array().parse(data);
}

export async function getVideoById(id: string): Promise<VideoByIdDto> {
  const { data } = await apiClient.get(`/videos/${id}`);
  return videoByIdDtoSchema.parse(data);
}
