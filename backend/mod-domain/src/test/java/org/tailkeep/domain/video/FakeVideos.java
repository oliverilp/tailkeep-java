package org.tailkeep.domain.video;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** In-memory stand-in for the video ports. */
class FakeVideos implements FindVideo, SaveVideo {
    private final List<Video> videos = new ArrayList<>();

    @Override
    public Optional<Video> byId(UUID id) {
        return videos.stream().filter(video -> id.equals(video.id())).findFirst();
    }

    @Override
    public Optional<Video> byYoutubeId(String youtubeId) {
        return videos.stream().filter(video -> youtubeId.equals(video.youtubeId())).findFirst();
    }

    @Override
    public List<Video> newestFirst() {
        return videos.stream().sorted(Comparator.comparing(Video::createdAt).reversed()).toList();
    }

    @Override
    public Video save(Video video) {
        Video saved = video.id() == null ? video.toBuilder().id(UUID.randomUUID()).build() : video;
        videos.removeIf(existing -> saved.id().equals(existing.id()));
        videos.add(saved);
        return saved;
    }

    Video add(UUID channelId, String title, LocalDateTime createdAt) {
        return save(Video.builder()
                .youtubeId(title)
                .channelId(channelId)
                .url("https://www.youtube.com/watch?v=" + title)
                .title(title)
                .filename(title + ".mp4")
                .createdAt(createdAt)
                .build());
    }
}
