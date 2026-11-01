package org.tailkeep.domain.download;

import org.tailkeep.domain.channel.FindChannel;
import org.tailkeep.domain.shared.PageQuery;
import org.tailkeep.domain.shared.PageResult;
import org.tailkeep.domain.shared.Sort;
import org.tailkeep.domain.shared.UseCase;
import org.tailkeep.domain.video.FindVideo;

@UseCase
public class GetDownloadsDashboardImpl implements GetDownloadsDashboard {
    private final FindDownload findDownload;
    private final CountDownloads countDownloads;
    private final DownloadDetailsAssembler assembler;

    public GetDownloadsDashboardImpl(
            FindDownload findDownload, CountDownloads countDownloads, FindVideo findVideo, FindChannel findChannel) {
        this.findDownload = findDownload;
        this.countDownloads = countDownloads;
        this.assembler = new DownloadDetailsAssembler(findVideo, findChannel);
    }

    @Override
    public DownloadsDashboard execute(DownloadFilter filter, int page, int size) {
        QueueInfo queueInfo = countDownloads.queueInfo();
        PageResult<DownloadProgress> downloads =
                findDownload.notDeleted(filter, new PageQuery(page, size, Sort.descending("createdAt")));

        return new DownloadsDashboard(queueInfo,
                new PageResult<>(assembler.assemble(downloads.items()), downloads.page(), downloads.size(),
                        downloads.totalItems()));
    }
}
