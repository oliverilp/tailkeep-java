package org.tailkeep.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tailkeep.api.dto.DownloadProgressDto;
import org.tailkeep.api.service.DownloadService;
import org.tailkeep.api.dto.DownloadRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.tailkeep.api.dto.DownloadsDashboardDto;
import org.tailkeep.api.dto.PageRequestDto;

@Validated
@RestController
@RequestMapping("api/v1/downloads")
@RequiredArgsConstructor
public class DownloadController {
    private final DownloadService downloadService;

    @PostMapping
    public ResponseEntity<Void> startDownload(@Valid @RequestBody DownloadRequestDto request) {
        downloadService.validateAndStartDownload(request);
        return ResponseEntity.accepted().build();
    }

    @GetMapping
    public ResponseEntity<List<DownloadProgressDto>> getAllDownloadProgress() {
        return ResponseEntity.ok(downloadService.getAllDownloadProgress());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DownloadProgressDto> getDownloadProgressById(@PathVariable UUID id) {
        return ResponseEntity.ok(downloadService.getDownloadProgressById(id));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DownloadsDashboardDto> getDownloadsDashboard(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "all") String progress
    ) {
        return ResponseEntity.ok(downloadService.getDownloadsDashboard(new PageRequestDto(page, size, progress)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteDownload(@PathVariable UUID id) {
        downloadService.softDeleteDownload(id);
        return ResponseEntity.noContent().build();
    }
}
