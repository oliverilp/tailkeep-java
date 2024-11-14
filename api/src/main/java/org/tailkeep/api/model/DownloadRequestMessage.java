package org.tailkeep.api.model;

public record DownloadRequestMessage(String jobId, String videoId, String url) {

}
