package org.tailkeep.api.integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.tailkeep.api.dto.AuthenticationResponseDto;
import org.tailkeep.api.dto.DownloadProgressDto;
import org.tailkeep.api.dto.DownloadRequestDto;
import org.tailkeep.api.dto.DownloadsDashboardDto;
import org.tailkeep.api.exception.ApiError;
import org.tailkeep.api.model.user.Role;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RequiredArgsConstructor
class DownloadIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestDataFactory testDataFactory;

    @Test
    void startDownload_WithValidUrlAndAuth_ShouldAcceptRequest() {
        // Arrange
        AuthenticationResponseDto auth = testDataFactory.createTestUser("admin", "password12345", Role.ADMIN);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(auth.getAccessToken());

        DownloadRequestDto request = new DownloadRequestDto("https://www.youtube.com/watch?v=dQw4w9WgXcQ");

        // Act
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/downloads",
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                Void.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        // Verify job was created
        assertThat(jobRepository.count()).isEqualTo(1);
        assertThat(Objects.requireNonNull(jobRepository.findAll().getFirst().getInputUrl()))
                .contains("dQw4w9WgXcQ");
    }

    @Test
    void startDownload_WithoutLoggedIn_ShouldFail() {
        // Arrange
        DownloadRequestDto request = new DownloadRequestDto("https://www.youtube.com/watch?v=dQw4w9WgXcQ");

        // Act
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/v1/downloads",
                request,
                Void.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void startDownload_WithoutAdminPermission_ShouldFail() {
        // Arrange
        AuthenticationResponseDto auth = testDataFactory.createTestUser("user", "password12345", Role.USER);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(auth.getAccessToken());

        DownloadRequestDto request = new DownloadRequestDto("https://www.youtube.com/watch?v=dQw4w9WgXcQ");

        // Act
        ResponseEntity<ApiError> response = restTemplate.exchange(
                "/api/v1/downloads",
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                ApiError.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void startDownload_WithInvalidUrl_ShouldFail() {
        // Arrange
        AuthenticationResponseDto auth = testDataFactory.createTestUser("admin", "password12345678", Role.ADMIN);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(auth.getAccessToken());

        DownloadRequestDto request = new DownloadRequestDto("not-a-url");

        // Act
        ResponseEntity<ApiError> response = restTemplate.exchange(
                "/api/v1/downloads",
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                ApiError.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void startDownload_WithNonHttpsUrl_ShouldFail() {
        // Arrange
        AuthenticationResponseDto auth = testDataFactory.createTestUser("admin", "password12345", Role.ADMIN);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(auth.getAccessToken());

        DownloadRequestDto request = new DownloadRequestDto("http://example.com");

        // Act
        ResponseEntity<ApiError> response = restTemplate.exchange(
                "/api/v1/downloads",
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                ApiError.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .isNotNull()
                .satisfies(body -> assertThat(body.message()).isEqualTo("URL must use HTTPS protocol"));
    }

    @Test
    void startDownload_WithEmptyHost_ShouldFail() {
        // Arrange
        AuthenticationResponseDto auth = testDataFactory.createTestUser("admin", "password12345", Role.ADMIN);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(auth.getAccessToken());

        DownloadRequestDto request = new DownloadRequestDto("https:///path");

        // Act
        ResponseEntity<ApiError> response = restTemplate.exchange(
                "/api/v1/downloads",
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                ApiError.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .isNotNull()
                .satisfies(body -> assertThat(body.message()).isEqualTo("URL must have a valid host"));
    }

    @Test
    void getDownloadProgress_WithValidId_ShouldSucceed() {
        // Arrange
        AuthenticationResponseDto auth = testDataFactory.createTestUser("admin", "password12345", Role.ADMIN);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(auth.getAccessToken());

        TestEntities testData = testDataFactory.createCompleteTestData();
        String jobId = testData.job().getId();

        // Act
        ResponseEntity<DownloadProgressDto> response = restTemplate.exchange(
                "/api/v1/downloads/" + jobId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                DownloadProgressDto.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .satisfies(body -> {
                    assertThat(body.id()).isEqualTo(jobId);
                    assertThat(body.status()).isEqualTo("downloading");
                    assertThat(body.progress()).isEqualTo(0.0);
                    assertThat(body.hasEnded()).isFalse();
                    assertThat(body.size()).isEqualTo("10MB");
                    assertThat(body.speed()).isEqualTo("1MB/s");
                    assertThat(body.eta()).isEqualTo("00:10");
                });
    }

    @Test
    void getDownloadProgress_WithInvalidId_ShouldFail() {
        // Arrange
        AuthenticationResponseDto auth = testDataFactory.createTestUser("admin", "password12345", Role.ADMIN);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(auth.getAccessToken());

        testDataFactory.createCompleteTestData();

        // Act
        ResponseEntity<ApiError> response = restTemplate.exchange(
                "/api/v1/downloads/invalid-id",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ApiError.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getDashboard_ShouldReturnCorrectCounts() {
        // Arrange
        AuthenticationResponseDto auth = testDataFactory.createTestUser("admin", "password12345", Role.ADMIN);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(auth.getAccessToken());

        testDataFactory.createCompleteTestData();

        // Act
        ResponseEntity<DownloadsDashboardDto> response = restTemplate.exchange(
                "/api/v1/downloads/dashboard",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                DownloadsDashboardDto.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .satisfies(body -> {
                    assertThat(body.queueInfo().queue()).isEqualTo(0);
                    assertThat(body.downloads()).hasSize(1);
                });

    }

    @Test
    void getAllDownloadProgress_WithAuth_ShouldReturnEmptyList() {
        // Arrange
        AuthenticationResponseDto auth = testDataFactory.createTestUser("user", "password12345", Role.USER);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(auth.getAccessToken());

        // Act
        ResponseEntity<List<DownloadProgressDto>> response = restTemplate.exchange(
                "/api/v1/downloads",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                }
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .isEmpty();
    }

    @Test
    void getAllDownloadProgress_WithData_ShouldReturnList() {
        // Arrange
        AuthenticationResponseDto auth = testDataFactory.createTestUser("admin", "password12345", Role.ADMIN);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(auth.getAccessToken());

        // Create test data
        testDataFactory.createCompleteTestData(0);
        testDataFactory.createCompleteTestData(1);
        testDataFactory.createCompleteTestData(2);

        // Act
        ResponseEntity<List<DownloadProgressDto>> response = restTemplate.exchange(
                "/api/v1/downloads",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                }
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .hasSize(3)
                .extracting(DownloadProgressDto::status)
                .containsOnly("downloading");
    }
} 
