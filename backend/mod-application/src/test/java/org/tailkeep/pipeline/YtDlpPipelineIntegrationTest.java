package org.tailkeep.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tailkeep.integration.BaseIntegrationTest;
import org.tailkeep.support.Api;
import org.tailkeep.support.Json;
import org.tailkeep.support.Wire;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the yt-dlp steps of the pipeline with yt-dlp replaced by {@link FakeYtDlp}. Messages go in and out as raw
 * JSON, so the topic names and the wire format are pinned. The other steps run in the same context, so the last
 * test follows a job all the way to a finished download.
 */
class YtDlpPipelineIntegrationTest extends BaseIntegrationTest {
    private static final String URL = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";

    @Autowired
    private Wire wire;

    @BeforeEach
    void startYtDlp() {
        ytDlp.setAvailable(true);
        wire.awaitListenersAssigned();
    }

    @Test
    void metadataRequest_ShouldProduceTheVideoMetadata() throws Exception {
        UUID jobId = db.queuedJob(URL);

        wire.send(Wire.METADATA_QUEUE, "{\"jobId\":\"%s\",\"url\":\"%s\"}".formatted(jobId, URL));

        JsonNode result = wire.awaitMessagesForJob(Wire.METADATA_RESULTS, jobId, message -> true).getFirst();
        Json.assertKeys(result, "jobId", "metadata");
        assertThat(result.get("jobId").asText()).isEqualTo(jobId.toString());
        JsonNode metadata = result.get("metadata");
        Json.assertKeys(metadata, "youtubeId", "url", "title", "uploader", "channelId", "channelUrl",
                "durationString", "duration", "thumbnailUrl", "description", "viewCount", "commentCount", "filename");
        assertThat(metadata.get("youtubeId").asText()).isEqualTo("dQw4w9WgXcQ");
        assertThat(metadata.get("url").asText()).isEqualTo(URL);
        assertThat(metadata.get("title").asText()).isEqualTo("Rick Astley - Never Gonna Give You Up (Official Music Video)");
        assertThat(metadata.get("uploader").asText()).isEqualTo("Rick Astley");
        assertThat(metadata.get("channelId").asText()).isEqualTo("UCuAXFkgsw1L7xaCfnd5JJOw");
        assertThat(metadata.get("channelUrl").asText())
                .isEqualTo("https://www.youtube.com/channel/UCuAXFkgsw1L7xaCfnd5JJOw");
        assertThat(metadata.get("durationString").asText()).isEqualTo("3:32");
        assertThat(metadata.get("duration").asDouble()).isEqualTo(212.0);
        assertThat(metadata.get("thumbnailUrl").asText())
                .isEqualTo("https://i.ytimg.com/vi_webp/dQw4w9WgXcQ/maxresdefault.webp");
        assertThat(metadata.get("description").asText()).startsWith("The official video");
        assertThat(metadata.get("viewCount").asLong()).isEqualTo(1605836884L);
        assertThat(metadata.get("commentCount").asLong()).isEqualTo(2300000L);
        assertThat(metadata.get("filename").asText()).isEqualTo(FakeYtDlp.filename());
    }

    @Test
    void downloadRequest_ShouldReportProgressUntilTheDownloadHasEnded() throws Exception {
        UUID channelId = db.insertChannel("Rick Astley", "UCuAXFkgsw1L7xaCfnd5JJOw");
        UUID videoId = db.insertVideo(channelId, "dQw4w9WgXcQ", "Never gonna", LocalDateTime.now());
        UUID jobId = db.insertJob(videoId, URL);

        wire.send(Wire.DOWNLOAD_QUEUE, "{\"jobId\":\"%s\",\"videoId\":\"dQw4w9WgXcQ\",\"url\":\"%s\",\"filename\":\"%s\"}"
                .formatted(jobId, URL, FakeYtDlp.filename()));

        List<JsonNode> updates = wire.awaitMessagesForJob(Wire.DOWNLOAD_PROGRESS, jobId,
                message -> message.get("hasEnded").asBoolean());
        updates.forEach(update -> {
            Json.assertKeys(update, "videoId", "jobId", "status", "hasEnded", "progress", "size", "speed", "eta");
            assertThat(update.get("videoId").asText()).isEqualTo("dQw4w9WgXcQ");
            assertThat(update.get("jobId").asText()).isEqualTo(jobId.toString());
        });

        JsonNode midway = updates.stream().filter(update -> update.get("progress").asDouble() == 12.3).findFirst()
                .orElseThrow();
        assertThat(midway.get("status").asText()).isEqualTo("download");
        assertThat(midway.get("hasEnded").asBoolean()).isFalse();
        assertThat(midway.get("size").asText()).isEqualTo("50.00MiB");
        assertThat(midway.get("speed").asText()).isEqualTo("2.00MiB/s");
        assertThat(midway.get("eta").asText()).isEqualTo("00:25");

        List<Double> progress = updates.stream().map(update -> update.get("progress").asDouble()).toList();
        assertThat(progress).isSorted();

        // Only the last update has ended, and its size is the size of the file on disk
        assertThat(updates.stream().filter(update -> update.get("hasEnded").asBoolean())).hasSize(1);
        JsonNode last = updates.getLast();
        assertThat(last.get("hasEnded").asBoolean()).isTrue();
        assertThat(last.get("progress").asDouble()).isEqualTo(100.0);
        assertThat(last.get("size").asText()).isEqualTo("3.00KiB");
        assertThat(last.get("speed").asText()).isEqualTo("3.30MiB/s");
    }

    @Test
    void metadataRequest_ShouldEndInAFinishedDownload() {
        String token = adminToken();
        UUID jobId = db.queuedJob(URL);

        wire.send(Wire.METADATA_QUEUE, "{\"jobId\":\"%s\",\"url\":\"%s\"}".formatted(jobId, URL));

        Awaitility.await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofMillis(100)).until(() -> {
            Api.Response response = api.get("/api/v1/downloads/" + jobId, token);
            return response.status() == 200 && response.json().get("hasEnded").asBoolean();
        });
        JsonNode finished = api.get("/api/v1/downloads/" + jobId, token).json();
        assertThat(finished.get("status").asText()).isEqualTo("done");
        assertThat(finished.get("size").asText()).isEqualTo("3.00KiB");
        assertThat(finished.get("video").get("youtubeId").asText()).isEqualTo("dQw4w9WgXcQ");
    }
}
