package org.tailkeep.api.message;

import java.util.UUID;

public record MetadataResultMessage(UUID jobId, Metadata metadata) {

}
