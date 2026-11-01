package org.tailkeep.adapter.database.download;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.tailkeep.adapter.database.video.VideoEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface DownloadProgressRepository extends JpaRepository<DownloadProgressEntity, UUID> {
    List<DownloadProgressEntity> findByJob(JobEntity job);
    List<DownloadProgressEntity> findByVideo(VideoEntity video);

    long countByHasEndedTrue();
    long countByHasEndedFalse();
    long countByVideoId(UUID videoId);
    long countByVideoIdAndHasEndedTrue(UUID videoId);

    Page<DownloadProgressEntity> findByHasEndedTrue(Pageable pageable);
    Page<DownloadProgressEntity> findByHasEndedFalse(Pageable pageable);

    @Override
    @NonNull
    default List<DownloadProgressEntity> findAll() {
        return findByDeletedAtIsNull();
    }
    
    List<DownloadProgressEntity> findByDeletedAtIsNull();
    Page<DownloadProgressEntity> findByDeletedAtIsNull(Pageable pageable);
    Page<DownloadProgressEntity> findByHasEndedTrueAndDeletedAtIsNull(Pageable pageable);
    Page<DownloadProgressEntity> findByHasEndedFalseAndDeletedAtIsNull(Pageable pageable);
    
    long countByDeletedAtIsNull();
    long countByHasEndedTrueAndDeletedAtIsNull();
    long countByHasEndedFalseAndDeletedAtIsNull();
}
