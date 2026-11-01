package org.tailkeep.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tailkeep.adapter.security.user.InitialUserSeeder;
import org.tailkeep.support.Api.Response;

import static org.assertj.core.api.Assertions.assertThat;

/** Every test starts with empty tables, so running the seeder here is what the first boot does. */
class UserSeedingIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private InitialUserSeeder seeder;

    @Test
    void seedsAnAdminWhoCanLogInAndStartDownloads() {
        seeder.seed();

        assertThat(db.count("app_user")).isEqualTo(1);
        String token = authenticate("admin", "Admin1Admin1").get("access_token").asText();
        // Past the permission check, so refused by the URL rules and not with a 403
        assertThat(api.post("/api/v1/downloads", "{\"url\":\"not-a-url\"}", token).status()).isEqualTo(400);
    }

    @Test
    void seedsARegularDemoUserInDemoMode() {
        System.setProperty("DEMO_MODE", "true");

        seeder.seed();

        assertThat(db.count("app_user")).isEqualTo(1);
        String token = authenticate("demo", "Demo1Demo1").get("access_token").asText();
        assertThat(api.post("/api/v1/downloads", "{\"url\":\"not-a-url\"}", token).status()).isEqualTo(403);
    }

    @Test
    void addsNobodyWhenThereAreUsers() {
        db.insertUser("someone", "hash", "USER");

        seeder.seed();

        assertThat(db.count("app_user")).isEqualTo(1);
        Response response = api.post("/api/v1/auth/authenticate",
                "{\"username\":\"admin\",\"password\":\"Admin1Admin1\"}", null);
        assertThat(response.status()).isEqualTo(401);
    }
}
