package org.tailkeep.adapter.web.video;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.tailkeep.domain.channel.Channel;
import org.tailkeep.domain.video.Video;
import org.tailkeep.domain.video.VideoDetails;
import org.tailkeep.domain.video.VideoWithChannel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VideoResponseMapperTest {
    private final ObjectMapper json = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final Channel channel = Channel.builder()
            .id(UUID.randomUUID())
            .youtubeId("UCabc")
            .name("Channel")
            .channelUrl("https://www.youtube.com/channel/UCabc")
            .createdAt(LocalDateTime.of(2025, 1, 1, 12, 0))
            .updatedAt(LocalDateTime.of(2025, 1, 1, 13, 0))
            .build();
    private final Video video = Video.builder()
            .id(UUID.randomUUID())
            .youtubeId("abc")
            .channelId(channel.id())
            .url("https://www.youtube.com/watch?v=abc")
            .title("Title")
            .durationString("03:32")
            .duration(212.0)
            .thumbnailUrl("https://img.example.com/abc.jpg")
            .description("A description")
            .viewCount(1000L)
            .commentCount(50L)
            .filename("abc.mp4")
            .createdAt(LocalDateTime.of(2025, 1, 2, 12, 0))
            .updatedAt(LocalDateTime.of(2025, 1, 2, 13, 0))
            .build();

    @Test
    void mapsEveryVideoAndChannelField() {
        VideoResponse response = VideoResponseMapper.toResponse(new VideoWithChannel(video, channel));

        assertThat(response.id()).isEqualTo(video.id());
        assertThat(response.youtubeId()).isEqualTo("abc");
        assertThat(response.url()).isEqualTo(video.url());
        assertThat(response.title()).isEqualTo("Title");
        assertThat(response.durationString()).isEqualTo("03:32");
        assertThat(response.duration()).isEqualTo(212.0);
        assertThat(response.thumbnailUrl()).isEqualTo(video.thumbnailUrl());
        assertThat(response.description()).isEqualTo("A description");
        assertThat(response.viewCount()).isEqualTo(1000L);
        assertThat(response.commentCount()).isEqualTo(50L);
        assertThat(response.filename()).isEqualTo("abc.mp4");
        assertThat(response.createdAt()).isEqualTo(video.createdAt());
        assertThat(response.updatedAt()).isEqualTo(video.updatedAt());
        assertThat(response.channel().id()).isEqualTo(channel.id());
        assertThat(response.channel().name()).isEqualTo("Channel");
        assertThat(response.channel().youtubeId()).isEqualTo("UCabc");
        assertThat(response.channel().channelUrl()).isEqualTo(channel.channelUrl());
        assertThat(response.channel().createdAt()).isEqualTo(channel.createdAt());
        assertThat(response.channel().updatedAt()).isEqualTo(channel.updatedAt());
    }

    @Test
    void detailAddsDoneDownloading() {
        VideoDetailResponse response = VideoResponseMapper.toDetailResponse(new VideoDetails(video, channel, true));

        assertThat(response.doneDownloading()).isTrue();
        assertThat(response.id()).isEqualTo(video.id());
        assertThat(response.channel().id()).isEqualTo(channel.id());
        assertThat(response.filename()).isEqualTo("abc.mp4");
    }

    @Test
    void serializesTheFieldsTheWebAppReads() throws Exception {
        JsonNode list = json.valueToTree(VideoResponseMapper.toResponse(new VideoWithChannel(video, channel)));
        JsonNode detail = json.valueToTree(
                VideoResponseMapper.toDetailResponse(new VideoDetails(video, channel, false)));

        List<String> listFields = fieldNames(list);
        assertThat(listFields).containsExactly("id", "youtubeId", "channel", "url", "title", "durationString",
                "duration", "thumbnailUrl", "description", "viewCount", "commentCount", "filename", "createdAt",
                "updatedAt");
        assertThat(fieldNames(detail)).containsExactlyElementsOf(withDoneDownloading(listFields));
        assertThat(fieldNames(list.get("channel")))
                .containsExactly("id", "name", "youtubeId", "channelUrl", "createdAt", "updatedAt");
        assertThat(list.get("createdAt").asText()).isEqualTo("2025-01-02T12:00:00");
        assertThat(detail.get("doneDownloading").asBoolean()).isFalse();
    }

    private static List<String> withDoneDownloading(List<String> fields) {
        List<String> all = new ArrayList<>(fields);
        all.add("doneDownloading");
        return all;
    }

    private static List<String> fieldNames(JsonNode node) {
        List<String> names = new ArrayList<>();
        node.fieldNames().forEachRemaining(names::add);
        return names;
    }
}
