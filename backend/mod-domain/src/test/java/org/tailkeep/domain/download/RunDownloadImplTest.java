package org.tailkeep.domain.download;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RunDownloadImplTest {
    private final UUID jobId = UUID.randomUUID();
    private final DownloadRequest request = new DownloadRequest(jobId, "abc", "https://www.youtube.com/watch?v=abc", "abc.mp4");
    private final List<ProgressUpdate> published = new ArrayList<>();

    private ProgressUpdate update(boolean hasEnded, double progress) {
        return new ProgressUpdate("abc", jobId, hasEnded ? "finished" : "download", hasEnded, progress, "10MiB", "1MiB/s", "00:05");
    }

    @Test
    void execute_ShouldPublishEveryUpdateInOrderAndTheLastOneAfterThem() {
        RunDownloadImpl runDownload = new RunDownloadImpl((downloadRequest, onProgress) -> {
            assertThat(downloadRequest).isEqualTo(request);
            onProgress.accept(update(false, 10));
            onProgress.accept(update(false, 60));
            return update(true, 100);
        }, published::add);

        runDownload.execute(request);

        assertThat(published).containsExactly(update(false, 10), update(false, 60), update(true, 100));
    }

    @Test
    void execute_WhenTheDownloadFails_ShouldNotPublishAnEndedUpdate() {
        RunDownloadImpl runDownload = new RunDownloadImpl((downloadRequest, onProgress) -> {
            onProgress.accept(update(false, 10));
            throw new IllegalStateException("download failed");
        }, published::add);

        assertThatThrownBy(() -> runDownload.execute(request)).isInstanceOf(IllegalStateException.class);

        assertThat(published).containsExactly(update(false, 10));
    }
}
