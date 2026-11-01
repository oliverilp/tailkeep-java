package org.tailkeep.api.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.tailkeep.api.support.Json;
import org.tailkeep.api.support.Shapes;
import org.tailkeep.api.support.Wire;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MetadataResultListenerIntegrationTest extends MessagingIntegrationTest {

    @Test
    void metadataResult_ShouldSaveTheVideoAndItsChannelAndRequestTheDownload() {
        String token = userToken();
        UUID jobId = db.queuedJob("https://www.youtube.com/watch?v=abc123");

        wire.send(Wire.METADATA_RESULTS, metadataResult(jobId, "abc123", "First title", 1000));

        JsonNode request = wire.awaitMessageForJob(Wire.DOWNLOAD_QUEUE, jobId);
        Json.assertKeys(request, "jobId", "videoId", "url", "filename");
        assertThat(request.get("videoId").asText()).isEqualTo("abc123");
        assertThat(request.get("url").asText()).isEqualTo("https://www.youtube.com/watch?v=abc123");
        assertThat(request.get("filename").asText()).isEqualTo("abc123.mp4");

        JsonNode videos = api.get("/api/v1/videos", token).json();
        assertThat(videos).hasSize(1);
        JsonNode video = Shapes.video(videos.get(0), false);
        assertThat(video.get("youtubeId").asText()).isEqualTo("abc123");
        assertThat(video.get("url").asText()).isEqualTo("https://www.youtube.com/watch?v=abc123");
        assertThat(video.get("title").asText()).isEqualTo("First title");
        assertThat(video.get("durationString").asText()).isEqualTo("10:00");
        assertThat(video.get("duration").asDouble()).isEqualTo(600.0);
        assertThat(video.get("thumbnailUrl").asText()).isEqualTo("https://img.example.com/abc123.jpg");
        assertThat(video.get("description").asText()).isEqualTo("A description");
        assertThat(video.get("viewCount").asLong()).isEqualTo(1000);
        assertThat(video.get("commentCount").asLong()).isEqualTo(100);
        assertThat(video.get("filename").asText()).isEqualTo("abc123.mp4");
        JsonNode channel = video.get("channel");
        assertThat(channel.get("name").asText()).isEqualTo("Some Uploader");
        assertThat(channel.get("youtubeId").asText()).isEqualTo("UC123");
        assertThat(channel.get("channelUrl").asText()).isEqualTo("https://www.youtube.com/channel/UC123");
    }

    @Test
    void metadataResult_ForAKnownVideo_ShouldUpdateItWithoutDuplicates() {
        String token = userToken();
        UUID firstJob = db.queuedJob("https://www.youtube.com/watch?v=abc123");
        UUID secondJob = db.queuedJob("https://www.youtube.com/watch?v=abc123");
        wire.send(Wire.METADATA_RESULTS, metadataResult(firstJob, "abc123", "First title", 1000));
        wire.awaitMessageForJob(Wire.DOWNLOAD_QUEUE, firstJob);
        String channelId = api.get("/api/v1/videos", token).json().get(0).get("channel").get("id").asText();

        wire.send(Wire.METADATA_RESULTS, metadataResult(secondJob, "abc123", "Second title", 2000));

        wire.awaitMessageForJob(Wire.DOWNLOAD_QUEUE, secondJob);
        JsonNode videos = api.get("/api/v1/videos", token).json();
        assertThat(videos).hasSize(1);
        assertThat(videos.get(0).get("title").asText()).isEqualTo("Second title");
        assertThat(videos.get(0).get("viewCount").asLong()).isEqualTo(2000);
        assertThat(videos.get(0).get("channel").get("id").asText()).isEqualTo(channelId);
        assertThat(db.count("channel")).isEqualTo(1);
    }
}
