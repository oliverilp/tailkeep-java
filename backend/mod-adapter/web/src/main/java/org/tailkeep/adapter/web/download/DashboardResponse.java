package org.tailkeep.adapter.web.download;

import org.tailkeep.adapter.web.shared.PageResponse;

public record DashboardResponse(QueueInfoResponse queueInfo, PageResponse<DownloadResponse> downloads) {
}
