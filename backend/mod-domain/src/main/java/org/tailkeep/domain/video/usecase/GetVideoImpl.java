package org.tailkeep.domain.video.usecase;

import org.tailkeep.domain.channel.Channel;
import org.tailkeep.domain.channel.FindChannel;
import org.tailkeep.domain.shared.ResourceNotFoundException;
import org.tailkeep.domain.shared.UseCase;
import org.tailkeep.domain.video.model.Video;
import org.tailkeep.domain.video.model.VideoDetails;
import org.tailkeep.domain.video.model.VideoDownloadCount;
import org.tailkeep.domain.video.port.CountVideoDownloads;
import org.tailkeep.domain.video.port.FindVideo;

import java.util.UUID;

@UseCase
public class GetVideoImpl implements GetVideo {
    private final FindVideo findVideo;
    private final FindChannel findChannel;
    private final CountVideoDownloads countVideoDownloads;

    public GetVideoImpl(FindVideo findVideo, FindChannel findChannel, CountVideoDownloads countVideoDownloads) {
        this.findVideo = findVideo;
        this.findChannel = findChannel;
        this.countVideoDownloads = countVideoDownloads;
    }

    @Override
    public VideoDetails execute(UUID id) {
        Video video = findVideo.byId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video", id.toString()));
        Channel channel = findChannel.byId(video.channelId())
                .orElseThrow(() -> new ResourceNotFoundException("Channel", video.channelId().toString()));
        VideoDownloadCount downloads = countVideoDownloads.byVideoId(id);

        return new VideoDetails(video, channel, downloads.isDone());
    }
}
