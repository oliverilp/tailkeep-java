package org.tailkeep.domain.download.model;

import org.tailkeep.domain.channel.Channel;
import org.tailkeep.domain.video.model.Video;

/** A download with the video it is for and that video's channel, as the responses need them. */
public record DownloadDetails(DownloadProgress download, Video video, Channel channel) {
}
