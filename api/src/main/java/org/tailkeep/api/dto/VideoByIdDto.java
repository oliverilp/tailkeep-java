package org.tailkeep.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class VideoByIdDto extends VideoDto {
    private boolean doneDownloading;
}
