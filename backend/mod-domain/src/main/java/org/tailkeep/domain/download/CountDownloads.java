package org.tailkeep.domain.download;

public interface CountDownloads {
    /** Counts the downloads that are not deleted. */
    QueueInfo queueInfo();
}
