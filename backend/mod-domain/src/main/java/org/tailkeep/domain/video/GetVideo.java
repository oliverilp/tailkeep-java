package org.tailkeep.domain.video;

import java.util.UUID;

public interface GetVideo {
    /** @throws org.tailkeep.domain.shared.ResourceNotFoundException when there is no such video */
    VideoDetails execute(UUID id);
}
