package org.tailkeep.domain.download;

import org.junit.jupiter.api.Test;
import org.tailkeep.domain.video.FakeChannels;
import org.tailkeep.domain.video.FakeVideos;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the six steps of the pipeline in order. Each published message is handed to the next use case, as a
 * message handler would do it, so the order the domain defines can be read here.
 */
class DownloadPipelineTest {
    private final FakeJobs jobs = new FakeJobs();
    private final FakeChannels channels = new FakeChannels();
    private final FakeVideos videos = new FakeVideos();
    private final List<String> steps = new ArrayList<>();
    private final List<DownloadDetails> broadcast = new ArrayList<>();

    private final Clock clock = Clock.fixed(Instant.parse("2025-03-01T10:00:00Z"), ZoneOffset.UTC);

    // Step 6 is called by step 5; the other publishers hand their message to the use case of the next step
    private final RecordDownloadProgress step5 = new RecordDownloadProgressImpl(jobs, jobs, videos, channels,
            details -> {
                steps.add("6 broadcast " + details.download().status());
                broadcast.add(details);
            }, clock);
    private final RunDownload step4 = new RunDownloadImpl(
            (request, onProgress) -> {
                onProgress.accept(new ProgressUpdate(request.youtubeId(), request.jobId(), "download", false, 50, "10MiB", "1MiB/s", "00:05"));
                return new ProgressUpdate(request.youtubeId(), request.jobId(), "finished", true, 100, "10MiB", "0B/s", "00:00");
            },
            update -> {
                steps.add("4 progress " + update.status());
                step5.execute(update);
            });
    private final RecordMetadata step3 = new RecordMetadataImpl(jobs, jobs, channels, channels, videos, videos,
            new FakeRunInTransaction(),
            request -> {
                steps.add("3 download request");
                step4.execute(request);
            });
    private final FetchMetadata step2 = new FetchMetadataImpl(
            url -> FetchMetadataImplTest.metadata("abc"),
            result -> {
                steps.add("2 metadata result");
                step3.execute(result);
            });
    private final StartDownload step1 = new StartDownloadImpl(url -> 200, jobs,
            request -> {
                steps.add("1 metadata request");
                step2.execute(request);
            });

    @Test
    void aDownloadGoesThroughAllSixStepsInOrder() {
        step1.execute("https://www.youtube.com/watch?v=abc&list=xyz");

        assertThat(steps).containsExactly(
                "1 metadata request",
                "2 metadata result",
                "3 download request",
                "4 progress download",
                "6 broadcast download",
                "4 progress finished",
                "6 broadcast done");
        DownloadProgress done = broadcast.get(1).download();
        assertThat(done.hasEnded()).isTrue();
        assertThat(done.progress()).isEqualTo(100.0);
        assertThat(broadcast.get(1).video().youtubeId()).isEqualTo("abc");
        assertThat(jobs.size()).isEqualTo(1);
    }
}
