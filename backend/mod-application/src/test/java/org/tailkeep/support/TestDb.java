package org.tailkeep.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Resets and seeds the database with SQL. The table layout is frozen by the refactor,
 * unlike the entity classes, so these tests survive moving the entities.
 */
@Component
public class TestDb {
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2025, 1, 1, 12, 0);

    public record Download(UUID channelId, UUID videoId, UUID jobId, String youtubeId, String title,
            String channelName, String channelYoutubeId) {
    }

    @Autowired
    private JdbcTemplate jdbc;

    public void clean() {
        jdbc.execute("TRUNCATE download_progress, job, video, channel, token, app_user CASCADE");
    }

    public UUID insertUser(String username, String passwordHash, String role) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO app_user (id, nickname, username, password, role) VALUES (?, ?, ?, ?, ?)",
                id, "Test User", username, passwordHash, role);
        return id;
    }

    public void deleteUser(String username) {
        jdbc.update("DELETE FROM token WHERE user_id = (SELECT id FROM app_user WHERE username = ?)", username);
        jdbc.update("DELETE FROM app_user WHERE username = ?", username);
    }

    public UUID insertChannel(String name, String youtubeId) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO channel (id, name, youtube_id, channel_url) VALUES (?, ?, ?, ?)",
                id, name, youtubeId, "https://www.youtube.com/channel/" + youtubeId);
        return id;
    }

    public UUID insertVideo(UUID channelId, String youtubeId, String title, LocalDateTime createdAt) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO video (id, youtube_id, channel_id, url, title, duration_string, duration,
                    thumbnail_url, description, view_count, comment_count, filename, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, '03:32', 212.0, ?, 'A description', 1000, 50, ?, ?, ?)
                """,
                id, youtubeId, channelId, "https://www.youtube.com/watch?v=" + youtubeId, title,
                "https://img.example.com/" + youtubeId + ".jpg", youtubeId + ".mp4",
                Timestamp.valueOf(createdAt), Timestamp.valueOf(createdAt));
        return id;
    }

    public UUID insertJob(UUID videoId, String inputUrl) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO job (id, input_url, video_id) VALUES (?, ?, ?)", id, inputUrl, videoId);
        return id;
    }

    public void insertProgress(UUID jobId, UUID videoId, String status, boolean hasEnded, double progress,
            String size, String speed, String eta, LocalDateTime createdAt, LocalDateTime completedAt) {
        jdbc.update("""
                INSERT INTO download_progress (job_id, video_id, status, has_ended, progress, size, speed, eta,
                    created_at, updated_at, completed_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                jobId, videoId, status, hasEnded, progress, size, speed, eta,
                Timestamp.valueOf(createdAt), Timestamp.valueOf(createdAt),
                completedAt == null ? null : Timestamp.valueOf(completedAt));
    }

    /** A job that is still downloading. A higher {@code n} is created later. */
    public Download downloading(int n) {
        return download(n, "downloading", false, 42.5, "10.00MiB", "1.00MiB/s", "00:10", null);
    }

    /** A job that has finished. A higher {@code n} is created later. */
    public Download finished(int n) {
        LocalDateTime createdAt = BASE_TIME.plusMinutes(n);
        return download(n, "done", true, 100.0, "10.00MiB", "0B/s", "00:00", createdAt.plusSeconds(30));
    }

    private Download download(int n, String status, boolean hasEnded, double progress, String size, String speed,
            String eta, LocalDateTime completedAt) {
        LocalDateTime createdAt = BASE_TIME.plusMinutes(n);
        String channelYoutubeId = "UCchannel" + n;
        String youtubeId = "video" + n;
        String channelName = "Channel " + n;
        String title = "Video " + n;
        UUID channelId = insertChannel(channelName, channelYoutubeId);
        UUID videoId = insertVideo(channelId, youtubeId, title, createdAt);
        UUID jobId = insertJob(videoId, "https://www.youtube.com/watch?v=" + youtubeId);
        insertProgress(jobId, videoId, status, hasEnded, progress, size, speed, eta, createdAt, completedAt);
        return new Download(channelId, videoId, jobId, youtubeId, title, channelName, channelYoutubeId);
    }

    /** A job that was just submitted: it has no video and no progress yet. */
    public UUID queuedJob(String inputUrl) {
        return insertJob(null, inputUrl);
    }

    public UUID onlyJobId() {
        return jdbc.queryForObject("SELECT id FROM job", UUID.class);
    }

    public long count(String table) {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Long.class);
        return count == null ? 0 : count;
    }
}
