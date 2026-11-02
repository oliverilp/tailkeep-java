package org.tailkeep.domain.download.port;

import org.tailkeep.domain.download.model.MetadataResult;

public interface PublishMetadataResult {
    void publish(MetadataResult result);
}
