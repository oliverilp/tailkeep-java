package org.tailkeep.domain.download;

import org.junit.jupiter.api.Test;
import org.tailkeep.domain.channel.Channel;
import org.tailkeep.domain.shared.ResourceNotFoundException;
import org.tailkeep.domain.video.FakeChannels;
import org.tailkeep.domain.video.FakeVideos;
import org.tailkeep.domain.video.Video;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecordDownloadProgressImplTest {
    private static final Instant NOW = Instant.parse("2025-03-01T10:00:00Z");

    private final FakeJobs jobs = new FakeJobs();
    private final FakeChannels channels = new FakeChannels();
    private final FakeVideos videos = new FakeVideos();
    private final List<DownloadDetails> broadcast = new ArrayList<>();

    private final RecordDownloadProgressImpl recordProgress = new RecordDownloadProgressImpl(
            jobs, jobs, videos, channels, broadcast::add, Clock.fixed(NOW, ZoneOffset.UTC));

    private final Channel channel = channels.add("Uploader");
    private final Video video = videos.add(channel.id(), "abc", LocalDateTime.of(2025, 1, 1, 12, 0));
    private final Job job = jobs.save(Job.builder().inputUrl("https://x.com/v").videoId(video.id()).build());

    private ProgressUpdate update(UUID jobId, boolean hasEnded, double progress) {
        return new ProgressUpdate("abc", jobId, "download", hasEnded, progress, "100MiB", "2MiB/s", "00:30");
    }

    @Test
    void execute_ShouldCreateThenUpdateTheProgressAndBroadcastEach() {
        recordProgress.execute(update(job.id(), false, 12.5));
        DownloadDetails second = recordProgress.execute(update(job.id(), false, 60));

        assertThat(jobs.byJobId(job.id()).orElseThrow().progress()).isEqualTo(60);
        assertThat(second.video()).isEqualTo(video);
        assertThat(second.channel()).isEqualTo(channel);
        assertThat(broadcast).hasSize(2);
        assertThat(broadcast.get(0).download().progress()).isEqualTo(12.5);
        assertThat(broadcast.get(1)).isEqualTo(second);
    }

    @Test
    void execute_WithAnEndedUpdate_ShouldCompleteTheDownload() {
        recordProgress.execute(update(job.id(), false, 90));

        recordProgress.execute(new ProgressUpdate("abc", job.id(), "finished", true, 99, "98MiB", "2MiB/s", "00:01"));

        DownloadProgress done = jobs.byJobId(job.id()).orElseThrow();
        assertThat(done.status()).isEqualTo("done");
        assertThat(done.progress()).isEqualTo(100.0);
        assertThat(done.size()).isEqualTo("98MiB");
        assertThat(done.hasEnded()).isTrue();
        assertThat(done.completedAt()).isEqualTo(LocalDateTime.of(2025, 3, 1, 10, 0));
        assertThat(broadcast).hasSize(2);
    }

    @Test
    void execute_ForAnUnknownJob_ShouldFailAndBroadcastNothing() {
        UUID unknown = UUID.randomUUID();

        assertThatThrownBy(() -> recordProgress.execute(update(unknown, false, 5)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Job not found with identifier: " + unknown);
        assertThatThrownBy(() -> recordProgress.execute(update(unknown, true, 5)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Download progress not found with identifier: " + unknown);

        assertThat(broadcast).isEmpty();
    }

    @Test
    void execute_WithAnEndedUpdateBeforeAnyProgress_ShouldFail() {
        assertThatThrownBy(() -> recordProgress.execute(update(job.id(), true, 5)))
                .isInstanceOf(ResourceNotFoundException.class);

        assertThat(broadcast).isEmpty();
        assertThat(jobs.byJobId(job.id())).isEmpty();
    }
}
