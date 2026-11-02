package org.tailkeep.domain.download.port;

import org.tailkeep.domain.download.model.VideoMetadata;

/** Outbound port that finds out what a video is. It knows nothing of the tool or service that does it. */
public interface VideoMetadataProvider {
    VideoMetadata fetch(String url);
}
