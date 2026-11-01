package org.tailkeep.api.message;

import java.util.UUID;

public record DownloadRequestMessage(UUID jobId, String videoId, String url, String filename) {

}
