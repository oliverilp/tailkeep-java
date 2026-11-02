package org.tailkeep.support;

import com.fasterxml.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

/** The JSON shapes the web app reads. Each check lists every key and its type. */
public final class Shapes {
    private Shapes() {
    }

    public static JsonNode channel(JsonNode channel) {
        Json.assertKeys(channel, "id", "name", "youtubeId", "channelUrl", "createdAt", "updatedAt");
        Json.assertUuid(channel.get("id"));
        assertThat(channel.get("name").isTextual()).isTrue();
        assertThat(channel.get("youtubeId").isTextual()).isTrue();
        assertThat(channel.get("channelUrl").isTextual()).isTrue();
        Json.assertTimestamp(channel.get("createdAt"));
        Json.assertTimestamp(channel.get("updatedAt"));
        return channel;
    }

    /** {@code detailed} is the single-video response, which has one extra key. */
    public static JsonNode video(JsonNode video, boolean detailed) {
        if (detailed) {
            Json.assertKeys(video, "id", "youtubeId", "channel", "url", "title", "durationString", "duration",
                    "thumbnailUrl", "description", "viewCount", "commentCount", "filename", "createdAt", "updatedAt",
                    "doneDownloading");
            assertThat(video.get("doneDownloading").isBoolean()).isTrue();
        } else {
            Json.assertKeys(video, "id", "youtubeId", "channel", "url", "title", "durationString", "duration",
                    "thumbnailUrl", "description", "viewCount", "commentCount", "filename", "createdAt", "updatedAt");
        }
        Json.assertUuid(video.get("id"));
        channel(video.get("channel"));
        for (String text : new String[] { "youtubeId", "url", "title", "durationString", "thumbnailUrl",
                "description", "filename" }) {
            assertThat(video.get(text).isTextual()).as(text).isTrue();
        }
        assertThat(video.get("duration").isNumber()).isTrue();
        assertThat(video.get("viewCount").isNumber()).isTrue();
        assertThat(video.get("commentCount").isNumber()).isTrue();
        Json.assertTimestamp(video.get("createdAt"));
        Json.assertTimestamp(video.get("updatedAt"));
        return video;
    }

    /** What the downloads endpoints and the SSE stream send for one job. */
    public static JsonNode download(JsonNode download) {
        Json.assertKeys(download, "id", "video", "status", "hasEnded", "progress", "size", "speed", "eta",
                "createdAt", "updatedAt", "completedAt");
        Json.assertUuid(download.get("id"));
        video(download.get("video"), false);
        assertThat(download.get("status").isTextual()).isTrue();
        assertThat(download.get("hasEnded").isBoolean()).isTrue();
        assertThat(download.get("progress").isNumber()).isTrue();
        assertThat(download.get("size").isTextual()).isTrue();
        assertThat(download.get("speed").isTextual()).isTrue();
        assertThat(download.get("eta").isTextual()).isTrue();
        Json.assertTimestamp(download.get("createdAt"));
        Json.assertTimestamp(download.get("updatedAt"));
        Json.assertNullOrTimestamp(download.get("completedAt"));
        return download;
    }
}
