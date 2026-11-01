package org.tailkeep.domain.video;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VideoDownloadCountTest {
    @Test
    void noDownloadsIsNotDone() {
        assertThat(new VideoDownloadCount(0, 0).isDone()).isFalse();
    }

    @Test
    void aRunningDownloadIsNotDone() {
        assertThat(new VideoDownloadCount(1, 0).isDone()).isFalse();
        assertThat(new VideoDownloadCount(3, 2).isDone()).isFalse();
    }

    @Test
    void endedDownloadsAreDone() {
        assertThat(new VideoDownloadCount(1, 1).isDone()).isTrue();
        assertThat(new VideoDownloadCount(3, 3).isDone()).isTrue();
    }
}
