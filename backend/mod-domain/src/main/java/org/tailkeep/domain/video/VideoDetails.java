package org.tailkeep.domain.video;

import org.tailkeep.domain.channel.Channel;

public record VideoDetails(Video video, Channel channel, boolean doneDownloading) {
}
