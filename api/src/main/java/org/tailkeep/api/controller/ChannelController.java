package org.tailkeep.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tailkeep.api.dto.ChannelDetailDto;
import org.tailkeep.api.dto.ChannelWithVideosDto;
import org.tailkeep.api.service.ChannelService;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/channels")
@RequiredArgsConstructor
public class ChannelController {
    private final ChannelService channelService;

    @GetMapping
    public ResponseEntity<Page<ChannelDetailDto>> getAllChannels(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "name,asc") String sort
    ) {
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
        return ResponseEntity.ok(channelService.getAllChannels(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChannelWithVideosDto> getChannelById(
        @PathVariable UUID id,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(channelService.getChannelWithVideos(id, pageable));
    }
}

