package org.tailkeep.api.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.tailkeep.api.dto.AuthenticationResponseDto;
import org.tailkeep.api.dto.DownloadRequestDto;
import org.tailkeep.api.model.user.Role;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DownloadIntegrationTest extends BaseIntegrationTest {

  @Test
  void startDownload_WithValidUrlAndAuth_ShouldAcceptRequest() {
      // Arrange
      AuthenticationResponseDto auth = createTestUser("admin", "password12345", Role.ADMIN);

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
   void startDownload_WithoutAuth_ShouldFail() {
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
   void startDownload_WithInvalidUrl_ShouldFail() {
       // Arrange
       AuthenticationResponseDto auth = createTestUser("admin", "password12345678", Role.ADMIN);

       HttpHeaders headers = new HttpHeaders();
       headers.setBearerAuth(auth.getAccessToken());

       DownloadRequestDto request = new DownloadRequestDto("not-a-url");

       // Act
       ResponseEntity<Void> response = restTemplate.exchange(
               "/api/v1/downloads",
               HttpMethod.POST,
               new HttpEntity<>(request, headers),
               Void.class
       );

       // Assert
       assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
   }
} 
