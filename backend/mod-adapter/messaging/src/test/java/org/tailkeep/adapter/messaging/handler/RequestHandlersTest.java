package org.tailkeep.adapter.messaging.handler;

import org.junit.jupiter.api.Test;
import org.tailkeep.adapter.messaging.contract.DownloadBroadcastMessage;
import org.tailkeep.adapter.messaging.contract.DownloadProgressMessage;
import org.tailkeep.adapter.messaging.contract.DownloadRequestMessage;
import org.tailkeep.adapter.messaging.contract.Metadata;
import org.tailkeep.adapter.messaging.contract.MetadataRequestMessage;
import org.tailkeep.adapter.messaging.contract.MetadataResultMessage;
import org.tailkeep.domain.download.model.DownloadRequest;
import org.tailkeep.domain.download.model.MetadataRequest;
import org.tailkeep.domain.download.model.MetadataResult;
import org.tailkeep.domain.download.model.ProgressUpdate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestHandlersTest {
    private final UUID jobId = UUID.randomUUID();

    @Test
    void metadataRequestHandler_ShouldFetchTheMetadataForTheRequest() {
        List<MetadataRequest> fetched = new ArrayList<>();

        new MetadataRequestHandler(fetched::add)
                .handle(new MetadataRequestMessage(jobId, "https://youtube.com/watch?v=test"));

        assertThat(fetched).containsExactly(new MetadataRequest(jobId, "https://youtube.com/watch?v=test"));
    }

    @Test
    void metadataRequestHandler_WhenTheFetchFails_ShouldLetTheExceptionOutSoTheMessageIsNotAcknowledged() {
        MetadataRequestHandler handler = new MetadataRequestHandler(request -> {
            throw new IllegalStateException("fetch failed");
        });

        assertThatThrownBy(() -> handler.handle(new MetadataRequestMessage(jobId, "https://youtube.com/watch?v=test")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void metadataResultHandler_ShouldRecordTheMetadata() {
        List<MetadataResult> recorded = new ArrayList<>();
        Metadata metadata = new Metadata("abc", "https://youtube.com/watch?v=abc", "Title", "Uploader", "UC1",
                "https://youtube.com/channel/UC1", "3:25", 205.0, "https://img/abc.jpg", "About", 10L, 2L, "abc.mp4");

        new MetadataResultHandler(recorded::add).handle(new MetadataResultMessage(jobId, metadata));

        assertThat(recorded).hasSize(1);
        assertThat(recorded.get(0).jobId()).isEqualTo(jobId);
        assertThat(recorded.get(0).metadata().youtubeId()).isEqualTo("abc");
        assertThat(recorded.get(0).metadata().viewCount()).isEqualTo(10L);
    }

    @Test
    void downloadRequestHandler_ShouldRunTheDownloadWithTheYouTubeIdOfTheVideo() {
        List<DownloadRequest> started = new ArrayList<>();

        new DownloadRequestHandler(started::add)
                .handle(new DownloadRequestMessage(jobId, "video123", "https://youtube.com/watch?v=test", "test.mp4"));

        assertThat(started)
                .containsExactly(new DownloadRequest(jobId, "video123", "https://youtube.com/watch?v=test", "test.mp4"));
    }

    @Test
    void downloadRequestHandler_WhenTheDownloadFails_ShouldLetTheExceptionOutSoTheMessageIsNotAcknowledged() {
        DownloadRequestHandler handler = new DownloadRequestHandler(request -> {
            throw new IllegalStateException("download failed");
        });

        assertThatThrownBy(() -> handler.handle(
                new DownloadRequestMessage(jobId, "video123", "https://youtube.com/watch?v=test", "test.mp4")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void downloadProgressHandler_ShouldRecordTheProgress() {
        List<ProgressUpdate> recorded = new ArrayList<>();

        new DownloadProgressHandler(update -> {
            recorded.add(update);
            return null;
        }).handle(new DownloadProgressMessage("abc", jobId, "download", false, 42.5, "10MiB", "1MiB/s", "00:05"));

        assertThat(recorded)
                .containsExactly(new ProgressUpdate("abc", jobId, "download", false, 42.5, "10MiB", "1MiB/s", "00:05"));
    }

    @Test
    void downloadBroadcastHandler_ShouldPushTheDownloadOfTheJob() {
        List<UUID> pushed = new ArrayList<>();

        new DownloadBroadcastHandler(pushed::add).handle(new DownloadBroadcastMessage(jobId));

        assertThat(pushed).containsExactly(jobId);
    }
}
