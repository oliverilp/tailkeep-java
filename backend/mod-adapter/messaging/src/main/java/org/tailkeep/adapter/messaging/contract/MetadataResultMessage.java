package org.tailkeep.adapter.messaging.contract;

import java.util.UUID;

public record MetadataResultMessage(UUID jobId, Metadata metadata) {

}
