package org.tailkeep.domain.video.usecase;

import org.tailkeep.domain.video.model.VideoWithChannel;

import java.util.List;

public interface ListVideos {
    /** Every video, newest first. */
    List<VideoWithChannel> execute();
}
