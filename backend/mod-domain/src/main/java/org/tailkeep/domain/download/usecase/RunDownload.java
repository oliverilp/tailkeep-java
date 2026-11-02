package org.tailkeep.domain.download.usecase;

import org.tailkeep.domain.download.model.DownloadRequest;

public interface RunDownload {
    /**
     * Step 4 of the pipeline: downloads the video, publishing the progress on the way and once more when the
     * download has ended. It returns when the download is over. A request for a job whose download has already
     * ended or was deleted does nothing, because the request may be delivered more than once. A job that is not
     * finished is downloaded again from the start of the request, which is what recovers from a crash.
     */
    void execute(DownloadRequest request);
}
