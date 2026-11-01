package org.tailkeep.worker.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tailkeep.api.message.DownloadProgressMessage;
import org.tailkeep.api.message.Metadata;
import org.tailkeep.domain.download.DownloadRequest;
import org.tailkeep.domain.download.ProgressUpdate;
import org.tailkeep.domain.download.VideoMetadata;
import org.tailkeep.worker.download.YtDlpDownloadService;
import org.tailkeep.worker.metadata.MetadataFetcher;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class YtDlpAdaptersTest {

    @Mock
    private MetadataFetcher metadataFetcher;

    @Mock
    private YtDlpDownloadService downloadService;

    @Test
    void metadataProvider_ShouldReturnTheFetchedMetadata() {
        Metadata metadata = new Metadata("id", "https://youtube.com/watch?v=id", "Title", "Uploader", "UC1",
                "https://youtube.com/c/uc1", "10:00", 600.0, "https://img/id.jpg", "Description", 1000L, 100L, "id.mp4");
        when(metadataFetcher.fetch("https://youtube.com/watch?v=id"))
                .thenReturn(CompletableFuture.completedFuture(metadata));

        VideoMetadata result = new YtDlpVideoMetadataProvider(metadataFetcher).fetch("https://youtube.com/watch?v=id");

        assertThat(result).isEqualTo(new VideoMetadata("id", "https://youtube.com/watch?v=id", "Title", "Uploader",
                "UC1", "https://youtube.com/c/uc1", "10:00", 600.0, "https://img/id.jpg", "Description", 1000L, 100L,
                "id.mp4"));
    }

    @Test
    void metadataProvider_WhenTheFetchFails_ShouldThrowTheOriginalException() {
        when(metadataFetcher.fetch(any())).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("boom")));

        assertThatThrownBy(() -> new YtDlpVideoMetadataProvider(metadataFetcher).fetch("https://x.com/v"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");
    }

    @Test
    @SuppressWarnings("unchecked")
    void downloader_ShouldPassProgressOnAndReturnTheLastUpdate() {
        UUID jobId = UUID.randomUUID();
        DownloadProgressMessage midway = new DownloadProgressMessage("id", jobId, "download", false, 50, "10MiB", "1MiB/s", "00:05");
        DownloadProgressMessage last = new DownloadProgressMessage("id", jobId, "download", true, 100, "10MiB", "1MiB/s", "00:00");
        when(downloadService.processDownload(eq(jobId), eq("id"), eq("https://x.com/v"), eq("id.mp4"), any()))
                .thenAnswer(invocation -> {
                    ((Consumer<DownloadProgressMessage>) invocation.getArgument(4)).accept(midway);
                    return CompletableFuture.completedFuture(last);
                });
        List<ProgressUpdate> updates = new ArrayList<>();

        ProgressUpdate result = new YtDlpVideoDownloader(downloadService)
                .download(new DownloadRequest(jobId, "id", "https://x.com/v", "id.mp4"), updates::add);

        assertThat(updates).containsExactly(new ProgressUpdate("id", jobId, "download", false, 50, "10MiB", "1MiB/s", "00:05"));
        assertThat(result).isEqualTo(new ProgressUpdate("id", jobId, "download", true, 100, "10MiB", "1MiB/s", "00:00"));
    }
}
