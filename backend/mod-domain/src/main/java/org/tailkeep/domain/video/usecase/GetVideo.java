package org.tailkeep.domain.video.usecase;

import org.tailkeep.domain.video.model.VideoDetails;

import java.util.UUID;

public interface GetVideo {
    /** @throws org.tailkeep.domain.shared.ResourceNotFoundException when there is no such video */
    VideoDetails execute(UUID id);
}
