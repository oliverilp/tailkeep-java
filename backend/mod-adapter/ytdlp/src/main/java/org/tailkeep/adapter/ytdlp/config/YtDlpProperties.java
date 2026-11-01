package org.tailkeep.adapter.ytdlp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Where yt-dlp runs and writes the downloaded files. */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.media")
public class YtDlpProperties {
    private String path = System.getProperty("user.home") + "/Videos/";
}
