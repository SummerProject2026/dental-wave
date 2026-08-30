package com.summerproject2026.DentalWave.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Serves browser-only routes from the packaged React application.
 * API and static asset paths are intentionally not matched.
 */
@RestController
public class SpaForwardController {

    private final String indexHtml;

    public SpaForwardController() {
        ClassPathResource index = new ClassPathResource("static/index.html");
        try (InputStream input = index.getInputStream()) {
            this.indexHtml = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "The packaged frontend is missing static/index.html.", exception);
        }
    }

    @GetMapping(value = {
            "/",
            "/login",
            "/manager/{*path}"
    }, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> serveReact() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.TEXT_HTML)
                .body(indexHtml);
    }
}
