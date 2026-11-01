package org.tailkeep.domain.download;

import org.tailkeep.domain.shared.PageQuery;
import org.tailkeep.domain.shared.PageResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FindDownload {
    /** The progress of a job, deleted or not. */
    Optional<DownloadProgress> byJobId(UUID jobId);

    /** Every download, deleted or not, newest first. */
    List<DownloadProgress> newestFirst();

    /** One page of the downloads that are not deleted, newest first. */
    PageResult<DownloadProgress> notDeleted(DownloadFilter filter, PageQuery query);
}
