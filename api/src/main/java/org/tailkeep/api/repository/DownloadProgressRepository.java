package org.tailkeep.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tailkeep.api.model.DownloadProgress;
import org.tailkeep.api.model.Video;

import java.util.List;
import java.util.Optional;

@Repository
public interface DownloadProgressRepository extends JpaRepository<DownloadProgress, String> {
    Optional<DownloadProgress> findByJobId(String jobId);
    List<DownloadProgress> findByVideo(Video video);
}
