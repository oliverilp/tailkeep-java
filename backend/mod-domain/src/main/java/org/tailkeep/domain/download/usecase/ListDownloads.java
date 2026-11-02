package org.tailkeep.domain.download.usecase;

import org.tailkeep.domain.download.model.DownloadDetails;

import java.util.List;

public interface ListDownloads {
    /** Every download, newest first. */
    List<DownloadDetails> execute();
}
