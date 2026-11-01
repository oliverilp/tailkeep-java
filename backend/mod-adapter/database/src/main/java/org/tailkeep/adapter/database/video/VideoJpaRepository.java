package org.tailkeep.adapter.database.video;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VideoJpaRepository extends JpaRepository<VideoEntity, UUID> {
    Optional<VideoEntity> findByYoutubeId(String youtubeId);

    List<VideoEntity> findAllByOrderByCreatedAtDesc();
}
