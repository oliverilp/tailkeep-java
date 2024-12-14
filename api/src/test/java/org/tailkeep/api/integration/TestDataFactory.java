package org.tailkeep.api.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tailkeep.api.dto.AuthenticationResponseDto;
import org.tailkeep.api.dto.RegisterRequestDto;
import org.tailkeep.api.model.Channel;
import org.tailkeep.api.model.DownloadProgress;
import org.tailkeep.api.model.Job;
import org.tailkeep.api.model.Video;
import org.tailkeep.api.model.user.Role;
import org.tailkeep.api.repository.ChannelRepository;
import org.tailkeep.api.repository.DownloadProgressRepository;
import org.tailkeep.api.repository.JobRepository;
import org.tailkeep.api.repository.VideoRepository;
import org.tailkeep.api.service.AuthenticationService;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TestDataFactory {
    private final ChannelRepository channelRepository;
    private final VideoRepository videoRepository;
    private final JobRepository jobRepository;
    private final DownloadProgressRepository downloadProgressRepository;
    private final AuthenticationService authenticationService;

    @Transactional
    public Channel createTestChannel(String name, String youtubeId, int counter) {
        String uniqueId = String.format("%s-%d", youtubeId, counter);
        Channel channel = new Channel();
        channel.setId(UUID.randomUUID().toString());
        channel.setName(name + "-" + counter);
        channel.setYoutubeId(uniqueId);
        channel.setChannelUrl("https://www.youtube.com/channel/" + uniqueId);
        return channelRepository.save(channel);
    }

    @Transactional
    public Video createTestVideo(Channel channel, String title, String youtubeId, int counter) {
        String uniqueId = String.format("%s-%d", youtubeId, counter);
        Video video = new Video();
        video.setId(UUID.randomUUID().toString());
        video.setFilename(uniqueId + ".mp4");
        video.setTitle(title + "-" + counter);
        video.setUrl("https://www.youtube.com/watch?v=" + uniqueId);
        video.setYoutubeId(uniqueId);
        video.setChannel(channel);
        return videoRepository.save(video);
    }

    @Transactional
    public Job createTestJob(Video video, String inputUrl, int counter) {
        String uniqueId = UUID.randomUUID().toString();
        Job job = new Job();
        job.setId(uniqueId);
        job.setInputUrl(inputUrl + "?id=" + uniqueId);
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
        return createCompleteTestData(0);
    }

    @Transactional
    public TestEntities createCompleteTestData(int counter) {
        Channel channel = createTestChannel(
                "Test Channel",
                "UCuAXFkgsw1L7xaCfnd5JJOw",
                counter
        );

        Video video = createTestVideo(
                channel,
                "Test Video",
                "dQw4w9WgXcQ",
                counter
        );

        Job job = createTestJob(
                video,
                "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                counter
        );

        DownloadProgress progress = createTestDownloadProgress(
                job,
                video,
                "downloading"
        );

        return new TestEntities(channel, video, job, progress);
    }

    @Transactional
    public AuthenticationResponseDto createTestUser(String username, String password) {
        return createTestUser(username, password, Role.USER);
    }

    @Transactional
    public AuthenticationResponseDto createTestUser(String username, String password, Role role) {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .nickname("Test User")
                .username(username)
                .password(password)
                .build();
        return authenticationService.registerWithRole(request, role);
    }
}
