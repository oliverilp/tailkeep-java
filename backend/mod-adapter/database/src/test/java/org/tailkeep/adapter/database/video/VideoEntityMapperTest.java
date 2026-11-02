package org.tailkeep.adapter.database.video;

import org.junit.jupiter.api.Test;
import org.tailkeep.adapter.database.channel.ChannelEntity;
import org.tailkeep.domain.video.model.Video;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VideoEntityMapperTest {
    private final UUID channelId = UUID.randomUUID();
    private final ChannelEntity channel = ChannelEntity.builder().id(channelId).build();
    private final Video video = Video.builder()
            .id(UUID.randomUUID())
            .youtubeId("abc")
            .channelId(channelId)
            .url("https://www.youtube.com/watch?v=abc")
            .title("Title")
            .durationString("03:32")
            .duration(212.0)
            .thumbnailUrl("https://img.example.com/abc.jpg")
            .description("A description")
            .viewCount(1000L)
            .commentCount(50L)
            .filename("abc.mp4")
            .createdAt(LocalDateTime.of(2025, 1, 1, 12, 0))
            .updatedAt(LocalDateTime.of(2025, 1, 2, 12, 0))
            .build();

    @Test
    void mapsEveryFieldToTheEntity() {
        VideoEntity entity = VideoEntityMapper.toEntity(video, channel);

        assertThat(entity.getChannel()).isSameAs(channel);
        assertThat(entity).usingRecursiveComparison().ignoringFields("channel")
                .isEqualTo(VideoEntity.builder()
                        .id(video.id()).youtubeId("abc").url(video.url()).title("Title")
                        .durationString("03:32").duration(212.0).thumbnailUrl(video.thumbnailUrl())
                        .description("A description").viewCount(1000L).commentCount(50L)
                        .filename("abc.mp4").createdAt(video.createdAt()).updatedAt(video.updatedAt())
                        .build());
    }

    @Test
    void mapsBackToTheSameVideo() {
        assertThat(VideoEntityMapper.toDomain(VideoEntityMapper.toEntity(video, channel))).isEqualTo(video);
    }
}
