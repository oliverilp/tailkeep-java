package org.tailkeep.api.message;

public record DownloadRequestMessage(String jobId, String videoId, String url, String filename) {

}
