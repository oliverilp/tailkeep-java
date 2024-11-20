package org.tailkeep.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.tailkeep.api.model.Job;
import org.tailkeep.api.model.Video;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, String> {
    List<Job> findByVideo(Video video);
    List<Job> findByVideoIsNull();
    @Query("SELECT COUNT(j) FROM Job j WHERE j.downloadProgress IS NULL")
    long countQueuedJobs();
}
