package org.tailkeep.domain.download.model;

import org.tailkeep.domain.shared.PageResult;

public record DownloadsDashboard(QueueInfo queueInfo, PageResult<DownloadDetails> downloads) {
}
