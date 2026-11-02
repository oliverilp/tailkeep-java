package org.tailkeep.domain.download.model;

import java.util.UUID;

/** Asks for the metadata of the video at {@code url}, for the job. */
public record MetadataRequest(UUID jobId, String url) {
}
