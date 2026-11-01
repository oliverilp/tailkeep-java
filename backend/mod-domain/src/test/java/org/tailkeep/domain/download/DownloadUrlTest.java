package org.tailkeep.domain.download;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DownloadUrlTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "not-a-url|Failed to validate URL",
            "http://example.com|URL must use HTTPS protocol",
            "https:///path|URL must have a valid host",
            "https://localhost/path|URL must have a valid domain",
            "ftp://example.com/file|URL must use HTTPS protocol",
    })
    void parse_WithAnUnacceptableUrl_ShouldFail(String url, String message) {
        assertThatThrownBy(() -> DownloadUrl.parse(url))
                .isInstanceOf(InvalidUrlException.class)
                .hasMessage(message);
    }

    @Test
    void parse_WithNothing_ShouldFailToValidate() {
        assertThatThrownBy(() -> DownloadUrl.parse(null)).hasMessage("Failed to validate URL");
        assertThatThrownBy(() -> DownloadUrl.parse("")).hasMessage("Failed to validate URL");
    }

    @Test
    void parse_ShouldKeepTheUrlAsEntered() {
        String url = "https://www.youtube.com/watch?v=abc&list=xyz";

        assertThat(DownloadUrl.parse(url).value()).isEqualTo(url);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "https://www.youtube.com/watch?v=abc&list=xyz&t=5|https://www.youtube.com/watch?v=abc",
            "https://www.youtube.com/watch?v=abc|https://www.youtube.com/watch?v=abc",
            "https://www.youtube.com/watch?list=xyz|https://www.youtube.com/watch",
            "https://youtu.be/abc?si=tracking|https://youtu.be/abc",
            "https://example.com/video?id=1#top|https://example.com/video",
    })
    void cleaned_ShouldKeepOnlyWhatIdentifiesTheVideo(String url, String cleaned) {
        assertThat(DownloadUrl.parse(url).cleaned()).isEqualTo(cleaned);
    }
}
