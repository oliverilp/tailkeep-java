package org.tailkeep.api.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {
    
    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            // Clean the database (use with caution - only in dev/test)
            // flyway.clean(); // Uncomment only if you want to clean the DB first
            
            // Repair the metadata table if needed
            flyway.repair();
            
            // Run the migrations
            flyway.migrate();
        };
    }
} 
