package org.tailkeep.api.dto;

import java.util.UUID;

public record JobDto(
    UUID id,
    String inputUrl,
    VideoDto video,
    DownloadProgressDto downloadProgress
) {} 
