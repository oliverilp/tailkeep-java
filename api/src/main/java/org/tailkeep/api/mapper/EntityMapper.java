package org.tailkeep.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.tailkeep.api.dto.*;
import org.tailkeep.api.model.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EntityMapper {
    ChannelDto toDto(Channel channel);
    VideoDto toDto(Video video);
    JobDto toDto(Job job);
    DownloadProgressDto toDto(DownloadProgress downloadProgress);

    @Mapping(target = "doneDownloading", ignore = true)
    VideoByIdDto toDetailedDto(Video video);

    @Mapping(target = "videoCount", expression = "java(channel.getVideos() != null ? (long) channel.getVideos().size() : 0L)")
    ChannelDetailDto toDetailDto(Channel channel);

    Channel toEntity(ChannelDto dto);
    Video toEntity(VideoDto dto);
    Job toEntity(JobDto dto);
    DownloadProgress toEntity(DownloadProgressDto dto);
} 
