package org.tailkeep.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.tailkeep.api.service.SseService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/sse")
@RequiredArgsConstructor
public class SseController {
    private final SseService sseService;

    @GetMapping("/subscribe")
    public SseEmitter subscribe() {
        return sseService.createEmitter();
    }
} 
