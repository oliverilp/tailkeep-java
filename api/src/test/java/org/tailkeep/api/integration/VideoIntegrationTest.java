package org.tailkeep.api.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.tailkeep.api.dto.AuthenticationResponseDto;
import org.tailkeep.api.dto.VideoDto;
import org.tailkeep.api.exception.ApiError;
import org.tailkeep.api.model.user.Role;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VideoIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestDataFactory testDataFactory;

    @Test
    void getVideo_WithoutAuth_ShouldFail() {
        // Act
        ResponseEntity<ApiError> response = restTemplate.exchange(
                "/api/v1/videos/some-id",
                HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                ApiError.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getVideo_WithInvalidId_ShouldReturn404() {
        // Arrange
        AuthenticationResponseDto auth = testDataFactory.createTestUser("user", "password12345", Role.USER);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(auth.getAccessToken());

        // Act
        ResponseEntity<ApiError> response = restTemplate.exchange(
                "/api/v1/videos/invalid-id",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ApiError.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getVideo_WithValidId_ShouldReturnVideo() {
        // Arrange
        AuthenticationResponseDto auth = testDataFactory.createTestUser("user", "password12345", Role.USER);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(auth.getAccessToken());

        TestEntities testData = testDataFactory.createCompleteTestData();
        String videoId = testData.video().getId();

        // Act
        ResponseEntity<VideoDto> response = restTemplate.exchange(
                "/api/v1/videos/" + videoId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                VideoDto.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .satisfies(video -> {
                    assertThat(video.getId()).isEqualTo(videoId);
                    assertThat(video.getTitle()).isEqualTo("Test Video-0");
                    assertThat(video.getChannel().name()).isEqualTo("Test Channel-0");
                });
    }

    @Test
    void getAllVideos_WithoutAuth_ShouldFail() {
        // Act
        ResponseEntity<ApiError> response = restTemplate.exchange(
                "/api/v1/videos",
                HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                ApiError.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getAllVideos_WithAuth_ShouldReturnEmptyList() {
        // Arrange
        AuthenticationResponseDto auth = testDataFactory.createTestUser("user", "password12345", Role.USER);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(auth.getAccessToken());

        // Act
        ResponseEntity<List<VideoDto>> response = restTemplate.exchange(
                "/api/v1/videos",
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
    void getAllVideos_WithData_ShouldReturnList() {
        // Arrange
        AuthenticationResponseDto auth = testDataFactory.createTestUser("user", "password12345", Role.USER);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(auth.getAccessToken());

        // Create test data
        TestEntities data1 = testDataFactory.createCompleteTestData(1);
        TestEntities data2 = testDataFactory.createCompleteTestData(2);
        TestEntities data3 = testDataFactory.createCompleteTestData(3);

        // Act
        ResponseEntity<List<VideoDto>> response = restTemplate.exchange(
                "/api/v1/videos",
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
            .satisfies(videos -> {
                assertThat(videos)
                    .extracting(VideoDto::getId)
                    .containsExactlyInAnyOrder(
                        data1.video().getId(),
                        data2.video().getId(),
                        data3.video().getId()
                    );
                
                assertThat(videos)
                    .extracting(VideoDto::getTitle)
                    .containsExactlyInAnyOrder(
                        "Test Video-1",
                        "Test Video-2",
                        "Test Video-3"
                    );
            });
    }
} 
