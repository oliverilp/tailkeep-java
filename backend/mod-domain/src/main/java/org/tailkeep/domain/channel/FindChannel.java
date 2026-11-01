package org.tailkeep.domain.channel;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FindChannel {
    Optional<Channel> byId(UUID id);

    Optional<Channel> byYoutubeId(String youtubeId);

    List<Channel> byIds(Collection<UUID> ids);
}
