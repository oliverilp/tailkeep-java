package org.tailkeep.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tailkeep.api.message.Metadata;
import org.tailkeep.domain.channel.Channel;
import org.tailkeep.domain.channel.FindChannel;
import org.tailkeep.domain.channel.SaveChannel;

import lombok.RequiredArgsConstructor;

// Legacy: replaced by the RecordMetadata use case in task 03
@Service
@RequiredArgsConstructor
public class ChannelService {
    private final FindChannel findChannel;
    private final SaveChannel saveChannel;

    @Transactional
    public Channel getOrCreateChannel(Metadata metadata) {
        return findChannel.byYoutubeId(metadata.channelId())
            .orElseGet(() -> saveChannel.save(Channel.builder()
                .youtubeId(metadata.channelId())
                .name(metadata.uploader())
                .channelUrl(metadata.channelUrl())
                .build()));
    }
}
