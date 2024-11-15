package org.tailkeep.worker.download;

public record DownloadRequestMessage(String jobId, String videoId, String url, String filename) {

}
