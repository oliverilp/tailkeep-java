package org.tailkeep.adapter.database.download;

import org.tailkeep.adapter.database.video.VideoEntity;
import org.tailkeep.domain.download.model.DownloadProgress;
import org.tailkeep.domain.download.model.Job;

public final class JobEntityMapper {
    private JobEntityMapper() {
    }

    public static Job toDomain(JobEntity entity) {
        return Job.builder()
                .id(entity.getId())
                .inputUrl(entity.getInputUrl())
                .videoId(entity.getVideo() == null ? null : entity.getVideo().getId())
                .progress(entity.getDownloadProgress() == null ? null : toDomain(entity.getDownloadProgress()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static DownloadProgress toDomain(DownloadProgressEntity entity) {
        return DownloadProgress.builder()
                .jobId(entity.getId())
                .videoId(entity.getVideo().getId())
                .status(entity.getStatus())
                .hasEnded(entity.isHasEnded())
                .progress(entity.getProgress())
                .size(entity.getSize())
                .speed(entity.getSpeed())
                .eta(entity.getEta())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .completedAt(entity.getCompletedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }

    /**
     * Copies the job onto its entity, which is a new one or the managed one, and creates the progress entity
     * when the job has progress and the entity not yet. The video is passed in because a domain job only knows
     * its ID. The timestamps the database keeps are not copied.
     */
    public static void copyTo(Job job, JobEntity entity, VideoEntity video) {
        entity.setInputUrl(job.inputUrl());
        entity.setVideo(video);

        DownloadProgress progress = job.progress();
        if (progress == null) {
            return;
        }
        DownloadProgressEntity progressEntity = entity.getDownloadProgress();
        if (progressEntity == null) {
            progressEntity = new DownloadProgressEntity();
            progressEntity.setId(entity.getId());
            progressEntity.setJob(entity);
            entity.setDownloadProgress(progressEntity);
        }
        progressEntity.setVideo(video);
        progressEntity.setStatus(progress.status());
        progressEntity.setHasEnded(progress.hasEnded());
        progressEntity.setProgress(progress.progress());
        progressEntity.setSize(progress.size());
        progressEntity.setSpeed(progress.speed());
        progressEntity.setEta(progress.eta());
        progressEntity.setCompletedAt(progress.completedAt());
        progressEntity.setDeletedAt(progress.deletedAt());
    }
}
