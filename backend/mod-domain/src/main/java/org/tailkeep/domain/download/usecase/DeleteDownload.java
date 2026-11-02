package org.tailkeep.domain.download.usecase;

import java.util.UUID;

public interface DeleteDownload {
    /**
     * Hides the download from the dashboard. The data is kept.
     *
     * @throws org.tailkeep.domain.shared.ResourceNotFoundException when the job has no progress
     */
    void execute(UUID jobId);
}
