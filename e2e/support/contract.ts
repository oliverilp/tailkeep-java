// The Java API contract, kept by hand. Paths are relative to
// backend/mod-application/src/main/java/org/tailkeep/api. Java LocalDateTime values are serialized
// as ISO strings with no zone, so they are plain strings here.

// dto/ChannelDto.java
export interface ChannelDto {
  id: string;
  name: string;
  youtubeId: string;
  channelUrl: string;
  createdAt: string;
  updatedAt: string;
}

// dto/VideoDto.java
export interface VideoDto {
  id: string;
  youtubeId: string;
  channel: ChannelDto;
  url: string;
  title: string;
  durationString: string;
  duration: number;
  thumbnailUrl: string;
  description: string;
  viewCount: number;
  commentCount: number;
  filename: string;
  createdAt: string;
  updatedAt: string;
}

// dto/VideoByIdDto.java
export interface VideoByIdDto extends VideoDto {
  doneDownloading: boolean;
}

// dto/DownloadProgressDto.java
export interface DownloadProgressDto {
  id: string;
  video: VideoDto;
  status: string | null;
  hasEnded: boolean;
  progress: number;
  size: string | null;
  speed: string | null;
  eta: string | null;
  createdAt: string;
  updatedAt: string;
  completedAt: string | null;
}

// dto/PageResponseDto.java
export interface PageResponseDto<T> {
  items: T[];
  totalPages: number;
  totalItems: number;
  currentPage: number;
  pageSize: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

// dto/QueueInfoDto.java
export interface QueueInfoDto {
  queue: number;
  active: number;
  finished: number;
}

// dto/DownloadsDashboardDto.java
export interface DownloadsDashboardDto {
  queueInfo: QueueInfoDto;
  downloads: PageResponseDto<DownloadProgressDto>;
}

// dto/AuthenticationResponseDto.java
export interface AuthenticationResponseDto {
  access_token: string;
  refresh_token: string;
}

// dto/ChangePasswordRequestDto.java
export interface ChangePasswordRequestDto {
  currentPassword: string;
  newPassword: string;
  confirmationPassword: string;
}

// exception/ApiError.java
export interface ApiError {
  path: string;
  message: string;
  statusCode: number;
  timestamp: string;
}

// model/user/Role.java
export type Role = 'ADMIN' | 'USER';
