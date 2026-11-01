package org.tailkeep.api.dto;

import org.tailkeep.adapter.web.video.VideoResponse;
import java.util.UUID;

public record JobDto(
    UUID id,
    String inputUrl,
    VideoResponse video,
    DownloadProgressDto downloadProgress
) {} 
