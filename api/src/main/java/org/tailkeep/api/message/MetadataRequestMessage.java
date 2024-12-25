package org.tailkeep.api.message;

import java.util.UUID;

public record MetadataRequestMessage(UUID jobId, String url) {

}
