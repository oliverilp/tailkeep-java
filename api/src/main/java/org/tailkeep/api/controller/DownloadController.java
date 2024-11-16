package org.tailkeep.api.controller;

import java.util.List;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tailkeep.api.dto.MetadataRequestMessage;
import org.tailkeep.api.model.DownloadProgress;
import org.tailkeep.api.service.DownloadService;

@RestController
@RequestMapping("api/v1/downloads")
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class DownloadController {
    private final DownloadService downloadService;

    public DownloadController(DownloadService downloadService) {
        this.downloadService = downloadService;
    }

    @PostMapping
    public void processDownload(@RequestBody MetadataRequestMessage request) throws Exception {
        downloadService.processDownloadRequest(request);
    }

    @GetMapping
    public ResponseEntity<List<DownloadProgress>> getAllDownloadProgress() {
        return ResponseEntity.ok(downloadService.getAllDownloadProgress());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DownloadProgress> getDownloadProgressById(@PathVariable("id") String id) {
        return ResponseEntity.ok(downloadService.getDownloadProgressById(id));
    }
}
