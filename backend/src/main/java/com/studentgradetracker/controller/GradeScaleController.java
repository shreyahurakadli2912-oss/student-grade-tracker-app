package com.studentgradetracker.controller;

import com.studentgradetracker.dto.GradeScaleDto;
import com.studentgradetracker.service.GradeScaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grade-scale")
@Tag(name = "Grade Scale", description = "Endpoints for viewing and resetting letter grade boundaries")
public class GradeScaleController {

    private final GradeScaleService gradeScaleService;

    public GradeScaleController(GradeScaleService gradeScaleService) {
        this.gradeScaleService = gradeScaleService;
    }

    @GetMapping
    @Operation(summary = "Get active grade scale boundaries")
    public ResponseEntity<List<GradeScaleDto>> getGradeScale() {
        return ResponseEntity.ok(gradeScaleService.getAllRules());
    }

    @PostMapping("/reset")
    @Operation(summary = "Reset grade scale to standard default intervals (A: 90-100, B: 80-89.99, C: 70-79.99, D: 60-69.99, F: <60)")
    public ResponseEntity<List<GradeScaleDto>> resetGradeScale() {
        return ResponseEntity.ok(gradeScaleService.resetToDefaults());
    }
}
