package org.tailkeep.domain.video.port;

import org.tailkeep.domain.video.model.Video;

public interface SaveVideo {
    /** Inserts the video when it has no id, otherwise updates it. Returns the saved video. */
    Video save(Video video);
}
