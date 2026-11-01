package org.tailkeep.adapter.messaging.publisher;

import org.junit.jupiter.api.Test;
import org.tailkeep.adapter.messaging.contract.Destination;
import org.tailkeep.adapter.messaging.contract.DownloadBroadcastMessage;
import org.tailkeep.adapter.messaging.contract.DownloadProgressMessage;
import org.tailkeep.adapter.messaging.contract.DownloadRequestMessage;
import org.tailkeep.adapter.messaging.contract.MetadataRequestMessage;
import org.tailkeep.domain.download.DownloadDetails;
import org.tailkeep.domain.download.DownloadProgress;
import org.tailkeep.domain.download.DownloadRequest;
import org.tailkeep.domain.download.MetadataRequest;
import org.tailkeep.domain.download.ProgressUpdate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MessagingPublishersTest {
    record Sent(Destination destination, String key, Object message) {
    }

    private final List<Sent> sent = new ArrayList<>();
    private final MessagingPublishers publishers =
            new MessagingPublishers((destination, key, message) -> sent.add(new Sent(destination, key, message)));
    private final UUID jobId = UUID.randomUUID();

    @Test
    void publish_ShouldSendTheWireMessageToTheDestinationKeyedByTheJob() {
        publishers.publish(new MetadataRequest(jobId, "https://youtube.com/watch?v=abc"));
        publishers.publish(new DownloadRequest(jobId, "abc", "https://youtube.com/watch?v=abc", "abc.mp4"));
        publishers.publish(new ProgressUpdate("abc", jobId, "download", false, 10, "1MiB", "1MiB/s", "00:10"));

        assertThat(sent).containsExactly(
                new Sent(Destination.METADATA_REQUESTS, jobId.toString(),
                        new MetadataRequestMessage(jobId, "https://youtube.com/watch?v=abc")),
                new Sent(Destination.DOWNLOAD_REQUESTS, jobId.toString(),
                        new DownloadRequestMessage(jobId, "abc", "https://youtube.com/watch?v=abc", "abc.mp4")),
                new Sent(Destination.DOWNLOAD_PROGRESS, jobId.toString(),
                        new DownloadProgressMessage("abc", jobId, "download", false, 10, "1MiB", "1MiB/s", "00:10")));
    }

    @Test
    void broadcast_ShouldSendOnlyTheJobToTheBroadcastDestination() {
        publishers.broadcast(new DownloadDetails(DownloadProgress.builder().jobId(jobId).build(), null, null));

        assertThat(sent).containsExactly(
                new Sent(Destination.DOWNLOAD_BROADCAST, jobId.toString(), new DownloadBroadcastMessage(jobId)));
        assertThat(Destination.DOWNLOAD_BROADCAST.deliveryType()).isEqualTo(org.tailkeep.adapter.messaging.contract.DeliveryType.BROADCAST);
    }
}
