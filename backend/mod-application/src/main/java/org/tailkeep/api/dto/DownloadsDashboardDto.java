package org.tailkeep.api.dto;

public record DownloadsDashboardDto(
    QueueInfoDto queueInfo,
    PageResponseDto<DownloadProgressDto> downloads
) {} 
