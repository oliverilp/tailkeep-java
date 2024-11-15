package org.tailkeep.api.controller;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tailkeep.api.dto.MetadataRequestMessage;
import org.tailkeep.api.service.DownloadService;

@RestController
@RequestMapping("api/v1/messages")
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class DownloadController {
    private final DownloadService downloadService;

    public DownloadController(DownloadService downloadService) {
        this.downloadService = downloadService;
    }

    @PostMapping
    public void publish(@RequestBody MetadataRequestMessage request) throws Exception {
        downloadService.processDownloadRequest(request);
    }
}
