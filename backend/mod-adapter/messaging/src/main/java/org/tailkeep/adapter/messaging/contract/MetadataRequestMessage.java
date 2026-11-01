package org.tailkeep.adapter.messaging.contract;

import java.util.UUID;

public record MetadataRequestMessage(UUID jobId, String url) {

}
