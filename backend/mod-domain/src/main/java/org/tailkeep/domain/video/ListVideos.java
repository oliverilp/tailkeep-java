package org.tailkeep.domain.video;

import java.util.List;

public interface ListVideos {
    /** Every video, newest first. */
    List<VideoWithChannel> execute();
}
