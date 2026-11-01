package org.tailkeep.adapter.database.channel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChannelJpaRepository extends JpaRepository<ChannelEntity, UUID> {
    Optional<ChannelEntity> findByYoutubeId(String youtubeId);
}
