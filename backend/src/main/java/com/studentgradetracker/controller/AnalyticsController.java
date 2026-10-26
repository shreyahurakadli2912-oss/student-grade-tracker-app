package com.studentgradetracker.controller;

import com.studentgradetracker.dto.ClassStatisticsResponse;
import com.studentgradetracker.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Endpoints for class statistics, grade distribution, pass/fail ratios, and subject benchmarks")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/statistics")
    @Operation(summary = "Calculate class statistics, averages, highest/lowest marks, and grade distributions")
    public ResponseEntity<ClassStatisticsResponse> getStatistics(
            @RequestParam(required = false, defaultValue = "ALL") String className) {
        return ResponseEntity.ok(analyticsService.getClassStatistics(className));
    }
}
