package com.summerproject2026.DentalWave.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Local-only readiness and graceful shutdown controls used by the USB scripts. */
@RestController
@Profile("portable")
@RequestMapping("/api/portable")
public class PortableControlController {

    private final ConfigurableApplicationContext applicationContext;
    private final String controlToken;

    public PortableControlController(
            ConfigurableApplicationContext applicationContext,
            @Value("${app.portable-control-token:}") String controlToken) {
        this.applicationContext = applicationContext;
        this.controlToken = controlToken;
    }

    @GetMapping("/status")
    public Map<String, String> status() {
        return Map.of("application", "DentalWave", "status", "ready");
    }

    @PostMapping("/shutdown")
    public ResponseEntity<Map<String, String>> shutdown(
            @RequestHeader(value = "X-DentalWave-Control", required = false) String suppliedToken) {
        if (!validToken(suppliedToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Shutdown authorization failed."));
        }

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(250);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
            applicationContext.close();
        });
        return ResponseEntity.ok(Map.of("message", "DentalWave is stopping."));
    }

    private boolean validToken(String suppliedToken) {
        if (controlToken == null || controlToken.isBlank()
                || suppliedToken == null || suppliedToken.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(
                controlToken.getBytes(StandardCharsets.UTF_8),
                suppliedToken.getBytes(StandardCharsets.UTF_8));
    }
}
