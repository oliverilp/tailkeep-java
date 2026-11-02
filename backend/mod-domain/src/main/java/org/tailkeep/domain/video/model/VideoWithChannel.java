package org.tailkeep.domain.video.model;

import org.tailkeep.domain.channel.Channel;

public record VideoWithChannel(Video video, Channel channel) {
}
