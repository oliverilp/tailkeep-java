package org.tailkeep.adapter.ytdlp.metadata;

import org.junit.jupiter.api.Test;
import org.tailkeep.adapter.ytdlp.process.YtDlpRunner;
import org.tailkeep.domain.download.model.VideoMetadata;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class YtDlpVideoMetadataProviderTest {
    private static final String URL = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";

    @Test
    void fetch_MapsTheJsonFields() throws IOException {
        VideoMetadata metadata = provider(load("/metadata/youtube-metadata-response.json")).fetch(URL);

        assertThat(metadata).isEqualTo(new VideoMetadata(
                "dQw4w9WgXcQ",
                "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                "Rick Astley - Never Gonna Give You Up (Official Music Video)",
                "Rick Astley",
                "UCuAXFkgsw1L7xaCfnd5JJOw",
                "https://www.youtube.com/channel/UCuAXFkgsw1L7xaCfnd5JJOw",
                "3:33",
                213.0,
                "https://i.ytimg.com/vi_webp/dQw4w9WgXcQ/maxresdefault.webp",
                "The official video",
                1_500_000_000L,
                2_400_000L,
                "Rick Astley - Never Gonna Give You Up (Official Music Video) [dQw4w9WgXcQ].mp4"));
    }

    @Test
    void fetch_DefaultsTheOptionalFields() {
        String json = """
                {"id":"a","webpage_url":"u","title":"t","uploader":"up","channel_id":"c","channel_url":"cu",
                "duration_string":"1:00","duration":60,"thumbnail":"th","filename":"f.mp4"}""";

        VideoMetadata metadata = provider(json).fetch(URL);

        assertThat(metadata.description()).isEmpty();
        assertThat(metadata.viewCount()).isZero();
        assertThat(metadata.commentCount()).isZero();
    }

    @Test
    void fetch_WhenARequiredFieldIsMissing_Throws() {
        assertThatThrownBy(() -> provider("{\"id\":\"a\",\"title\":\"t\"}").fetch(URL))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to parse metadata");
    }

    /** yt-dlp prints the JSON in one line, but the lines are joined in case it is split. */
    private static YtDlpVideoMetadataProvider provider(String json) {
        return new YtDlpVideoMetadataProvider(new YtDlpRunner(null, new org.tailkeep.adapter.ytdlp.config.YtDlpProperties()) {
            @Override
            public void run(List<String> args, Consumer<String> onStdoutLine) {
                assertThat(args).containsExactly("-j", URL);
                onStdoutLine.accept(json);
            }
        });
    }

    private static String load(String resource) throws IOException {
        try (InputStream in = YtDlpVideoMetadataProviderTest.class.getResourceAsStream(resource)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).replaceAll("\\s*\\n\\s*", "");
        }
    }
}
