package org.tailkeep.domain.download;

import org.tailkeep.domain.channel.Channel;
import org.tailkeep.domain.channel.FindChannel;
import org.tailkeep.domain.shared.ResourceNotFoundException;
import org.tailkeep.domain.video.FindVideo;
import org.tailkeep.domain.video.Video;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Adds the video and channel to downloads, with one lookup of each. */
final class DownloadDetailsAssembler {
    private final FindVideo findVideo;
    private final FindChannel findChannel;

    DownloadDetailsAssembler(FindVideo findVideo, FindChannel findChannel) {
        this.findVideo = findVideo;
        this.findChannel = findChannel;
    }

    DownloadDetails assemble(DownloadProgress download) {
        return assemble(List.of(download)).get(0);
    }

    List<DownloadDetails> assemble(List<DownloadProgress> downloads) {
        if (downloads.isEmpty()) {
            return List.of();
        }

        Set<UUID> videoIds = downloads.stream().map(DownloadProgress::videoId).collect(Collectors.toSet());
        Map<UUID, Video> videos = findVideo.byIds(videoIds).stream()
                .collect(Collectors.toMap(Video::id, Function.identity()));
        Set<UUID> channelIds = videos.values().stream().map(Video::channelId).collect(Collectors.toSet());
        Map<UUID, Channel> channels = findChannel.byIds(channelIds).stream()
                .collect(Collectors.toMap(Channel::id, Function.identity()));

        return downloads.stream().map(download -> {
            Video video = videos.get(download.videoId());
            if (video == null) {
                throw new ResourceNotFoundException("Video", download.videoId().toString());
            }
            Channel channel = channels.get(video.channelId());
            if (channel == null) {
                throw new ResourceNotFoundException("Channel", video.channelId().toString());
            }
            return new DownloadDetails(download, video, channel);
        }).toList();
    }
}
