package org.tailkeep.adapter.database.download;

import org.junit.jupiter.api.Test;
import org.tailkeep.adapter.database.video.VideoEntity;
import org.tailkeep.domain.download.DownloadProgress;
import org.tailkeep.domain.download.Job;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JobEntityMapperTest {
    private final UUID jobId = UUID.randomUUID();
    private final UUID videoId = UUID.randomUUID();
    private final VideoEntity video = VideoEntity.builder().id(videoId).build();
    private final DownloadProgress progress = DownloadProgress.builder()
            .jobId(jobId)
            .videoId(videoId)
            .status("download")
            .hasEnded(true)
            .progress(42.5)
            .size("10MiB")
            .speed("1MiB/s")
            .eta("00:05")
            .completedAt(LocalDateTime.of(2025, 3, 1, 10, 0))
            .deletedAt(LocalDateTime.of(2025, 3, 2, 10, 0))
            .build();

    @Test
    void copyTo_ShouldFillANewEntityIncludingItsProgress() {
        JobEntity entity = new JobEntity();
        entity.setId(jobId);

        JobEntityMapper.copyTo(
                Job.builder().id(jobId).inputUrl("https://x.com/v").videoId(videoId).progress(progress).build(),
                entity, video);

        assertThat(entity.getInputUrl()).isEqualTo("https://x.com/v");
        assertThat(entity.getVideo()).isSameAs(video);
        DownloadProgressEntity child = entity.getDownloadProgress();
        assertThat(child.getId()).isEqualTo(jobId);
        assertThat(child.getJob()).isSameAs(entity);
        assertThat(child.getVideo()).isSameAs(video);
        assertThat(child.getStatus()).isEqualTo("download");
        assertThat(child.isHasEnded()).isTrue();
        assertThat(child.getProgress()).isEqualTo(42.5);
        assertThat(child.getSize()).isEqualTo("10MiB");
        assertThat(child.getSpeed()).isEqualTo("1MiB/s");
        assertThat(child.getEta()).isEqualTo("00:05");
        assertThat(child.getCompletedAt()).isEqualTo(progress.completedAt());
        assertThat(child.getDeletedAt()).isEqualTo(progress.deletedAt());
    }

    @Test
    void copyTo_ShouldUpdateTheExistingProgressEntityInPlace() {
        JobEntity entity = new JobEntity();
        entity.setId(jobId);
        JobEntityMapper.copyTo(Job.builder().id(jobId).videoId(videoId).progress(progress).build(), entity, video);
        DownloadProgressEntity child = entity.getDownloadProgress();

        JobEntityMapper.copyTo(Job.builder().id(jobId).videoId(videoId)
                .progress(progress.toBuilder().progress(99.0).build()).build(), entity, video);

        assertThat(entity.getDownloadProgress()).isSameAs(child);
        assertThat(child.getProgress()).isEqualTo(99.0);
    }

    @Test
    void copyTo_WithoutProgress_ShouldLeaveItOut() {
        JobEntity entity = new JobEntity();

        JobEntityMapper.copyTo(Job.builder().inputUrl("https://x.com/v").build(), entity, null);

        assertThat(entity.getVideo()).isNull();
        assertThat(entity.getDownloadProgress()).isNull();
    }

    @Test
    void toDomain_ShouldMapTheJobWithItsProgress() {
        JobEntity entity = new JobEntity();
        entity.setId(jobId);
        entity.setInputUrl("https://x.com/v");
        entity.setCreatedAt(LocalDateTime.of(2025, 1, 1, 0, 0));
        entity.setUpdatedAt(LocalDateTime.of(2025, 1, 2, 0, 0));
        JobEntityMapper.copyTo(Job.builder().id(jobId).inputUrl("https://x.com/v").videoId(videoId)
                .progress(progress).build(), entity, video);
        entity.getDownloadProgress().setCreatedAt(LocalDateTime.of(2025, 1, 3, 0, 0));

        Job job = JobEntityMapper.toDomain(entity);

        assertThat(job.id()).isEqualTo(jobId);
        assertThat(job.videoId()).isEqualTo(videoId);
        assertThat(job.createdAt()).isEqualTo(LocalDateTime.of(2025, 1, 1, 0, 0));
        assertThat(job.progress()).isEqualTo(progress.toBuilder().createdAt(LocalDateTime.of(2025, 1, 3, 0, 0)).build());
    }

    @Test
    void toDomain_WithoutVideoOrProgress_ShouldLeaveThemNull() {
        JobEntity entity = new JobEntity();
        entity.setId(jobId);

        Job job = JobEntityMapper.toDomain(entity);

        assertThat(job.videoId()).isNull();
        assertThat(job.progress()).isNull();
    }
}
