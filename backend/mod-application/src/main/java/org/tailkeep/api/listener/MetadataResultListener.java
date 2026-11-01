package org.tailkeep.api.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.tailkeep.api.config.KafkaTopicNames;
import org.tailkeep.api.message.DownloadRequestMessage;
import org.tailkeep.api.message.MetadataResultMessage;
import org.tailkeep.adapter.database.download.JobEntity;
import org.tailkeep.domain.channel.Channel;
import org.tailkeep.domain.video.Video;
import org.tailkeep.api.service.ChannelService;
import org.tailkeep.api.service.JobService;
import org.tailkeep.api.service.VideoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class MetadataResultListener {
    private final KafkaTemplate<String, DownloadRequestMessage> downloadKafkaTemplate;
    private final ChannelService channelService;
    private final VideoService videoService;
    private final JobService jobService;

    @KafkaListener(topics = KafkaTopicNames.METADATA_RESULTS, groupId = "metadata-results-consumer", containerFactory = "metadataResultFactory")
    public void listen(MetadataResultMessage message) {
        log.info("Received metadata result: {}", message);

        Channel channel = channelService.getOrCreateChannel(message.metadata());
        Video video = videoService.createOrUpdateVideo(message.metadata(), channel);
        
        JobEntity job = jobService.updateJobMetadata(message.jobId(), video.id());

        DownloadRequestMessage downloadRequest = new DownloadRequestMessage(
                job.getId(),
                video.youtubeId(),
                message.metadata().url(),
                message.metadata().filename());
        downloadKafkaTemplate.send(KafkaTopicNames.DOWNLOAD_QUEUE, downloadRequest);

        log.info("Sent download request for job: {}", message.jobId());
    }
}
