package org.tailkeep.adapter.ytdlp.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tailkeep.adapter.ytdlp.process.YtDlpRunner;
import org.tailkeep.domain.download.model.VideoMetadata;
import org.tailkeep.domain.download.port.VideoMetadataProvider;

import java.util.List;

/** Fetches metadata with {@code yt-dlp -j}, which prints the video's JSON on stdout. */
@Component
@Slf4j
public class YtDlpVideoMetadataProvider implements VideoMetadataProvider {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final YtDlpRunner runner;

    public YtDlpVideoMetadataProvider(YtDlpRunner runner) {
        this.runner = runner;
    }

    @Override
    public VideoMetadata fetch(String url) {
        StringBuilder json = new StringBuilder();
        runner.run(List.of("-j", url), json::append);
        return parse(json.toString());
    }

    private VideoMetadata parse(String jsonStr) {
        try {
            JsonNode json = OBJECT_MAPPER.readTree(jsonStr);
            log.info("Metadata finished {}", json.get("title").asText());

            return new VideoMetadata(
                    json.get("id").asText(),
                    json.get("webpage_url").asText(),
                    json.get("title").asText(),
                    json.get("uploader").asText(),
                    json.get("channel_id").asText(),
                    json.get("channel_url").asText(),
                    json.get("duration_string").asText(),
                    json.get("duration").asDouble(),
                    json.get("thumbnail").asText(),
                    json.path("description").asText(""),
                    json.path("view_count").asLong(0),
                    json.path("comment_count").asLong(0),
                    json.get("filename").asText());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse metadata", e);
        }
    }
}
