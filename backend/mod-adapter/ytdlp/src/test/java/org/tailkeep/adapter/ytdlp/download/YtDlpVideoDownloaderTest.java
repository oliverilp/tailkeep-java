package org.tailkeep.adapter.ytdlp.download;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tailkeep.adapter.ytdlp.config.YtDlpProperties;
import org.tailkeep.adapter.ytdlp.process.YtDlpRunner;
import org.tailkeep.domain.download.model.DownloadRequest;
import org.tailkeep.domain.download.model.ProgressUpdate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class YtDlpVideoDownloaderTest {
    private static final List<String> OUTPUT = List.of(
            "[download] Destination: video.mp4",
            "[download]  12.3% of ~  50.00MiB at    2.00MiB/s ETA 00:25 (frag 1/10)",
            "[download] 100% of   50.00MiB in 00:15 at 3.30MiB/s");

    @TempDir
    Path mediaDir;

    private final UUID jobId = UUID.randomUUID();
    private final DownloadRequest request = new DownloadRequest(jobId, "abc", "https://example.com/v", "video.mp4");

    @Test
    void download_ReportsEachLineAndEndsWithTheSizeOnDisk() throws IOException {
        Files.write(mediaDir.resolve("video.mp4"), new byte[3072]);
        List<ProgressUpdate> updates = new ArrayList<>();

        ProgressUpdate last = downloader().download(request, updates::add);

        assertThat(updates).hasSize(OUTPUT.size());
        assertThat(updates.get(1).progress()).isEqualTo(12.3);
        assertThat(updates).noneMatch(ProgressUpdate::hasEnded);
        assertThat(last).isEqualTo(new ProgressUpdate("abc", jobId, "download", true, 100.0, "3.00KiB", "3.30MiB/s", "00:25"));
    }

    @Test
    void download_WhenTheFileIsMissing_KeepsTheParsedSize() {
        ProgressUpdate last = downloader().download(request, update -> {});

        assertThat(last.hasEnded()).isTrue();
        assertThat(last.size()).isEqualTo("50.00MiB");
    }

    private YtDlpVideoDownloader downloader() {
        YtDlpProperties properties = new YtDlpProperties();
        properties.setPath(mediaDir.toString());
        YtDlpRunner runner = new YtDlpRunner(null, properties) {
            @Override
            public void run(List<String> args, Consumer<String> onStdoutLine) {
                assertThat(args).containsExactly("https://example.com/v");
                OUTPUT.forEach(onStdoutLine);
            }
        };
        return new YtDlpVideoDownloader(runner, properties);
    }
}
