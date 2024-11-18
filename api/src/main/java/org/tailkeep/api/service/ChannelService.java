package org.tailkeep.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tailkeep.api.message.Metadata;
import org.tailkeep.api.model.Channel;
import org.tailkeep.api.repository.ChannelRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChannelService {
    private final ChannelRepository channelRepository;

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
