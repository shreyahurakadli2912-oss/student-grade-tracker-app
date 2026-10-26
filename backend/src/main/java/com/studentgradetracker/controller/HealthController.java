package com.studentgradetracker.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@Tag(name = "System", description = "Endpoints for system health and diagnostics")
public class HealthController {

    private final LocalDateTime startedAt = LocalDateTime.now();

    @Autowired(required = false)
    private DataSource dataSource;

    @Autowired
    private Environment environment;

    @GetMapping({"/api/health", "/health"})
    @Operation(summary = "System health and database status check")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "Student Grade Management System");
        status.put("version", "1.0.0");
        status.put("startedAt", startedAt.toString());
        status.put("timestamp", LocalDateTime.now().toString());

        // Dynamic database detection
        String activeProfile = environment.getProperty("spring.profiles.active", "default");
        String dsUrl = environment.getProperty("spring.datasource.url", "");
        String cloudDbUrl = System.getenv("DATABASE_URL");

        if (cloudDbUrl != null && !cloudDbUrl.isBlank()) {
            status.put("database", "PostgreSQL (Managed Cloud Database)");
            status.put("persistenceMode", "Managed Cloud Storage");
        } else if (dsUrl.contains("postgresql")) {
            status.put("database", "PostgreSQL Database Engine");
            status.put("persistenceMode", "PostgreSQL Persistent Storage");
        } else if (dsUrl.contains("h2:mem")) {
            status.put("database", "H2 In-Memory Engine (Test Isolation)");
            status.put("persistenceMode", "In-Memory Test Isolation");
        } else {
            status.put("database", "H2 Persistent File-based Engine");
            status.put("persistenceMode", "Disk-backed file (./data/studentgradedb)");
        }

        // Check live DB connectivity
        boolean dbConnected = false;
        if (dataSource != null) {
            try (Connection conn = dataSource.getConnection()) {
                dbConnected = conn.isValid(2);
            } catch (Exception ignored) {
                dbConnected = false;
            }
        }
        status.put("databaseConnected", dbConnected);
        status.put("profile", activeProfile);

        return ResponseEntity.ok(status);
    }
}
