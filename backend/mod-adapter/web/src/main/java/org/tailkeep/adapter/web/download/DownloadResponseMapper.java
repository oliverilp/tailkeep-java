package org.tailkeep.adapter.web.download;

import org.tailkeep.adapter.web.shared.PageResponse;
import org.tailkeep.adapter.web.video.VideoResponseMapper;
import org.tailkeep.domain.download.model.DownloadDetails;
import org.tailkeep.domain.download.model.DownloadProgress;
import org.tailkeep.domain.download.model.DownloadsDashboard;
import org.tailkeep.domain.download.model.QueueInfo;
import org.tailkeep.domain.shared.PageResult;

public final class DownloadResponseMapper {
    private DownloadResponseMapper() {
    }

    public static DownloadResponse toResponse(DownloadDetails details) {
        DownloadProgress download = details.download();
        return new DownloadResponse(
                download.jobId(),
                VideoResponseMapper.toResponse(details.video(), details.channel()),
                download.status(),
                download.hasEnded(),
                download.progress(),
                download.size(),
                download.speed(),
                download.eta(),
                download.createdAt(),
                download.updatedAt(),
                download.completedAt());
    }

    public static DashboardResponse toDashboardResponse(DownloadsDashboard dashboard) {
        QueueInfo queueInfo = dashboard.queueInfo();
        PageResult<DownloadDetails> page = dashboard.downloads();
        return new DashboardResponse(
                new QueueInfoResponse(queueInfo.queue(), queueInfo.active(), queueInfo.finished()),
                new PageResponse<>(
                        page.items().stream().map(DownloadResponseMapper::toResponse).toList(),
                        page.totalPages(),
                        page.totalItems(),
                        page.page(),
                        page.size(),
                        page.hasNext(),
                        page.hasPrevious()));
    }
}
