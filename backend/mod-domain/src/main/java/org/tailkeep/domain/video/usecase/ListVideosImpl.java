package org.tailkeep.domain.video.usecase;

import org.tailkeep.domain.channel.Channel;
import org.tailkeep.domain.channel.FindChannel;
import org.tailkeep.domain.shared.ResourceNotFoundException;
import org.tailkeep.domain.shared.UseCase;
import org.tailkeep.domain.video.model.Video;
import org.tailkeep.domain.video.model.VideoWithChannel;
import org.tailkeep.domain.video.port.FindVideo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@UseCase
public class ListVideosImpl implements ListVideos {
    private final FindVideo findVideo;
    private final FindChannel findChannel;

    public ListVideosImpl(FindVideo findVideo, FindChannel findChannel) {
        this.findVideo = findVideo;
        this.findChannel = findChannel;
    }

    @Override
    public List<VideoWithChannel> execute() {
        List<Video> videos = findVideo.newestFirst();
        if (videos.isEmpty()) {
            return List.of();
        }

        Set<UUID> channelIds = videos.stream().map(Video::channelId).collect(Collectors.toSet());
        Map<UUID, Channel> channels = findChannel.byIds(channelIds).stream()
                .collect(Collectors.toMap(Channel::id, Function.identity()));

        return videos.stream()
                .map(video -> new VideoWithChannel(video, channelOf(video, channels)))
                .toList();
    }

    private static Channel channelOf(Video video, Map<UUID, Channel> channels) {
        Channel channel = channels.get(video.channelId());
        if (channel == null) {
            throw new ResourceNotFoundException("Channel", video.channelId().toString());
        }
        return channel;
    }
}
