package org.tailkeep.adapter.messaging.publisher;

import org.springframework.stereotype.Component;
import org.tailkeep.adapter.messaging.contract.Destination;
import org.tailkeep.adapter.messaging.contract.DownloadBroadcastMessage;
import org.tailkeep.adapter.messaging.contract.DownloadMessageMapper;
import org.tailkeep.adapter.messaging.contract.MetadataMessageMapper;
import org.tailkeep.domain.download.model.DownloadDetails;
import org.tailkeep.domain.download.model.DownloadRequest;
import org.tailkeep.domain.download.model.MetadataRequest;
import org.tailkeep.domain.download.model.MetadataResult;
import org.tailkeep.domain.download.model.ProgressUpdate;
import org.tailkeep.domain.download.port.BroadcastDownloadProgress;
import org.tailkeep.domain.download.port.PublishDownloadProgress;
import org.tailkeep.domain.download.port.PublishDownloadRequest;
import org.tailkeep.domain.download.port.PublishMetadataRequest;
import org.tailkeep.domain.download.port.PublishMetadataResult;

import java.util.UUID;

/**
 * Implements the domain's publish ports by turning the domain message into its wire message and handing it to the
 * {@link MessagePublisher}. It knows destinations, not brokers. Every message is keyed by its job.
 */
@Component
public class MessagingPublishers implements
        PublishMetadataRequest, PublishMetadataResult, PublishDownloadRequest, PublishDownloadProgress,
        BroadcastDownloadProgress {

    private final MessagePublisher publisher;

    public MessagingPublishers(MessagePublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publish(MetadataRequest request) {
        publisher.publish(Destination.METADATA_REQUESTS, request.jobId().toString(),
                MetadataMessageMapper.toMessage(request));
    }

    @Override
    public void publish(MetadataResult result) {
        publisher.publish(Destination.METADATA_RESULTS, result.jobId().toString(),
                MetadataMessageMapper.toMessage(result));
    }

    @Override
    public void publish(DownloadRequest request) {
        publisher.publish(Destination.DOWNLOAD_REQUESTS, request.jobId().toString(),
                DownloadMessageMapper.toMessage(request));
    }

    @Override
    public void publish(ProgressUpdate update) {
        publisher.publish(Destination.DOWNLOAD_PROGRESS, update.jobId().toString(),
                DownloadMessageMapper.toMessage(update));
    }

    /** Only the job travels. Every instance loads the current state for its own clients. */
    @Override
    public void broadcast(DownloadDetails download) {
        UUID jobId = download.download().jobId();
        publisher.publish(Destination.DOWNLOAD_BROADCAST, jobId.toString(), new DownloadBroadcastMessage(jobId));
    }
}
