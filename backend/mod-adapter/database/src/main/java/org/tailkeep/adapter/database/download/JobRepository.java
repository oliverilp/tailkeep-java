package org.tailkeep.adapter.database.download;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.tailkeep.adapter.database.video.VideoEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<JobEntity, UUID> {
    List<JobEntity> findByVideo(VideoEntity video);
    List<JobEntity> findByVideoIsNull();
    @Query("SELECT COUNT(j) FROM JobEntity j WHERE j.downloadProgress IS NULL")
    long countQueuedJobs();
}
