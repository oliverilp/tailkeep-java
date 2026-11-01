package org.tailkeep.adapter.database.channel;

import org.junit.jupiter.api.Test;
import org.tailkeep.domain.channel.Channel;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ChannelEntityMapperTest {
    private final Channel channel = Channel.builder()
            .id(UUID.randomUUID())
            .youtubeId("UCabc")
            .name("Channel")
            .channelUrl("https://www.youtube.com/channel/UCabc")
            .createdAt(LocalDateTime.of(2025, 1, 1, 12, 0))
            .updatedAt(LocalDateTime.of(2025, 1, 2, 12, 0))
            .build();

    @Test
    void mapsEveryFieldToTheEntity() {
        ChannelEntity entity = ChannelEntityMapper.toEntity(channel);

        assertThat(entity.getId()).isEqualTo(channel.id());
        assertThat(entity.getYoutubeId()).isEqualTo("UCabc");
        assertThat(entity.getName()).isEqualTo("Channel");
        assertThat(entity.getChannelUrl()).isEqualTo("https://www.youtube.com/channel/UCabc");
        assertThat(entity.getCreatedAt()).isEqualTo(channel.createdAt());
        assertThat(entity.getUpdatedAt()).isEqualTo(channel.updatedAt());
    }

    @Test
    void mapsBackToTheSameChannel() {
        assertThat(ChannelEntityMapper.toDomain(ChannelEntityMapper.toEntity(channel))).isEqualTo(channel);
    }
}
