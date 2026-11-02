package org.tailkeep.domain.download.port;

import org.tailkeep.domain.download.model.QueueInfo;

public interface CountDownloads {
    /** Counts the downloads that are not deleted. */
    QueueInfo queueInfo();
}
