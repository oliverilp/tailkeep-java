package org.tailkeep.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.tailkeep.adapter.database.download.DownloadProgressEntity;
import org.tailkeep.adapter.database.download.JobEntity;
import org.tailkeep.adapter.database.video.VideoEntity;
import org.tailkeep.adapter.database.video.VideoEntityMapper;
import org.tailkeep.adapter.web.video.VideoResponse;
import org.tailkeep.adapter.web.video.VideoResponseMapper;
import org.tailkeep.api.dto.DownloadProgressDto;
import org.tailkeep.api.dto.JobDto;
import org.tailkeep.adapter.database.channel.ChannelEntityMapper;

// Legacy: goes away with the download migration (task 03). Videos no longer use MapStruct.
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EntityMapper {
    JobDto toDto(JobEntity job);
    DownloadProgressDto toDto(DownloadProgressEntity downloadProgress);

    default VideoResponse toVideoResponse(VideoEntity video) {
        if (video == null) {
            return null;
        }
        return VideoResponseMapper.toResponse(
                VideoEntityMapper.toDomain(video),
                ChannelEntityMapper.toDomain(video.getChannel()));
    }
}
