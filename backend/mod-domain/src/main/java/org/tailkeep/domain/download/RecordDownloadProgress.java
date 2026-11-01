package org.tailkeep.domain.download;

public interface RecordDownloadProgress {
    /**
     * Steps 5 and 6 of the pipeline: saves the progress of the job, completing the download when the update
     * has ended, and broadcasts the saved progress.
     *
     * @throws org.tailkeep.domain.shared.ResourceNotFoundException when there is no such job, or an ended
     *         update arrives for a job that has no progress yet
     */
    DownloadDetails execute(ProgressUpdate update);
}
