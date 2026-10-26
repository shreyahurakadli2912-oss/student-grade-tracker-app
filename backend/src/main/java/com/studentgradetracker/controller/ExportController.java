package com.studentgradetracker.controller;

import com.studentgradetracker.dto.ClassStatisticsResponse;
import com.studentgradetracker.dto.StudentResponse;
import com.studentgradetracker.service.AnalyticsService;
import com.studentgradetracker.service.StudentService;
import com.studentgradetracker.util.CsvHelper;
import com.studentgradetracker.util.ExcelHelper;
import com.studentgradetracker.util.PdfReportGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/export")
@Tag(name = "Exports", description = "Endpoints for downloading CSV, Excel, and PDF reports")
public class ExportController {

    private final StudentService studentService;
    private final AnalyticsService analyticsService;

    public ExportController(StudentService studentService, AnalyticsService analyticsService) {
        this.studentService = studentService;
        this.analyticsService = analyticsService;
    }

    @GetMapping("/csv")
    @Operation(summary = "Export filtered student records to CSV")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String status) {

        List<StudentResponse> students = studentService.getAllStudentsRanked(query, className, grade, status, "rank", "asc");
        byte[] csv = CsvHelper.exportStudentsToCsv(students);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"students_grade_report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/excel")
    @Operation(summary = "Export filtered student records to Excel (.xlsx)")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String status) {

        List<StudentResponse> students = studentService.getAllStudentsRanked(query, className, grade, status, "rank", "asc");
        byte[] excel = ExcelHelper.exportStudentsToExcel(students, "Grade Report");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"students_grade_report.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @GetMapping("/pdf/class")
    @Operation(summary = "Export class report to professional PDF")
    public ResponseEntity<byte[]> exportClassPdf(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String status) {

        List<StudentResponse> students = studentService.getAllStudentsRanked(query, className, grade, status, "rank", "asc");
        ClassStatisticsResponse stats = analyticsService.getClassStatistics(className);
        byte[] pdf = PdfReportGenerator.generateClassReportPdf(students, stats, className);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"class_grade_report.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/pdf/student/{id}")
    @Operation(summary = "Export individual student report card to PDF")
    public ResponseEntity<byte[]> exportStudentPdf(@PathVariable Long id) {
        StudentResponse student = studentService.getStudentById(id);
        byte[] pdf = PdfReportGenerator.generateIndividualStudentPdf(student);

        String filename = "report_card_" + student.getStudentId() + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
