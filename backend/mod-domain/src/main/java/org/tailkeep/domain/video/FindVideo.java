package org.tailkeep.domain.video;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FindVideo {
    Optional<Video> byId(UUID id);

    Optional<Video> byYoutubeId(String youtubeId);

    List<Video> newestFirst();
}
