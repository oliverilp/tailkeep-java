package org.tailkeep.api.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.tailkeep.api.dto.AuthenticationRequestDto;
import org.tailkeep.api.dto.AuthenticationResponseDto;
import org.tailkeep.api.dto.RegisterRequestDto;
import org.tailkeep.api.exception.ApiError;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestDataFactory testDataFactory;

    @Test
    void register_WithValidCredentials_ShouldSucceed() {
        // Arrange
        RegisterRequestDto request = RegisterRequestDto.builder()
                .nickname("Test User")
                .username("testuser")
                .password("password12345")
                .build();

        // Act
        ResponseEntity<AuthenticationResponseDto> response = getRestTemplate().postForEntity(
                "/api/v1/auth/register",
                request,
                AuthenticationResponseDto.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .satisfies(body -> {
                    assertThat(body.getAccessToken()).isNotBlank();
                    assertThat(body.getRefreshToken()).isNotBlank();
                });
    }

    @Test
    void authenticate_WithValidCredentials_ShouldSucceed() {
        // Arrange
        testDataFactory.createTestUser("testuser", "password12345");

        AuthenticationRequestDto request = AuthenticationRequestDto.builder()
                .username("testuser")
                .password("password12345")
                .build();

        // Act
        ResponseEntity<AuthenticationResponseDto> response = getRestTemplate().postForEntity(
                "/api/v1/auth/authenticate",
                request,
                AuthenticationResponseDto.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .satisfies(body -> {
                    assertThat(body.getAccessToken()).isNotBlank();
                    assertThat(body.getRefreshToken()).isNotBlank();
                });
    }

    @Test
    void authenticate_WithInvalidCredentials_ShouldFail() {
        // Arrange
        AuthenticationRequestDto request = AuthenticationRequestDto.builder()
                .username("nonexistent")
                .password("wrongpassword")
                .build();

        // Act
        ResponseEntity<ApiError> response = getRestTemplate().postForEntity(
                "/api/v1/auth/authenticate",
                request,
                ApiError.class
        );

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody())
                .isNotNull()
                .extracting(ApiError::statusCode)
                .isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }
}
