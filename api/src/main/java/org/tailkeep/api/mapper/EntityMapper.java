package org.tailkeep.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.tailkeep.api.dto.*;
import org.tailkeep.api.model.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EntityMapper {
    ChannelDto toDto(Channel channel);
    VideoDto toDto(Video video);
    JobDto toDto(Job job);
    DownloadProgressDto toDto(DownloadProgress downloadProgress);

    Channel toEntity(ChannelDto dto);
    Video toEntity(VideoDto dto);
    Job toEntity(JobDto dto);
    DownloadProgress toEntity(DownloadProgressDto dto);
} 
