package org.tailkeep.adapter.database.channel;

import org.tailkeep.domain.channel.Channel;

public final class ChannelEntityMapper {
    private ChannelEntityMapper() {
    }

    public static Channel toDomain(ChannelEntity entity) {
        return Channel.builder()
                .id(entity.getId())
                .youtubeId(entity.getYoutubeId())
                .name(entity.getName())
                .channelUrl(entity.getChannelUrl())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static ChannelEntity toEntity(Channel channel) {
        return ChannelEntity.builder()
                .id(channel.id())
                .youtubeId(channel.youtubeId())
                .name(channel.name())
                .channelUrl(channel.channelUrl())
                .createdAt(channel.createdAt())
                .updatedAt(channel.updatedAt())
                .build();
    }
}
