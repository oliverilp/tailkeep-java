package org.tailkeep.api.dto;

public record DownloadRequestMessage(String jobId, String videoId, String url, String filename) {

}
