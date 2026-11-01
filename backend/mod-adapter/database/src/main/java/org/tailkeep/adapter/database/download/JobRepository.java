package org.tailkeep.adapter.database.download;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<JobEntity, UUID> {
    @Query("SELECT COUNT(j) FROM JobEntity j WHERE j.downloadProgress IS NULL")
    long countQueuedJobs();
}
