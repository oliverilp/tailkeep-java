package org.tailkeep.domain.video;

import java.util.UUID;

public interface CountVideoDownloads {
    VideoDownloadCount byVideoId(UUID videoId);
}
