package org.tailkeep.adapter.web.channel;

import org.tailkeep.domain.channel.Channel;

public final class ChannelResponseMapper {
    private ChannelResponseMapper() {
    }

    public static ChannelResponse toResponse(Channel channel) {
        return new ChannelResponse(
                channel.id(),
                channel.name(),
                channel.youtubeId(),
                channel.channelUrl(),
                channel.createdAt(),
                channel.updatedAt());
    }
}
