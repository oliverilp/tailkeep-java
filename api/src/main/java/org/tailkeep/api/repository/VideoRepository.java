package org.tailkeep.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tailkeep.api.model.Channel;
import org.tailkeep.api.model.Video;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, String> {
    Optional<Video> findByYoutubeId(String youtubeId);
    List<Video> findByChannel(Channel channel);
}
