package org.tailkeep.adapter.database.download;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DownloadProgressRepository extends JpaRepository<DownloadProgressEntity, UUID> {
    long countByVideoId(UUID videoId);
    long countByVideoIdAndHasEndedTrue(UUID videoId);

    Page<DownloadProgressEntity> findByDeletedAtIsNull(Pageable pageable);
    Page<DownloadProgressEntity> findByHasEndedTrueAndDeletedAtIsNull(Pageable pageable);
    Page<DownloadProgressEntity> findByHasEndedFalseAndDeletedAtIsNull(Pageable pageable);

    long countByHasEndedTrueAndDeletedAtIsNull();
    long countByHasEndedFalseAndDeletedAtIsNull();
}
