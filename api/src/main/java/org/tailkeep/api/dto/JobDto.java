package org.tailkeep.api.dto;

public record JobDto(
    String id,
    String inputUrl,
    VideoDto video,
    DownloadProgressDto downloadProgress
) {} 
