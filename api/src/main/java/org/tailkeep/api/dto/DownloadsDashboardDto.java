package org.tailkeep.api.dto;

import java.util.List;

public record DownloadsDashboardDto(
    QueueInfoDto queueInfo,
    List<DownloadProgressDto> downloads
) {} 
