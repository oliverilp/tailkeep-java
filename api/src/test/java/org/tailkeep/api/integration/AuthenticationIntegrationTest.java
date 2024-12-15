package org.tailkeep.api.integration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.tailkeep.api.dto.AuthenticationRequestDto;
import org.tailkeep.api.dto.AuthenticationResponseDto;
import org.tailkeep.api.dto.RegisterRequestDto;
import org.tailkeep.api.exception.ApiError;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

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

    @Test
    void refreshToken_WithValidToken_ShouldSucceed() {
        // Arrange
        AuthenticationResponseDto auth = testDataFactory.createTestUser("user", "password12345");
        Map<String, String> request = Map.of("refreshToken", auth.getRefreshToken());

        // Act
        ResponseEntity<AuthenticationResponseDto> response = restTemplate.postForEntity(
                "/api/v1/auth/refresh-token",
                request,
                AuthenticationResponseDto.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .satisfies(body -> {
                    assertThat(body.getAccessToken()).isNotBlank();
                    assertThat(body.getRefreshToken())
                            .isNotBlank()
                            .isNotEqualTo(auth.getRefreshToken()); // Should get new refresh token
                });
    }

    @Test
    void refreshToken_WithInvalidToken_ShouldFail() {
        // Arrange
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJhMDljZmE1Yy1iZDEzLTQwZGEtYjg2Yy03Njk0ZWNlZjZhM2YiLCJzdWIiOiJkZW1vIiwiaWF0IjoxNzM0MjA4OTc1LCJleHAiOjE3MzQyOTUzNzV9.SjQ77AIv02SsrFoS8SOs-YXbSfvt6ZTp1MuUsx_wxAQ";
        Map<String, String> request = Map.of("refreshToken", token);

        // Act
        ResponseEntity<ApiError> response = restTemplate.postForEntity(
                "/api/v1/auth/refresh-token",
                request,  // Send as JSON object
                ApiError.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody())
                .isNotNull()
                .satisfies(error -> 
                    assertThat(error.message()).contains("Invalid token")
                );
    }

    @Test
    void logout_WithValidToken_ShouldSucceed() {
        // Arrange
        AuthenticationResponseDto auth = testDataFactory.createTestUser("user", "password12345");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(auth.getAccessToken());

        // Act
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/auth/logout",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                Void.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify token is invalidated by trying to use it
        ResponseEntity<ApiError> verifyResponse = restTemplate.exchange(
                "/api/v1/users",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ApiError.class
        );
        assertThat(verifyResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void logout_WithoutAuth_ShouldFail() {
        // Act
        ResponseEntity<ApiError> response = restTemplate.exchange(
                "/api/v1/auth/logout",
                HttpMethod.POST,
                new HttpEntity<>(new HttpHeaders()),
                ApiError.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void logout_WithInvalidToken_ShouldFail() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("invalid-token");

        // Act
        ResponseEntity<ApiError> response = restTemplate.exchange(
                "/api/v1/auth/logout",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                ApiError.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
