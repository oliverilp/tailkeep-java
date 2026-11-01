package org.tailkeep.adapter.database.channel;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tailkeep.domain.channel.Channel;
import org.tailkeep.domain.channel.FindChannel;
import org.tailkeep.domain.channel.SaveChannel;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Transactional
public class ChannelDatabaseAdapter implements FindChannel, SaveChannel {
    private final ChannelJpaRepository repository;

    public ChannelDatabaseAdapter(ChannelJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Channel> byId(UUID id) {
        return repository.findById(id).map(ChannelEntityMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Channel> byYoutubeId(String youtubeId) {
        return repository.findByYoutubeId(youtubeId).map(ChannelEntityMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Channel> byIds(Collection<UUID> ids) {
        return repository.findAllById(ids).stream().map(ChannelEntityMapper::toDomain).toList();
    }

    @Override
    public Channel save(Channel channel) {
        return ChannelEntityMapper.toDomain(repository.saveAndFlush(ChannelEntityMapper.toEntity(channel)));
    }
}
