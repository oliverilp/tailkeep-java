package org.tailkeep.api.integration;

import org.tailkeep.api.model.Channel;
import org.tailkeep.api.model.DownloadProgress;
import org.tailkeep.api.model.Job;
import org.tailkeep.api.model.Video;
import org.tailkeep.api.repository.ChannelRepository;
import org.tailkeep.api.repository.DownloadProgressRepository;
import org.tailkeep.api.repository.JobRepository;
import org.tailkeep.api.repository.VideoRepository;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TestDataFactory {
    private final ChannelRepository channelRepository;
    private final VideoRepository videoRepository;
    private final JobRepository jobRepository;
    private final DownloadProgressRepository downloadProgressRepository;

    @Transactional
    public Channel createTestChannel(String name, String youtubeId) {
        Channel channel = new Channel();
        channel.setId(UUID.randomUUID().toString());
        channel.setName(name);
        channel.setYoutubeId(youtubeId);
        channel.setChannelUrl("https://www.youtube.com/channel/" + youtubeId);
        return channelRepository.save(channel);
    }

    @Transactional
    public Video createTestVideo(Channel channel, String title, String youtubeId) {
        Video video = new Video();
        video.setId(UUID.randomUUID().toString());
        video.setFilename(youtubeId + ".mp4");
        video.setTitle(title);
        video.setUrl("https://www.youtube.com/watch?v=" + youtubeId);
        video.setYoutubeId(youtubeId);
        video.setChannel(channel);
        return videoRepository.save(video);
    }

    @Transactional
    public Job createTestJob(Video video, String inputUrl) {
        Job job = new Job();
        job.setInputUrl(inputUrl);
        job.setVideo(video);
        return jobRepository.save(job);
    }

    @Transactional
    public DownloadProgress createTestDownloadProgress(Job job, Video video, String status) {
        DownloadProgress progress = new DownloadProgress();
        progress.setId(job.getId());
        progress.setJob(job);
        progress.setVideo(video);
        progress.setStatus(status);
        progress.setProgress(0.0);
        progress.setHasEnded(false);
        progress.setSize("10MB");
        progress.setSpeed("1MB/s");
        progress.setEta("00:10");

        job.setDownloadProgress(progress);
        jobRepository.save(job);
        return downloadProgressRepository.save(progress);
    }

    @Transactional
    public TestEntities createCompleteTestData() {
        Channel channel = createTestChannel(
            "Test Channel",
            "UCuAXFkgsw1L7xaCfnd5JJOw"
        );

        Video video = createTestVideo(
            channel,
            "Test Video",
            "dQw4w9WgXcQ"
        );

        Job job = createTestJob(
            video,
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        );

        DownloadProgress progress = createTestDownloadProgress(
            job,
            video,
            "downloading"
        );

        return new TestEntities(channel, video, job, progress);
    }
}
