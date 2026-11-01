package org.tailkeep.api.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.tailkeep.api.config.KafkaTopicNames;
import org.tailkeep.api.message.DownloadRequestMessage;
import org.tailkeep.api.message.MetadataResultMessage;
import org.tailkeep.api.model.Channel;
import org.tailkeep.api.model.Job;
import org.tailkeep.api.model.Video;
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

    @KafkaListener(topics = KafkaTopicNames.METADATA_RESULTS, groupId = "metadata-results-consumer", containerFactory = "metadataFactory")
    public void listen(MetadataResultMessage message) {
        log.info("Received metadata result: {}", message);

        Channel channel = channelService.getOrCreateChannel(message.metadata());
        Video video = videoService.createOrUpdateVideo(message.metadata(), channel);
        
        Job job = jobService.updateJobMetadata(message.jobId(), video);

        DownloadRequestMessage downloadRequest = new DownloadRequestMessage(
                job.getId(),
                video.getYoutubeId(),
                message.metadata().url(),
                message.metadata().filename());
        downloadKafkaTemplate.send(KafkaTopicNames.DOWNLOAD_QUEUE, downloadRequest);

        log.info("Sent download request for job: {}", message.jobId());
    }
}
