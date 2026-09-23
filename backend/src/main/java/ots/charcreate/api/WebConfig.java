package ots.charcreate.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Allows the dev frontend to call the API, per {@code specs/03-api.md}. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String corsOrigin;

    public WebConfig(@Value("${app.cors-origin}") String corsOrigin) {
        this.corsOrigin = corsOrigin;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOrigins(corsOrigin);
    }
}
