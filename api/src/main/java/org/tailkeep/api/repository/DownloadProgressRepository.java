package org.tailkeep.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.tailkeep.api.model.DownloadProgress;
import org.tailkeep.api.model.Job;
import org.tailkeep.api.model.Video;

import java.util.List;
import java.util.UUID;

@Repository
public interface DownloadProgressRepository extends JpaRepository<DownloadProgress, UUID> {
    List<DownloadProgress> findByJob(Job job);
    List<DownloadProgress> findByVideo(Video video);

    long countByHasEndedTrue();
    long countByHasEndedFalse();
    long countByVideo(Video video);
    long countByVideoAndHasEndedTrue(Video video);

    Page<DownloadProgress> findByHasEndedTrue(Pageable pageable);
    Page<DownloadProgress> findByHasEndedFalse(Pageable pageable);

    @Override
    @NonNull
    default List<DownloadProgress> findAll() {
        return findByDeletedAtIsNull();
    }
    
    List<DownloadProgress> findByDeletedAtIsNull();
    Page<DownloadProgress> findByDeletedAtIsNull(Pageable pageable);
    Page<DownloadProgress> findByHasEndedTrueAndDeletedAtIsNull(Pageable pageable);
    Page<DownloadProgress> findByHasEndedFalseAndDeletedAtIsNull(Pageable pageable);
    
    long countByDeletedAtIsNull();
    long countByHasEndedTrueAndDeletedAtIsNull();
    long countByHasEndedFalseAndDeletedAtIsNull();
}
