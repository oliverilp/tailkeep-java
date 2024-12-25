CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    nickname VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE jwt_secret (
    id UUID PRIMARY KEY,
    secret_key VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE channel (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    youtube_id VARCHAR(255) NOT NULL UNIQUE,
    channel_url VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE video (
    id UUID PRIMARY KEY,
    youtube_id VARCHAR(255) NOT NULL UNIQUE,
    channel_id UUID NOT NULL REFERENCES channel(id),
    url VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    duration_string VARCHAR(50),
    duration DOUBLE PRECISION,
    thumbnail_url VARCHAR(255),
    description TEXT,
    view_count BIGINT,
    comment_count BIGINT,
    filename VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE job (
    id UUID PRIMARY KEY,
    input_url VARCHAR(255) NOT NULL,
    video_id UUID REFERENCES video(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE download_progress (
    job_id UUID PRIMARY KEY REFERENCES job(id),
    video_id UUID NOT NULL REFERENCES video(id),
    status VARCHAR(50),
    has_ended BOOLEAN NOT NULL DEFAULT FALSE,
    progress DOUBLE PRECISION DEFAULT 0.0,
    size VARCHAR(50),
    speed VARCHAR(50),
    eta VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

CREATE TABLE token (
    id UUID PRIMARY KEY,
    token VARCHAR(255) NOT NULL,
    token_type VARCHAR(50) NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    expired BOOLEAN NOT NULL DEFAULT FALSE,
    user_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_token_user FOREIGN KEY (user_id) REFERENCES app_user(id)
);

-- Create indexes for foreign keys and frequently queried columns
CREATE INDEX idx_video_channel_id ON video(channel_id);
CREATE INDEX idx_download_progress_video_id ON download_progress(video_id);
CREATE INDEX idx_job_video_id ON job(video_id);
CREATE INDEX idx_video_youtube_id ON video(youtube_id);
CREATE INDEX idx_channel_youtube_id ON channel(youtube_id);
CREATE INDEX idx_user_username ON app_user(username); 
