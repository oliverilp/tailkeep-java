package org.tailkeep.domain.download;

import org.tailkeep.domain.shared.PageResult;

public record DownloadsDashboard(QueueInfo queueInfo, PageResult<DownloadDetails> downloads) {
}
