package org.tailkeep.domain.download.port;

import org.tailkeep.domain.download.model.ProgressUpdate;

public interface PublishDownloadProgress {
    void publish(ProgressUpdate update);
}
