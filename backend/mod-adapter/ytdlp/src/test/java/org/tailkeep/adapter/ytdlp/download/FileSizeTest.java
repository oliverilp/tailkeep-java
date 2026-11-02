package org.tailkeep.adapter.ytdlp.download;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileSizeTest {
    @Test
    void parseSize_ReadsBinaryAndDecimalUnits() {
        assertThat(FileSize.parseSize("1.00KiB")).isEqualTo(1024);
        assertThat(FileSize.parseSize("2.00MB")).isEqualTo(2_000_000);
        assertThat(FileSize.parseSize("1.50mib")).isEqualTo(1_572_864);
    }

    @Test
    void parseSize_RejectsGarbage() {
        assertThatThrownBy(() -> FileSize.parseSize("lots")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> FileSize.parseSize("1.00XB")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void formatSize_UsesBinaryUnits() {
        assertThat(FileSize.formatSize(3072)).isEqualTo("3.00KiB");
        assertThat(FileSize.formatSize(500)).isEqualTo("500.00B");
        assertThat(FileSize.formatSize(0)).isEqualTo("0 B");
    }
}
