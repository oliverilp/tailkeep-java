package org.tailkeep.api.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.tailkeep.api.integration.BaseIntegrationTest;
import org.tailkeep.api.support.Wire;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;

/** Sends raw JSON to the topics the worker uses and checks the effects over HTTP. */
abstract class MessagingIntegrationTest extends BaseIntegrationTest {

    @Autowired
    protected Wire wire;

    @BeforeEach
    void waitForListeners() {
        wire.awaitListenersAssigned();
    }

    protected static void await(Supplier<Boolean> condition) {
        Awaitility.await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofMillis(100)).until(condition::get);
    }

    protected static String metadataResult(UUID jobId, String youtubeId, String title, long viewCount) {
        return """
                {"jobId":"%s","metadata":{
                  "youtubeId":"%s","url":"https://www.youtube.com/watch?v=%s","title":"%s",
                  "uploader":"Some Uploader","channelId":"UC123","channelUrl":"https://www.youtube.com/channel/UC123",
                  "durationString":"10:00","duration":600.0,"thumbnailUrl":"https://img.example.com/%s.jpg",
                  "description":"A description","viewCount":%d,"commentCount":100,"filename":"%s.mp4"}}
                """.formatted(jobId, youtubeId, youtubeId, title, youtubeId, viewCount, youtubeId);
    }

    protected static String progress(UUID jobId, String youtubeId, String status, boolean hasEnded, double progress,
            String size, String speed, String eta) {
        return """
                {"videoId":"%s","jobId":"%s","status":"%s","hasEnded":%s,"progress":%s,
                 "size":"%s","speed":"%s","eta":"%s"}
                """.formatted(youtubeId, jobId, status, hasEnded, progress, size, speed, eta);
    }

    protected JsonNode download(String token, UUID jobId) {
        return api.get("/api/v1/downloads/" + jobId, token).json();
    }
}
