package org.tailkeep.domain.download.usecase;

import org.tailkeep.domain.download.model.DownloadDetails;

import java.util.UUID;

public interface GetDownload {
    /** @throws org.tailkeep.domain.shared.ResourceNotFoundException when the job has no progress */
    DownloadDetails execute(UUID jobId);
}
