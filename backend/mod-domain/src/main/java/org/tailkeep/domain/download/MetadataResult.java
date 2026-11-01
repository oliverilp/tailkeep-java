package org.tailkeep.domain.download;

import java.util.UUID;

/** The metadata that was found for the job. */
public record MetadataResult(UUID jobId, VideoMetadata metadata) {
}
