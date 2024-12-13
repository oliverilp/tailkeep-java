package org.tailkeep.api.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.tailkeep.api.dto.AuthenticationRequestDto;
import org.tailkeep.api.dto.AuthenticationResponseDto;
import org.tailkeep.api.dto.RegisterRequestDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthenticationIntegrationTest extends BaseIntegrationTest {

    @Test
    void register_WithValidCredentials_ShouldSucceed() {
        // Arrange
        RegisterRequestDto request = RegisterRequestDto.builder()
                .nickname("Test User")
                .username("testuser")
                .password("password12345")
                .build();

        // Act
        ResponseEntity<AuthenticationResponseDto> response = restTemplate.postForEntity(
                "/api/v1/auth/register",
                request,
                AuthenticationResponseDto.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isNotBlank();
        assertThat(response.getBody().getRefreshToken()).isNotBlank();
    }

    @Test
    void authenticate_WithValidCredentials_ShouldSucceed() {
        // Arrange
        createTestUser("testuser", "password12345");

        AuthenticationRequestDto request = AuthenticationRequestDto.builder()
                .username("testuser")
                .password("password12345")
                .build();

        // Act
        ResponseEntity<AuthenticationResponseDto> response = restTemplate.postForEntity(
                "/api/v1/auth/authenticate",
                request,
                AuthenticationResponseDto.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isNotBlank();
        assertThat(response.getBody().getRefreshToken()).isNotBlank();
    }

//     @Test
//     void authenticate_WithInvalidCredentials_ShouldFail() {
//         // Arrange
//         AuthenticationRequestDto request = AuthenticationRequestDto.builder()
//                 .username("nonexistent")
//                 .password("wrongpassword")
//                 .build();

//         // Act & Assert
//         HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
//             restTemplate.postForEntity(
//                     "/api/v1/auth/authenticate",
//                     request,
//                     AuthenticationResponseDto.class
//             );
//         });
        
//         assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
//     }
} 
