package org.tailkeep.domain.download;

public interface RunDownload {
    /**
     * Step 4 of the pipeline: downloads the video, publishing the progress on the way and once more when the
     * download has ended. It returns when the download is over.
     */
    void execute(DownloadRequest request);
}
