package com.studentgradetracker.controller;

import com.studentgradetracker.dto.ClassStatisticsResponse;
import com.studentgradetracker.dto.StudentResponse;
import com.studentgradetracker.service.AnalyticsService;
import com.studentgradetracker.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Endpoints for individual student report cards and comprehensive class reports")
public class ReportController {

    private final StudentService studentService;
    private final AnalyticsService analyticsService;

    public ReportController(StudentService studentService, AnalyticsService analyticsService) {
        this.studentService = studentService;
        this.analyticsService = analyticsService;
    }

    @GetMapping("/class")
    @Operation(summary = "Get comprehensive class report data with filtered students and metrics")
    public ResponseEntity<Map<String, Object>> getClassReport(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String status) {

        List<StudentResponse> students = studentService.getAllStudentsRanked(query, className, grade, status, "rank", "asc");
        ClassStatisticsResponse stats = analyticsService.getClassStatistics(className);

        Map<String, Object> result = new HashMap<>();
        result.put("students", students);
        result.put("statistics", stats);
        result.put("totalCount", students.size());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/student/{id}")
    @Operation(summary = "Get detailed report card data for an individual student")
    public ResponseEntity<StudentResponse> getStudentReport(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }
}
