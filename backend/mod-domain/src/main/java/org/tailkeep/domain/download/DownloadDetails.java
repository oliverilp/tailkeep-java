package org.tailkeep.domain.download;

import org.tailkeep.domain.channel.Channel;
import org.tailkeep.domain.video.Video;

/** A download with the video it is for and that video's channel, as the responses need them. */
public record DownloadDetails(DownloadProgress download, Video video, Channel channel) {
}
