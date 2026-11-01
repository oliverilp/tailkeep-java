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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The read and delete use cases, which share their fakes. */
class DownloadQueriesTest {
    private final FakeJobs jobs = new FakeJobs();
    private final FakeChannels channels = new FakeChannels();
    private final FakeVideos videos = new FakeVideos();
    private final Clock clock = Clock.fixed(Instant.parse("2025-03-01T10:00:00Z"), ZoneOffset.UTC);

    private final GetDownloadImpl getDownload = new GetDownloadImpl(jobs, videos, channels);
    private final ListDownloadsImpl listDownloads = new ListDownloadsImpl(jobs, videos, channels);
    private final GetDownloadsDashboardImpl dashboard = new GetDownloadsDashboardImpl(jobs, jobs, videos, channels);
    private final DeleteDownloadImpl deleteDownload = new DeleteDownloadImpl(jobs, jobs, clock);

    private final Channel channel = channels.add("Uploader");
    private final Video video = videos.add(channel.id(), "abc", LocalDateTime.of(2025, 1, 1, 12, 0));

    private Job downloading() {
        return jobs.addDownloading(video.id());
    }

    private Job finished() {
        Job job = downloading();
        return jobs.save(job.complete("1MiB", LocalDateTime.of(2025, 2, 1, 0, 0)));
    }

    @Test
    void getDownload_ShouldReturnTheProgressWithItsVideoAndChannel() {
        Job job = downloading();

        DownloadDetails details = getDownload.execute(job.id());

        assertThat(details.download().jobId()).isEqualTo(job.id());
        assertThat(details.video()).isEqualTo(video);
        assertThat(details.channel()).isEqualTo(channel);
    }

    @Test
    void getDownload_ShouldAlsoReturnADeletedDownload() {
        Job job = downloading();
        deleteDownload.execute(job.id());

        assertThat(getDownload.execute(job.id()).download().isDeleted()).isTrue();
    }

    @Test
    void getDownload_ForAnUnknownJobOrOneWithoutProgress_ShouldFail() {
        UUID unknown = UUID.randomUUID();
        Job withoutProgress = jobs.save(Job.builder().inputUrl("https://x.com/v").build());

        assertThatThrownBy(() -> getDownload.execute(unknown))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Download progress not found with identifier: " + unknown);
        assertThatThrownBy(() -> getDownload.execute(withoutProgress.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listDownloads_ShouldReturnNewestFirst() {
        Job first = downloading();
        Job second = finished();
        Job third = downloading();

        assertThat(listDownloads.execute()).extracting(details -> details.download().jobId())
                .containsExactly(third.id(), second.id(), first.id());
    }

    @Test
    void listDownloads_WithoutDownloads_ShouldBeEmpty() {
        assertThat(listDownloads.execute()).isEmpty();
    }

    @Test
    void dashboard_ShouldCountByStateAndSkipDeleted() {
        downloading();
        finished();
        Job deleted = finished();
        deleteDownload.execute(deleted.id());
        jobs.save(Job.builder().inputUrl("https://x.com/v").build());

        DownloadsDashboard result = dashboard.execute(DownloadFilter.ALL, 1, 10);

        assertThat(result.queueInfo()).isEqualTo(new QueueInfo(1, 1, 1));
        assertThat(result.downloads().totalItems()).isEqualTo(2);
    }

    @Test
    void dashboard_ShouldPageFromOneNewestFirst() {
        Job first = downloading();
        Job second = downloading();
        Job third = downloading();

        DownloadsDashboard firstPage = dashboard.execute(DownloadFilter.ALL, 1, 2);
        DownloadsDashboard secondPage = dashboard.execute(DownloadFilter.ALL, 2, 2);

        assertThat(firstPage.downloads().items()).extracting(details -> details.download().jobId())
                .containsExactly(third.id(), second.id());
        assertThat(firstPage.downloads().totalPages()).isEqualTo(2);
        assertThat(firstPage.downloads().hasNext()).isTrue();
        assertThat(secondPage.downloads().items()).extracting(details -> details.download().jobId())
                .containsExactly(first.id());
        assertThat(secondPage.downloads().hasPrevious()).isTrue();
    }

    @Test
    void dashboard_ShouldFilterByState() {
        Job active = downloading();
        Job done = finished();

        assertThat(ids(dashboard.execute(DownloadFilter.ACTIVE, 1, 10))).containsExactly(active.id());
        assertThat(ids(dashboard.execute(DownloadFilter.DONE, 1, 10))).containsExactly(done.id());
    }

    @Test
    void deleteDownload_ShouldHideItButKeepIt() {
        Job keep = downloading();
        Job delete = finished();

        deleteDownload.execute(delete.id());

        assertThat(jobs.byJobId(delete.id()).orElseThrow().deletedAt()).isEqualTo(LocalDateTime.of(2025, 3, 1, 10, 0));
        assertThat(ids(dashboard.execute(DownloadFilter.ALL, 1, 10))).containsExactly(keep.id());
        assertThat(jobs.size()).isEqualTo(2);
    }

    @Test
    void deleteDownload_ForAnUnknownJob_ShouldFail() {
        UUID unknown = UUID.randomUUID();

        assertThatThrownBy(() -> deleteDownload.execute(unknown))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Download progress not found with identifier: " + unknown);
    }

    private static List<UUID> ids(DownloadsDashboard dashboard) {
        return dashboard.downloads().items().stream().map(details -> details.download().jobId()).toList();
    }
}
