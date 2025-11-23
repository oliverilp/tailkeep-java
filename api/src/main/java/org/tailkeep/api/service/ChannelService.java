package org.tailkeep.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tailkeep.api.dto.ChannelDetailDto;
import org.tailkeep.api.dto.ChannelWithVideosDto;
import org.tailkeep.api.dto.VideoDto;
import org.tailkeep.api.exception.ResourceNotFoundException;
import org.tailkeep.api.mapper.EntityMapper;
import org.tailkeep.api.model.Channel;
import org.tailkeep.api.repository.ChannelRepository;
import org.tailkeep.api.repository.VideoRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChannelService {
    private final ChannelRepository channelRepository;
    private final VideoRepository videoRepository;
    private final EntityMapper mapper;

    @Transactional(readOnly = true)
    public Page<ChannelDetailDto> getAllChannels(Pageable pageable) {
        return channelRepository.findAll(pageable)
            .map(mapper::toDetailDto);
    }

    @Transactional(readOnly = true)
    public ChannelWithVideosDto getChannelWithVideos(UUID id, Pageable pageable) {
        Channel channel = channelRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Channel", id.toString()));

        Pageable videoPageable = PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<VideoDto> videos = videoRepository.findByChannel(channel, videoPageable)
            .map(mapper::toDto);

        return ChannelWithVideosDto.builder()
            .id(channel.getId())
            .name(channel.getName())
            .youtubeId(channel.getYoutubeId())
            .channelUrl(channel.getChannelUrl())
            .createdAt(channel.getCreatedAt())
            .updatedAt(channel.getUpdatedAt())
            .videos(videos)
            .build();
    }
}
