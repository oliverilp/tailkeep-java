package org.tailkeep.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class VideoDto {
    private UUID id;
    private String youtubeId;
    private ChannelDto channel;
    private String url;
    private String title;
    private String durationString;
    private Double duration;
    private String thumbnailUrl;
    private String description;
    private Long viewCount;
    private Long commentCount;
    private String filename;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
