package org.tailkeep.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.time.Duration;

@Configuration
public class SpaConfig implements WebMvcConfigurer {

    private static final String STATIC_LOCATION = "classpath:/static/";

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Every file name under assets/ contains a content hash
        registry.addResourceHandler("/assets/**")
            .addResourceLocations(STATIC_LOCATION + "assets/")
            .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable());
        registry.addResourceHandler("/index.html")
            .addResourceLocations(STATIC_LOCATION)
            .setCacheControl(CacheControl.noStore());
        // Files from web/public keep their names between builds
        registry.addResourceHandler("/*.*")
            .addResourceLocations(STATIC_LOCATION)
            .setCacheControl(CacheControl.noCache());
    }

    // A /** handler would answer other methods on unknown API paths with an
    // error instead of a 404, so routes that aren't the app must not match at all.
    @Bean
    public RouterFunction<ServerResponse> spaFallback() {
        Resource index = new ClassPathResource("static/index.html");

        return RouterFunctions.route(
            request -> index.exists() && isAppRoute(request),
            request -> ServerResponse.ok()
                .contentType(MediaType.TEXT_HTML)
                .cacheControl(CacheControl.noStore())
                .body(index)
        );
    }

    private static boolean isAppRoute(ServerRequest request) {
        HttpMethod method = request.method();
        if (!HttpMethod.GET.equals(method) && !HttpMethod.HEAD.equals(method)) {
            return false;
        }

        String path = request.path();
        boolean isApi = path.equals("/api") || path.startsWith("/api/");
        boolean isFile = path.substring(path.lastIndexOf('/') + 1).contains(".");

        return !isApi && !isFile;
    }
}
