package org.tailkeep.domain.video.model;

/** How many downloads a video has, and how many of them have ended. */
public record VideoDownloadCount(long total, long ended) {
    /** A video is done when it has downloads and every one of them has ended. */
    public boolean isDone() {
        return total > 0 && total == ended;
    }
}
