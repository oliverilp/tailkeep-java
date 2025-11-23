package org.tailkeep.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelWithVideosDto {
    private UUID id;
    private String name;
    private String youtubeId;
    private String channelUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Page<VideoDto> videos;
}

