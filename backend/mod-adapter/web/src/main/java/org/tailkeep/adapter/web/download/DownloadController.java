package org.tailkeep.adapter.web.download;

import jakarta.validation.Valid;
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
import org.tailkeep.domain.download.DeleteDownload;
import org.tailkeep.domain.download.DownloadFilter;
import org.tailkeep.domain.download.GetDownload;
import org.tailkeep.domain.download.GetDownloadsDashboard;
import org.tailkeep.domain.download.ListDownloads;
import org.tailkeep.domain.download.StartDownload;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("api/v1/downloads")
public class DownloadController {
    private final StartDownload startDownload;
    private final ListDownloads listDownloads;
    private final GetDownload getDownload;
    private final GetDownloadsDashboard getDownloadsDashboard;
    private final DeleteDownload deleteDownload;

    public DownloadController(
            StartDownload startDownload,
            ListDownloads listDownloads,
            GetDownload getDownload,
            GetDownloadsDashboard getDownloadsDashboard,
            DeleteDownload deleteDownload) {
        this.startDownload = startDownload;
        this.listDownloads = listDownloads;
        this.getDownload = getDownload;
        this.getDownloadsDashboard = getDownloadsDashboard;
        this.deleteDownload = deleteDownload;
    }

    @PostMapping
    public ResponseEntity<Void> startDownload(@Valid @RequestBody DownloadRequest request) {
        startDownload.execute(request.url());
        return ResponseEntity.accepted().build();
    }

    @GetMapping
    public ResponseEntity<List<DownloadResponse>> getAllDownloads() {
        return ResponseEntity.ok(listDownloads.execute().stream().map(DownloadResponseMapper::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DownloadResponse> getDownloadById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(DownloadResponseMapper.toResponse(getDownload.execute(id)));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "progress", defaultValue = "all") String progress) {
        return ResponseEntity.ok(DownloadResponseMapper.toDashboardResponse(
                getDownloadsDashboard.execute(filterOf(progress), page, size)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteDownload(@PathVariable("id") UUID id) {
        deleteDownload.execute(id);
        return ResponseEntity.noContent().build();
    }

    /** Anything but "active" and "done" lists every download. */
    private static DownloadFilter filterOf(String progress) {
        return switch (progress) {
            case "active" -> DownloadFilter.ACTIVE;
            case "done" -> DownloadFilter.DONE;
            default -> DownloadFilter.ALL;
        };
    }
}
