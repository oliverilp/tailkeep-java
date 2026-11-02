package org.tailkeep.adapter.web.video;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tailkeep.domain.video.usecase.GetVideo;
import org.tailkeep.domain.video.usecase.ListVideos;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/videos")
public class VideoController {
    private final ListVideos listVideos;
    private final GetVideo getVideo;

    public VideoController(ListVideos listVideos, GetVideo getVideo) {
        this.listVideos = listVideos;
        this.getVideo = getVideo;
    }

    @GetMapping
    public ResponseEntity<List<VideoResponse>> getAllVideos() {
        return ResponseEntity.ok(listVideos.execute().stream().map(VideoResponseMapper::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VideoDetailResponse> getVideoById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(VideoResponseMapper.toDetailResponse(getVideo.execute(id)));
    }
}
