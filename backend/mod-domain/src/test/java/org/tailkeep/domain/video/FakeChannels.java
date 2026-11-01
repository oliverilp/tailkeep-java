package org.tailkeep.domain.video;

import org.tailkeep.domain.channel.Channel;
import org.tailkeep.domain.channel.FindChannel;
import org.tailkeep.domain.channel.SaveChannel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** In-memory stand-in for the channel ports. */
public class FakeChannels implements FindChannel, SaveChannel {
    private final List<Channel> channels = new ArrayList<>();

    @Override
    public Optional<Channel> byId(UUID id) {
        return channels.stream().filter(channel -> id.equals(channel.id())).findFirst();
    }

    @Override
    public Optional<Channel> byYoutubeId(String youtubeId) {
        return channels.stream().filter(channel -> youtubeId.equals(channel.youtubeId())).findFirst();
    }

    @Override
    public List<Channel> byIds(Collection<UUID> ids) {
        return channels.stream().filter(channel -> ids.contains(channel.id())).toList();
    }

    @Override
    public Channel save(Channel channel) {
        Channel saved = channel.id() == null ? channel.toBuilder().id(UUID.randomUUID()).build() : channel;
        channels.removeIf(existing -> saved.id().equals(existing.id()));
        channels.add(saved);
        return saved;
    }

    public Channel add(String name) {
        return save(Channel.builder()
                .youtubeId("UC" + name)
                .name(name)
                .channelUrl("https://www.youtube.com/channel/UC" + name)
                .build());
    }
}
