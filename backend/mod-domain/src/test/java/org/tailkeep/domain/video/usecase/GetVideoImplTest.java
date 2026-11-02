package org.tailkeep.domain.video.usecase;

import org.junit.jupiter.api.Test;
import org.tailkeep.domain.channel.Channel;
import org.tailkeep.domain.shared.ResourceNotFoundException;
import org.tailkeep.domain.video.model.Video;
import org.tailkeep.domain.video.model.VideoDetails;
import org.tailkeep.domain.video.model.VideoDownloadCount;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetVideoImplTest {
    private final FakeVideos videos = new FakeVideos();
    private final FakeChannels channels = new FakeChannels();
    private VideoDownloadCount downloads = new VideoDownloadCount(0, 0);
    private final GetVideo getVideo = new GetVideoImpl(videos, channels, videoId -> downloads);

    @Test
    void returnsTheVideoWithItsChannel() {
        Channel channel = channels.add("Channel 1");
        Video video = videos.add(channel.id(), "Video 1", LocalDateTime.of(2025, 1, 1, 12, 0));

        VideoDetails details = getVideo.execute(video.id());

        assertThat(details.video()).isEqualTo(video);
        assertThat(details.channel()).isEqualTo(channel);
    }

    @Test
    void failsWhenThereIsNoSuchVideo() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> getVideo.execute(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Video not found with identifier: " + id);
    }

    @Test
    void isNotDoneWithoutDownloads() {
        Video video = videos.add(channels.add("Channel 1").id(), "Video 1", LocalDateTime.now());
        downloads = new VideoDownloadCount(0, 0);

        assertThat(getVideo.execute(video.id()).doneDownloading()).isFalse();
    }

    @Test
    void isNotDoneWhileADownloadIsRunning() {
        Video video = videos.add(channels.add("Channel 1").id(), "Video 1", LocalDateTime.now());
        downloads = new VideoDownloadCount(2, 1);

        assertThat(getVideo.execute(video.id()).doneDownloading()).isFalse();
    }

    @Test
    void isDoneWhenEveryDownloadHasEnded() {
        Video video = videos.add(channels.add("Channel 1").id(), "Video 1", LocalDateTime.now());
        downloads = new VideoDownloadCount(2, 2);

        assertThat(getVideo.execute(video.id()).doneDownloading()).isTrue();
    }
}
