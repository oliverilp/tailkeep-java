package org.tailkeep.domain.download;

import java.util.function.Consumer;

/** Outbound port that downloads a video to the media storage. It knows nothing of the tool that does it. */
public interface VideoDownloader {
    /**
     * Downloads the video and returns when it is stored. {@code onProgress} gets every update on the way.
     *
     * @return the last update, which has {@code hasEnded}
     */
    ProgressUpdate download(DownloadRequest request, Consumer<ProgressUpdate> onProgress);
}
