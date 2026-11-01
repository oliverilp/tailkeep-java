package org.tailkeep.adapter.messaging.contract;

import java.util.UUID;

public record DownloadRequestMessage(UUID jobId, String videoId, String url, String filename) {

}
