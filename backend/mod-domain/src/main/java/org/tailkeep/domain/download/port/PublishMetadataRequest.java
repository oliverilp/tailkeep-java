package org.tailkeep.domain.download.port;

import org.tailkeep.domain.download.model.MetadataRequest;

public interface PublishMetadataRequest {
    void publish(MetadataRequest request);
}
