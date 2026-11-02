package org.tailkeep.domain.download.usecase;

import org.tailkeep.domain.download.model.MetadataResult;

public interface RecordMetadata {
    /**
     * Step 3 of the pipeline: saves the channel and video of the metadata, attaches the video to the job and
     * publishes a download request. Running it twice for one result gives the same data.
     *
     * @throws org.tailkeep.domain.shared.ResourceNotFoundException when there is no such job
     */
    void execute(MetadataResult result);
}
