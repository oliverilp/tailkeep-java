package org.tailkeep.domain.download.model;

import org.junit.jupiter.api.Test;
import org.tailkeep.domain.shared.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JobTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2025, 3, 1, 10, 0);

    private final UUID jobId = UUID.randomUUID();
    private final UUID videoId = UUID.randomUUID();
    private final Job job = Job.builder().id(jobId).inputUrl("https://example.com/watch").build();

    private ProgressUpdate update(boolean hasEnded, double progress) {
        return new ProgressUpdate("abc", jobId, "download", hasEnded, progress, "10MiB", "1MiB/s", "00:05");
    }

    @Test
    void withVideo_ShouldAttachTheVideo() {
        assertThat(job.withVideo(videoId).videoId()).isEqualTo(videoId);
    }

    @Test
    void reportProgress_FirstTime_ShouldStartTheProgressOfTheVideo() {
        DownloadProgress progress = job.withVideo(videoId).reportProgress(update(false, 12.5)).progress();

        assertThat(progress.jobId()).isEqualTo(jobId);
        assertThat(progress.videoId()).isEqualTo(videoId);
        assertThat(progress.status()).isEqualTo("download");
        assertThat(progress.progress()).isEqualTo(12.5);
        assertThat(progress.size()).isEqualTo("10MiB");
        assertThat(progress.speed()).isEqualTo("1MiB/s");
        assertThat(progress.eta()).isEqualTo("00:05");
        assertThat(progress.hasEnded()).isFalse();
    }

    @Test
    void reportProgress_AgainOverwritesTheValuesAndKeepsTheRest() {
        Job started = job.withVideo(videoId).reportProgress(update(false, 12.5));

        DownloadProgress progress = started.reportProgress(update(false, 60)).progress();

        assertThat(progress.progress()).isEqualTo(60);
        assertThat(progress.videoId()).isEqualTo(videoId);
    }

    @Test
    void reportProgress_WithoutAVideo_ShouldFail() {
        assertThatThrownBy(() -> job.reportProgress(update(false, 1))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void complete_ShouldFinishTheDownloadWithTheReportedSize() {
        Job started = job.withVideo(videoId).reportProgress(update(false, 90));

        DownloadProgress progress = started.complete("98MiB", NOW).progress();

        assertThat(progress.status()).isEqualTo("done");
        assertThat(progress.progress()).isEqualTo(100.0);
        assertThat(progress.size()).isEqualTo("98MiB");
        assertThat(progress.speed()).isEqualTo("0B/s");
        assertThat(progress.eta()).isEqualTo("00:00");
        assertThat(progress.hasEnded()).isTrue();
        assertThat(progress.completedAt()).isEqualTo(NOW);
    }

    @Test
    void complete_WhenAlreadyComplete_ShouldKeepTheTimeItFinished() {
        Job done = job.withVideo(videoId).reportProgress(update(false, 90)).complete("98MiB", NOW);

        Job again = done.complete("98MiB", NOW.plusHours(1));

        assertThat(again).isEqualTo(done);
    }

    @Test
    void needsNoDownload_ShouldBeTrueOnlyForAnEndedOrDeletedDownload() {
        Job started = job.withVideo(videoId);

        assertThat(started.needsNoDownload()).isFalse();
        assertThat(started.reportProgress(update(false, 5)).needsNoDownload()).isFalse();
        assertThat(started.reportProgress(update(false, 5)).complete("1MiB", NOW).needsNoDownload()).isTrue();
        assertThat(started.reportProgress(update(false, 5)).softDelete(NOW).needsNoDownload()).isTrue();
    }

    @Test
    void complete_WithoutProgress_ShouldFail() {
        assertThatThrownBy(() -> job.complete("1MiB", NOW))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Download progress not found with identifier: " + jobId);
    }

    @Test
    void softDelete_ShouldMarkTheProgressDeleted() {
        Job started = job.withVideo(videoId).reportProgress(update(false, 5));

        DownloadProgress progress = started.softDelete(NOW).progress();

        assertThat(progress.deletedAt()).isEqualTo(NOW);
        assertThat(progress.isDeleted()).isTrue();
        assertThat(started.progress().isDeleted()).isFalse();
    }

    @Test
    void softDelete_WithoutProgress_ShouldFail() {
        assertThatThrownBy(() -> job.softDelete(NOW)).isInstanceOf(ResourceNotFoundException.class);
    }
}
