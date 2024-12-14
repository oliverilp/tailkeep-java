package org.tailkeep.api.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.tailkeep.api.dto.AuthenticationRequestDto;
import org.tailkeep.api.dto.AuthenticationResponseDto;
import org.tailkeep.api.dto.ChangePasswordRequestDto;
import org.tailkeep.api.exception.ApiError;
import org.tailkeep.api.model.user.Role;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestDataFactory testDataFactory;

    @Test
    void changePassword_WithoutAuth_ShouldFail() {
        // Arrange
        ChangePasswordRequestDto request = new ChangePasswordRequestDto(
            "oldPassword",
            "newPassword",
            "newPassword"
        );

        // Act
        ResponseEntity<ApiError> response = restTemplate.exchange(
            "/api/v1/users",
            HttpMethod.PATCH,
            new HttpEntity<>(request, new HttpHeaders()),
            ApiError.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void changePassword_WithIncorrectCurrentPassword_ShouldFail() {
        // Arrange
        AuthenticationResponseDto auth = testDataFactory.createTestUser("user", "password12345", Role.USER);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(auth.getAccessToken());

        ChangePasswordRequestDto request = new ChangePasswordRequestDto(
            "wrongPassword",
            "newPassword12345",
            "newPassword12345"
        );

        // Act
        ResponseEntity<ApiError> response = restTemplate.exchange(
            "/api/v1/users",
            HttpMethod.PATCH,
            new HttpEntity<>(request, headers),
            ApiError.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
            .isNotNull()
            .satisfies(error -> 
                assertThat(error.message()).contains("Current password is incorrect")
            );
    }

    @Test
    void changePassword_WithMismatchedNewPasswords_ShouldFail() {
        // Arrange
        AuthenticationResponseDto auth = testDataFactory.createTestUser("user", "password12345", Role.USER);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(auth.getAccessToken());

        ChangePasswordRequestDto request = new ChangePasswordRequestDto(
            "password12345",
            "newPassword12345",
            "differentPassword12345"
        );

        // Act
        ResponseEntity<ApiError> response = restTemplate.exchange(
            "/api/v1/users",
            HttpMethod.PATCH,
            new HttpEntity<>(request, headers),
            ApiError.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
            .isNotNull()
            .satisfies(error -> 
                assertThat(error.message()).contains("New passwords do not match")
            );
    }

    @Test
    void changePassword_WithTooShortNewPassword_ShouldFail() {
        // Arrange
        AuthenticationResponseDto auth = testDataFactory.createTestUser("user", "password12345", Role.USER);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(auth.getAccessToken());

        ChangePasswordRequestDto request = new ChangePasswordRequestDto(
            "password12345",
            "short",
            "short"
        );

        // Act
        ResponseEntity<ApiError> response = restTemplate.exchange(
            "/api/v1/users",
            HttpMethod.PATCH,
            new HttpEntity<>(request, headers),
            ApiError.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
            .isNotNull()
            .satisfies(error -> 
                assertThat(error.message()).contains("Password must be between 10 and 100 characters long")
            );
    }

    @Test
    void changePassword_WithValidRequest_ShouldSucceed() {
        // Arrange
        AuthenticationResponseDto auth = testDataFactory.createTestUser("user", "password12345", Role.USER);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(auth.getAccessToken());

        ChangePasswordRequestDto request = new ChangePasswordRequestDto(
            "password12345",
            "newPassword12345",
            "newPassword12345"
        );

        // Act
        ResponseEntity<Void> response = restTemplate.exchange(
            "/api/v1/users",
            HttpMethod.PATCH,
            new HttpEntity<>(request, headers),
            Void.class
        );

        ResponseEntity<AuthenticationResponseDto> loginResponse = restTemplate.postForEntity(
            "/api/v1/auth/authenticate",
            new AuthenticationRequestDto("user", "newPassword12345"),
            AuthenticationResponseDto.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).isNotNull();
    }

    @Test
    void changePassword_InDemoMode_WithNonAdminUser_ShouldFail() {
        // Set demo mode for this test
        System.setProperty("DEMO_MODE", "true");

        // Arrange
        AuthenticationResponseDto auth = testDataFactory.createTestUser("user", "password12345", Role.USER);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(auth.getAccessToken());

        ChangePasswordRequestDto request = new ChangePasswordRequestDto(
            "password12345",
            "newPassword12345",
            "newPassword12345"
        );

        // Act
        ResponseEntity<ApiError> response = restTemplate.exchange(
            "/api/v1/users",
            HttpMethod.PATCH,
            new HttpEntity<>(request, headers),
            ApiError.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
            .isNotNull()
            .satisfies(error -> 
                assertThat(error.message()).contains("disabled in demo mode")
            );
    }
} 
