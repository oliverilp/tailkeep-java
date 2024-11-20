package org.tailkeep.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tailkeep.api.model.DownloadProgress;
import org.tailkeep.api.model.Job;
import org.tailkeep.api.model.Video;

import java.util.List;

@Repository
public interface DownloadProgressRepository extends JpaRepository<DownloadProgress, String> {
    List<DownloadProgress> findByJob(Job job);
    List<DownloadProgress> findByVideo(Video video);

    long countByHasEndedTrue();
    long countByHasEndedFalse();
}
