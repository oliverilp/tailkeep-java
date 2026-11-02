package org.tailkeep.domain.video.usecase;

import org.junit.jupiter.api.Test;
import org.tailkeep.domain.channel.Channel;
import org.tailkeep.domain.shared.ResourceNotFoundException;
import org.tailkeep.domain.video.model.Video;
import org.tailkeep.domain.video.model.VideoWithChannel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ListVideosImplTest {
    private static final LocalDateTime START = LocalDateTime.of(2025, 1, 1, 12, 0);

    private final FakeVideos videos = new FakeVideos();
    private final FakeChannels channels = new FakeChannels();
    private final ListVideos listVideos = new ListVideosImpl(videos, channels);

    @Test
    void returnsNothingWithoutVideos() {
        assertThat(listVideos.execute()).isEmpty();
    }

    @Test
    void returnsTheNewestVideoFirst() {
        Channel channel = channels.add("Channel 1");
        Video first = videos.add(channel.id(), "Video 1", START);
        Video second = videos.add(channel.id(), "Video 2", START.plusMinutes(1));
        Video third = videos.add(channel.id(), "Video 3", START.plusMinutes(2));

        List<VideoWithChannel> result = listVideos.execute();

        assertThat(result).extracting(VideoWithChannel::video).containsExactly(third, second, first);
    }

    @Test
    void attachesEachVideosOwnChannel() {
        Channel one = channels.add("Channel 1");
        Channel two = channels.add("Channel 2");
        Video fromOne = videos.add(one.id(), "Video 1", START);
        Video fromTwo = videos.add(two.id(), "Video 2", START.plusMinutes(1));

        List<VideoWithChannel> result = listVideos.execute();

        assertThat(result).containsExactly(
                new VideoWithChannel(fromTwo, two),
                new VideoWithChannel(fromOne, one));
    }

    @Test
    void failsWhenAVideosChannelIsMissing() {
        UUID missing = UUID.randomUUID();
        videos.add(missing, "Video 1", START);

        assertThatThrownBy(listVideos::execute)
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Channel not found with identifier: " + missing);
    }
}
