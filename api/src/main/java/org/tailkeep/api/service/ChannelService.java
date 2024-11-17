package org.tailkeep.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tailkeep.api.message.Metadata;
import org.tailkeep.api.model.Channel;
import org.tailkeep.api.repository.ChannelRepository;

@Service
public class ChannelService {
    private final ChannelRepository channelRepository;

    public ChannelService(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    @Transactional
    public Channel getOrCreateChannel(Metadata metadata) {
        return channelRepository.findByYoutubeId(metadata.channelId())
            .orElseGet(() -> {
                Channel newChannel = new Channel();
                newChannel.setYoutubeId(metadata.channelId());
                newChannel.setName(metadata.uploader());
                newChannel.setChannelUrl(metadata.channelUrl());
                return channelRepository.save(newChannel);
            });
    }
}
