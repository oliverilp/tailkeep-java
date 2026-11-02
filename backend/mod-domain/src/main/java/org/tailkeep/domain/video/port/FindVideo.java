package org.tailkeep.domain.video.port;

import org.tailkeep.domain.video.model.Video;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FindVideo {
    Optional<Video> byId(UUID id);

    Optional<Video> byYoutubeId(String youtubeId);

    List<Video> byIds(Collection<UUID> ids);

    List<Video> newestFirst();
}
