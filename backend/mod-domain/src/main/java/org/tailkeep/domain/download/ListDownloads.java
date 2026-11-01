package org.tailkeep.domain.download;

import java.util.List;

public interface ListDownloads {
    /** Every download, newest first. */
    List<DownloadDetails> execute();
}
