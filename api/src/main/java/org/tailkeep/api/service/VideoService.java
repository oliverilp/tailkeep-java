package org.tailkeep.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tailkeep.api.message.Metadata;
import org.tailkeep.api.model.Channel;
import org.tailkeep.api.model.Video;
import org.tailkeep.api.repository.DownloadProgressRepository;
import org.tailkeep.api.repository.VideoRepository;
import org.tailkeep.api.dto.VideoByIdDto;
import org.tailkeep.api.dto.VideoDto;
import org.tailkeep.api.exception.ResourceNotFoundException;
import org.tailkeep.api.mapper.EntityMapper;
import org.springframework.data.domain.Sort;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoService {
    private final VideoRepository videoRepository;
    private final DownloadProgressRepository downloadProgressRepository;
    private final EntityMapper mapper;

    @Transactional
    public Video createOrUpdateVideo(Metadata metadata, Channel channel) {
        Video video = videoRepository.findByYoutubeId(metadata.youtubeId())
            .orElseGet(() -> {
                Video newVideo = new Video();
                newVideo.setYoutubeId(metadata.youtubeId());
                return newVideo;
            });

        // Update video fields
        video.setChannel(channel);
        video.setUrl(metadata.url());
        video.setTitle(metadata.title());
        video.setDurationString(metadata.durationString());
        video.setDuration(metadata.duration());
        video.setThumbnailUrl(metadata.thumbnailUrl());
        video.setDescription(metadata.description());
        video.setViewCount(metadata.viewCount());
        video.setCommentCount(metadata.commentCount());
        video.setFilename(metadata.filename());

        return videoRepository.save(video);
    }

    public List<VideoDto> getAllVideos() {
        return videoRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
            .stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }

     public VideoByIdDto getVideoById(String id) {
        Video video = videoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Video", id));

        VideoByIdDto dto = mapper.toDetailedDto(video);
        
        long totalProgressCount = downloadProgressRepository.countByVideo(video);
        long completedProgressCount = downloadProgressRepository.countByVideoAndHasEndedTrue(video);
        
        boolean doneDownloading = totalProgressCount > 0 && totalProgressCount == completedProgressCount;
        
        dto.setDoneDownloading(doneDownloading);
        
        return dto;
    }
}
