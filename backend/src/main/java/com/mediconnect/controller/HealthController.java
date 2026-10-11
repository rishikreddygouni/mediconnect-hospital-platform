package com.mediconnect.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {
    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("service", "MediConnect Java API");
        try (Connection connection = dataSource.getConnection()) {
            result.put("ok", true);
            result.put("database", connection.isValid(2) ? "UP" : "DOWN");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("ok", false);
            result.put("database", "DOWN");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(result);
        }
    }
}
