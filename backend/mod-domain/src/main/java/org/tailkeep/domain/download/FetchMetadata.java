package org.tailkeep.domain.download;

public interface FetchMetadata {
    /** Step 2 of the pipeline: finds the metadata of the video and publishes it as a result. */
    void execute(MetadataRequest request);
}
