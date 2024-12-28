package org.tailkeep.worker.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.media")
public class MediaProperties {
    private String path = System.getProperty("user.home") + "/Videos/";
} 
