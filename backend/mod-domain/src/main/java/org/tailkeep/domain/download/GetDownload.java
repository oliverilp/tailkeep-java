package org.tailkeep.domain.download;

import java.util.UUID;

public interface GetDownload {
    /** @throws org.tailkeep.domain.shared.ResourceNotFoundException when the job has no progress */
    DownloadDetails execute(UUID jobId);
}
