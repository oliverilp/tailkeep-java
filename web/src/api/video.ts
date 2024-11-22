import { apiClient } from '@/lib/api-client';
import { VideoDto, videoDtoSchema } from '@/schemas/video';

export async function getVideos(): Promise<VideoDto[]> {
  const { data } = await apiClient.get('/videos');
  return videoDtoSchema.array().parse(data);
}

export async function getVideoById(id: string): Promise<VideoDto> {
  const { data } = await apiClient.get(`/videos/${id}`);
  return videoDtoSchema.parse(data);
}
