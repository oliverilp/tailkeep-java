package org.tailkeep.api.service;

import org.tailkeep.api.dto.AuthenticationRequestDto;
import org.tailkeep.api.dto.AuthenticationResponseDto;
import org.tailkeep.api.dto.RegisterRequestDto;
import org.tailkeep.api.exception.UnauthorizedException;
import org.tailkeep.api.exception.UsernameAlreadyExistsException;
import org.tailkeep.api.model.auth.Token;
import org.tailkeep.api.model.auth.TokenType;
import org.tailkeep.api.model.user.Role;
import org.tailkeep.api.model.user.User;
import org.tailkeep.api.repository.TokenRepository;
import org.tailkeep.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthenticationService {
    private final UserRepository repository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ValidationService validationService;

    // API endpoint - always creates USER role
    public AuthenticationResponseDto register(RegisterRequestDto request) {
        return registerWithRole(request, Role.USER);
    }

    // Internal method - allows role specification
    public AuthenticationResponseDto registerWithRole(RegisterRequestDto request, Role role) {
        validationService.validatePasswordLength(request.getPassword());

        if (repository.findByUsername(request.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }

        var user = User.builder()
                .nickname(request.getNickname())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();
        var savedUser = repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(savedUser, jwtToken);
        return AuthenticationResponseDto.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponseDto authenticate(AuthenticationRequestDto request) {
        // Since user registration is not public, we need to prevent username enumeration.
        // Returning immediately allows malicious actors to figure out valid usernames from response times,
        // allowing them to only focus on guessing passwords in brute-force attacks.
        // As a preventive measure, we will hash passwords even for invalid usernames.

        // First fetch the user - this ensures consistent timing whether username exists or not
        var user = repository.findByUsername(request.getUsername())
                .orElse(User.builder()
                        .password(passwordEncoder.encode("dummy"))
                        .build());
        
        try {
            // Always perform authentication, even for non-existent users
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));
            
            // If we get here, authentication was successful
            var jwtToken = jwtService.generateToken(user);
            var refreshToken = jwtService.generateRefreshToken(user);
            revokeAllUserTokens(user);
            saveUserToken(user, jwtToken);
            
            return AuthenticationResponseDto.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .build();
                    
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid credentials provided");
        }
    }

    public AuthenticationResponseDto refreshToken(String refreshToken) {
        if (refreshToken == null) {
            throw new UnauthorizedException("Missing refresh token");
        }

        final String userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail == null) {
            throw new UnauthorizedException("Invalid token");
        }

        var user = repository.findByUsername(userEmail)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new UnauthorizedException("Invalid token");
        }

        var accessToken = jwtService.generateToken(user);
        var newRefreshToken = jwtService.generateRefreshToken(user);

        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);

        return AuthenticationResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }
}
