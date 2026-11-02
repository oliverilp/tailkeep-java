package org.tailkeep.domain.download.usecase;

import org.junit.jupiter.api.Test;
import org.tailkeep.domain.channel.Channel;
import org.tailkeep.domain.download.model.DownloadDetails;
import org.tailkeep.domain.download.model.Job;
import org.tailkeep.domain.download.port.SendDownloadToClients;
import org.tailkeep.domain.video.model.Video;
import org.tailkeep.domain.video.usecase.FakeChannels;
import org.tailkeep.domain.video.usecase.FakeVideos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PushDownloadProgressImplTest {
    private final FakeJobs jobs = new FakeJobs();
    private final FakeChannels channels = new FakeChannels();
    private final FakeVideos videos = new FakeVideos();
    private final List<DownloadDetails> sent = new ArrayList<>();
    private boolean hasClients = true;

    private final PushDownloadProgressImpl push = new PushDownloadProgressImpl(new SendDownloadToClients() {
        @Override
        public boolean hasClients() {
            return hasClients;
        }

        @Override
        public void send(DownloadDetails download) {
            sent.add(download);
        }
    }, jobs, videos, channels);

    private final Channel channel = channels.add("Uploader");
    private final Video video = videos.add(channel.id(), "abc", LocalDateTime.of(2025, 1, 1, 12, 0));

    @Test
    void execute_ShouldSendTheDownloadWithItsVideoAndChannel() {
        Job job = jobs.addDownloading(video.id());

        push.execute(job.id());

        assertThat(sent).hasSize(1);
        assertThat(sent.get(0).download().jobId()).isEqualTo(job.id());
        assertThat(sent.get(0).video()).isEqualTo(video);
        assertThat(sent.get(0).channel()).isEqualTo(channel);
    }

    @Test
    void execute_WithoutConnectedClients_ShouldSendNothing() {
        hasClients = false;

        push.execute(jobs.addDownloading(video.id()).id());

        assertThat(sent).isEmpty();
    }

    @Test
    void execute_ForADownloadThatIsGone_ShouldSendNothing() {
        push.execute(UUID.randomUUID());

        assertThat(sent).isEmpty();
    }
}
