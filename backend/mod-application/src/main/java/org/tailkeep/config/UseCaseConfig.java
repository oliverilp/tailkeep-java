package org.tailkeep.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.tailkeep.domain.shared.UseCase;

import java.time.Clock;

/**
 * Registers the use cases of {@code mod-domain}, which has no Spring and so no stereotype annotations.
 * Every class marked {@link UseCase} becomes a bean, and its ports are injected by constructor.
 */
@Configuration
@ComponentScan(
        basePackages = "org.tailkeep.domain",
        useDefaultFilters = false,
        includeFilters = @Filter(type = FilterType.ANNOTATION, classes = UseCase.class))
public class UseCaseConfig {

    /** The use cases ask this for the time, so tests can set it. */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
