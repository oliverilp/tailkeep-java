package org.tailkeep.domain.download.model;

import java.util.UUID;

/** Asks for the video at {@code url} to be downloaded to {@code filename}, for the job. */
public record DownloadRequest(UUID jobId, String youtubeId, String url, String filename) {
}
