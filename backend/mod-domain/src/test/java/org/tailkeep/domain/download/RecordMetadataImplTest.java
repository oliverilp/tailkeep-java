package org.tailkeep.domain.download;

import org.junit.jupiter.api.Test;
import org.tailkeep.domain.shared.ResourceNotFoundException;
import org.tailkeep.domain.video.FakeChannels;
import org.tailkeep.domain.video.FakeVideos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecordMetadataImplTest {
    private final FakeJobs jobs = new FakeJobs();
    private final FakeChannels channels = new FakeChannels();
    private final FakeVideos videos = new FakeVideos();
    private final FakeRunInTransaction transaction = new FakeRunInTransaction();
    private final List<DownloadRequest> published = new ArrayList<>();

    private final RecordMetadataImpl recordMetadata = new RecordMetadataImpl(
            jobs, jobs, channels, channels, videos, videos, transaction, published::add);

    private Job newJob() {
        return jobs.save(Job.builder().inputUrl("https://www.youtube.com/watch?v=abc").build());
    }

    @Test
    void execute_ShouldSaveTheChannelAndVideoAttachItToTheJobAndRequestTheDownload() {
        Job job = newJob();

        recordMetadata.execute(new MetadataResult(job.id(), FetchMetadataImplTest.metadata("abc")));

        var video = videos.byYoutubeId("abc").orElseThrow();
        var channel = channels.byYoutubeId("UC1").orElseThrow();
        assertThat(channel.name()).isEqualTo("Uploader");
        assertThat(video.channelId()).isEqualTo(channel.id());
        assertThat(video.title()).isEqualTo("A title");
        assertThat(video.viewCount()).isEqualTo(1000);
        assertThat(video.filename()).isEqualTo("abc.mp4");
        assertThat(jobs.byId(job.id()).orElseThrow().videoId()).isEqualTo(video.id());
        assertThat(published).containsExactly(
                new DownloadRequest(job.id(), "abc", "https://www.youtube.com/watch?v=abc", "abc.mp4"));
        assertThat(transaction.runs).isEqualTo(1);
    }

    @Test
    void execute_ForAKnownVideoAndChannel_ShouldUpdateTheVideoWithoutDuplicates() {
        Job first = newJob();
        recordMetadata.execute(new MetadataResult(first.id(), FetchMetadataImplTest.metadata("abc")));
        Job second = newJob();
        VideoMetadata changed = new VideoMetadata("abc", "https://www.youtube.com/watch?v=abc", "New title",
                "Renamed", "UC1", "https://www.youtube.com/channel/UC1", "10:00", 600.0, "https://img/abc.jpg",
                "A description", 2000, 100, "abc.mp4");

        recordMetadata.execute(new MetadataResult(second.id(), changed));

        assertThat(videos.newestFirst()).hasSize(1);
        assertThat(videos.byYoutubeId("abc").orElseThrow().title()).isEqualTo("New title");
        assertThat(videos.byYoutubeId("abc").orElseThrow().viewCount()).isEqualTo(2000);
        assertThat(channels.byIds(List.of(videos.byYoutubeId("abc").orElseThrow().channelId()))).hasSize(1);
        assertThat(channels.byYoutubeId("UC1").orElseThrow().name()).isEqualTo("Uploader");
        assertThat(jobs.byId(second.id()).orElseThrow().videoId()).isEqualTo(videos.byYoutubeId("abc").orElseThrow().id());
        assertThat(published).hasSize(2);
    }

    @Test
    void execute_ForAnUnknownJob_ShouldSaveAndPublishNothing() {
        UUID unknown = UUID.randomUUID();

        assertThatThrownBy(() -> recordMetadata.execute(new MetadataResult(unknown, FetchMetadataImplTest.metadata("abc"))))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Job not found with identifier: " + unknown);

        assertThat(videos.newestFirst()).isEmpty();
        assertThat(published).isEmpty();
    }

    @Test
    void execute_ShouldPublishOnlyAfterTheTransactionHasEnded() {
        List<String> order = new ArrayList<>();
        FakeRunInTransaction recording = new FakeRunInTransaction() {
            @Override
            public <T> T execute(java.util.function.Supplier<T> work) {
                T result = super.execute(work);
                order.add("committed");
                return result;
            }
        };
        Job job = newJob();
        RecordMetadataImpl impl = new RecordMetadataImpl(
                jobs, jobs, channels, channels, videos, videos, recording, request -> order.add("published"));

        impl.execute(new MetadataResult(job.id(), FetchMetadataImplTest.metadata("abc")));

        assertThat(order).containsExactly("committed", "published");
    }
}
