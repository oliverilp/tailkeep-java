package org.tailkeep.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tailkeep.api.dto.DownloadProgressDto;
import org.tailkeep.api.service.DownloadService;
import org.tailkeep.api.dto.DownloadRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
    public ResponseEntity<DownloadProgressDto> getDownloadProgressById(@PathVariable String id) {
        return ResponseEntity.ok(downloadService.getDownloadProgressById(id));
    }
}
