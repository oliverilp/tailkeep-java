package org.tailkeep.domain.download.port;

import org.tailkeep.domain.download.model.DownloadRequest;

public interface PublishDownloadRequest {
    void publish(DownloadRequest request);
}
