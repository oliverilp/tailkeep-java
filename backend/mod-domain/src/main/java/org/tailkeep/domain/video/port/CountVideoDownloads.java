package org.tailkeep.domain.video.port;

import org.tailkeep.domain.video.model.VideoDownloadCount;

import java.util.UUID;

public interface CountVideoDownloads {
    VideoDownloadCount byVideoId(UUID videoId);
}
