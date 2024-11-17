package org.tailkeep.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tailkeep.api.dto.DownloadProgressDto;
import org.tailkeep.api.service.DownloadService;

import java.util.List;

@RestController
@RequestMapping("/api/downloads")
public class DownloadProgressController {
    private final DownloadService downloadService;

    public DownloadProgressController(DownloadService downloadService) {
        this.downloadService = downloadService;
    }

    @GetMapping
    public ResponseEntity<List<DownloadProgressDto>> getAllDownloadProgress() {
        return ResponseEntity.ok(downloadService.getAllDownloadProgress());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DownloadProgressDto> getDownloadProgressById(@PathVariable String id) {
        return ResponseEntity.ok(downloadService.getDownloadProgressById(id));
    }
} 
