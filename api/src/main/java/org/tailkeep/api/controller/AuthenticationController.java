package org.tailkeep.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tailkeep.api.dto.AuthenticationRequestDto;
import org.tailkeep.api.dto.AuthenticationResponseDto;
import org.tailkeep.api.dto.RegisterRequestDto;
import org.tailkeep.api.service.AuthenticationService;

import java.io.IOException;
import java.util.Arrays;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;
    private final Environment env;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponseDto> register(@RequestBody RegisterRequestDto request) {
        // Only allow registration in development
        if (!Arrays.asList(env.getActiveProfiles()).contains("dev")) {
            throw new UnsupportedOperationException("Registration is disabled in production");
        }
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponseDto> authenticate(
            @RequestBody AuthenticationRequestDto request) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/refresh-token")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        service.refreshToken(request, response);
    }
}
