package com.studentgradetracker.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * Fallback resolver ensuring any direct client route refresh or direct URL access
 * returns the SPA index.html with HTTP 200 OK, while preserving standard 404
 * handling for API endpoints (/api/**), Swagger, Actuator, and static assets (.js, .css, etc.).
 */
@Configuration
public class SpaErrorViewResolver implements ErrorViewResolver {

    @Override
    public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
        if (status == HttpStatus.NOT_FOUND) {
            String path = request.getRequestURI();
            if (path != null &&
                    !path.startsWith("/api") &&
                    !path.startsWith("/actuator") &&
                    !path.startsWith("/swagger-ui") &&
                    !path.startsWith("/v3/api-docs") &&
                    !path.startsWith("/h2-console") &&
                    !path.equals("/health") &&
                    !path.contains(".")) {
                return new ModelAndView("forward:/index.html", HttpStatus.OK);
            }
        }
        return null;
    }
}
