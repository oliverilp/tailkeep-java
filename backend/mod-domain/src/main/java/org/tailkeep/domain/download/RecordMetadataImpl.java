package org.tailkeep.domain.download;

import org.tailkeep.domain.channel.Channel;
import org.tailkeep.domain.channel.FindChannel;
import org.tailkeep.domain.channel.SaveChannel;
import org.tailkeep.domain.shared.ResourceNotFoundException;
import org.tailkeep.domain.shared.RunInTransaction;
import org.tailkeep.domain.shared.UseCase;
import org.tailkeep.domain.video.FindVideo;
import org.tailkeep.domain.video.SaveVideo;
import org.tailkeep.domain.video.Video;

@UseCase
public class RecordMetadataImpl implements RecordMetadata {
    private final FindJob findJob;
    private final SaveJob saveJob;
    private final FindChannel findChannel;
    private final SaveChannel saveChannel;
    private final FindVideo findVideo;
    private final SaveVideo saveVideo;
    private final RunInTransaction runInTransaction;
    private final PublishDownloadRequest publishDownloadRequest;

    public RecordMetadataImpl(
            FindJob findJob,
            SaveJob saveJob,
            FindChannel findChannel,
            SaveChannel saveChannel,
            FindVideo findVideo,
            SaveVideo saveVideo,
            RunInTransaction runInTransaction,
            PublishDownloadRequest publishDownloadRequest) {
        this.findJob = findJob;
        this.saveJob = saveJob;
        this.findChannel = findChannel;
        this.saveChannel = saveChannel;
        this.findVideo = findVideo;
        this.saveVideo = saveVideo;
        this.runInTransaction = runInTransaction;
        this.publishDownloadRequest = publishDownloadRequest;
    }

    @Override
    public void execute(MetadataResult result) {
        VideoMetadata metadata = result.metadata();

        // The channel, video and job are saved together, and the message goes out after they are committed
        DownloadRequest request = runInTransaction.execute(() -> {
            Job job = findJob.byId(result.jobId())
                    .orElseThrow(() -> new ResourceNotFoundException("Job", result.jobId().toString()));

            Channel channel = channelOf(metadata);
            Video video = videoOf(metadata, channel);
            saveJob.save(job.withVideo(video.id()));

            return new DownloadRequest(result.jobId(), video.youtubeId(), metadata.url(), metadata.filename());
        });

        publishDownloadRequest.publish(request);
    }

    private Channel channelOf(VideoMetadata metadata) {
        return findChannel.byYoutubeId(metadata.channelId())
                .orElseGet(() -> saveChannel.save(Channel.builder()
                        .youtubeId(metadata.channelId())
                        .name(metadata.uploader())
                        .channelUrl(metadata.channelUrl())
                        .build()));
    }

    private Video videoOf(VideoMetadata metadata, Channel channel) {
        Video video = findVideo.byYoutubeId(metadata.youtubeId())
                .orElseGet(() -> Video.builder().youtubeId(metadata.youtubeId()).build());

        return saveVideo.save(video.toBuilder()
                .channelId(channel.id())
                .url(metadata.url())
                .title(metadata.title())
                .durationString(metadata.durationString())
                .duration(metadata.duration())
                .thumbnailUrl(metadata.thumbnailUrl())
                .description(metadata.description())
                .viewCount(metadata.viewCount())
                .commentCount(metadata.commentCount())
                .filename(metadata.filename())
                .build());
    }
}
