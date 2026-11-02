package org.tailkeep.adapter.web.download;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.tailkeep.domain.channel.Channel;
import org.tailkeep.domain.download.model.DownloadDetails;
import org.tailkeep.domain.download.model.DownloadProgress;
import org.tailkeep.domain.download.model.DownloadsDashboard;
import org.tailkeep.domain.download.model.QueueInfo;
import org.tailkeep.domain.shared.PageResult;
import org.tailkeep.domain.video.model.Video;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DownloadResponseMapperTest {
    private final ObjectMapper json = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final Channel channel = Channel.builder().id(UUID.randomUUID()).youtubeId("UC1").name("Channel").build();
    private final Video video = Video.builder().id(UUID.randomUUID()).youtubeId("abc").channelId(channel.id()).build();
    private final DownloadDetails details = new DownloadDetails(DownloadProgress.builder()
            .jobId(UUID.randomUUID())
            .videoId(video.id())
            .status("download")
            .hasEnded(false)
            .progress(42.5)
            .size("10MiB")
            .speed("1MiB/s")
            .eta("00:05")
            .createdAt(LocalDateTime.of(2025, 1, 1, 12, 0))
            .deletedAt(LocalDateTime.of(2025, 1, 5, 12, 0))
            .build(), video, channel);

    @Test
    void toResponse_ShouldKeepTheFieldNamesOfTheApi() throws Exception {
        JsonNode node = json.valueToTree(DownloadResponseMapper.toResponse(details));

        assertThat(node.fieldNames()).toIterable().containsExactlyInAnyOrder(
                "id", "video", "status", "hasEnded", "progress", "size", "speed", "eta",
                "createdAt", "updatedAt", "completedAt");
        assertThat(node.get("id").asText()).isEqualTo(details.download().jobId().toString());
        assertThat(node.get("video").get("youtubeId").asText()).isEqualTo("abc");
        assertThat(node.get("video").get("channel").get("name").asText()).isEqualTo("Channel");
        assertThat(node.get("createdAt").asText()).isEqualTo("2025-01-01T12:00:00");
        assertThat(node.get("completedAt").isNull()).isTrue();
    }

    @Test
    void toDashboardResponse_ShouldMapTheQueueAndPage() {
        DownloadsDashboard dashboard = new DownloadsDashboard(new QueueInfo(1, 2, 3),
                new PageResult<>(List.of(details), 2, 1, 3));

        JsonNode node = json.valueToTree(DownloadResponseMapper.toDashboardResponse(dashboard));

        assertThat(node.get("queueInfo").get("queue").asLong()).isEqualTo(1);
        assertThat(node.get("queueInfo").get("active").asLong()).isEqualTo(2);
        assertThat(node.get("queueInfo").get("finished").asLong()).isEqualTo(3);
        JsonNode page = node.get("downloads");
        assertThat(page.fieldNames()).toIterable().containsExactlyInAnyOrder(
                "items", "totalPages", "totalItems", "currentPage", "pageSize", "hasNext", "hasPrevious");
        assertThat(page.get("items")).hasSize(1);
        assertThat(page.get("totalPages").asInt()).isEqualTo(3);
        assertThat(page.get("totalItems").asLong()).isEqualTo(3);
        assertThat(page.get("currentPage").asInt()).isEqualTo(2);
        assertThat(page.get("pageSize").asInt()).isEqualTo(1);
        assertThat(page.get("hasNext").asBoolean()).isTrue();
        assertThat(page.get("hasPrevious").asBoolean()).isTrue();
    }
}
