package org.tailkeep.domain.download.usecase;

import org.tailkeep.domain.download.model.DownloadFilter;
import org.tailkeep.domain.download.model.DownloadsDashboard;

public interface GetDownloadsDashboard {
    /** The queue counts, and one page of the downloads that are not deleted, newest first. Pages start at 1. */
    DownloadsDashboard execute(DownloadFilter filter, int page, int size);
}
