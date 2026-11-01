package org.tailkeep.adapter.ytdlp.download;

import org.junit.jupiter.api.Test;
import org.tailkeep.domain.download.ProgressUpdate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StdoutProgressParserTest {
    private final UUID jobId = UUID.randomUUID();
    private final StdoutProgressParser parser = new StdoutProgressParser("abc", jobId);

    @Test
    void onLine_ReadsAProgressLine() {
        ProgressUpdate update = parser.onLine("[download]  12.3% of ~  50.00MiB at    2.00MiB/s ETA 00:25 (frag 1/10)");

        assertThat(update).isEqualTo(new ProgressUpdate("abc", jobId, "download", false, 12.3, "50.00MiB", "2.00MiB/s", "00:25"));
    }

    @Test
    void onLine_KeepsEarlierValuesWhenALineHasNoneOfThem() {
        parser.onLine("[download]  12.3% of ~  50.00MiB at    2.00MiB/s ETA 00:25 (frag 1/10)");

        ProgressUpdate update = parser.onLine("[download] 100% of   50.00MiB in 00:15 at 3.30MiB/s");

        assertThat(update.progress()).isEqualTo(100.0);
        assertThat(update.speed()).isEqualTo("3.30MiB/s");
        assertThat(update.eta()).isEqualTo("00:25");
    }

    @Test
    void onLine_TakesTheCategoryAsStatus() {
        assertThat(parser.onLine("[youtube] abc: Downloading webpage").status()).isEqualTo("youtube");
        assertThat(parser.onLine("[info] abc: Downloading 1 format(s)").status()).isEqualTo("info");
    }

    @Test
    void onLine_IgnoresASizeThatJumpsAfterTheStart() {
        parser.onLine("[download]   1.0% of ~  50.00MiB at    2.00MiB/s ETA 00:25");
        parser.onLine("[download]  50.0% of ~  60.00MiB at    2.00MiB/s ETA 00:25");

        ProgressUpdate update = parser.onLine("[download]  60.0% of ~ 500.00MiB at    2.00MiB/s ETA 00:25");

        assertThat(update.size()).isEqualTo("60.00MiB");
    }

    @Test
    void onLine_AcceptsAnySizeAtTheStart() {
        parser.onLine("[download]   1.0% of ~  50.00MiB at    2.00MiB/s ETA 00:25");

        ProgressUpdate update = parser.onLine("[download]   2.0% of ~ 500.00MiB at    2.00MiB/s ETA 00:25");

        assertThat(update.size()).isEqualTo("500.00MiB");
    }

    @Test
    void finish_EndsTheDownloadWithTheSizeOnDisk() {
        parser.onLine("[download] 100% of   50.00MiB in 00:15 at 3.30MiB/s");

        ProgressUpdate update = parser.finish("3.00KiB");

        assertThat(update.hasEnded()).isTrue();
        assertThat(update.size()).isEqualTo("3.00KiB");
        assertThat(update.speed()).isEqualTo("3.30MiB/s");
    }

    @Test
    void finish_KeepsTheParsedSizeWithoutAFileSize() {
        parser.onLine("[download]  12.3% of ~  50.00MiB at    2.00MiB/s ETA 00:25");

        assertThat(parser.finish(null).size()).isEqualTo("50.00MiB");
    }
}
