package com.studentgradetracker.controller;

import com.studentgradetracker.dto.BulkImportConfirmRequest;
import com.studentgradetracker.dto.BulkImportPreviewResponse;
import com.studentgradetracker.dto.BulkImportResultResponse;
import com.studentgradetracker.dto.BulkImportRowPreview;
import com.studentgradetracker.service.BulkImportService;
import com.studentgradetracker.util.CsvHelper;
import com.studentgradetracker.util.ExcelHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/import")
@Tag(name = "Bulk Import", description = "Endpoints for uploading, previewing, and importing student records via CSV and Excel")
public class BulkImportController {

    private final BulkImportService bulkImportService;

    public BulkImportController(BulkImportService bulkImportService) {
        this.bulkImportService = bulkImportService;
    }

    @PostMapping(value = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload and preview student file (CSV or Excel .xlsx) without saving")
    public ResponseEntity<BulkImportPreviewResponse> previewImport(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(bulkImportService.previewImportFile(file));
    }

    @PostMapping("/confirm")
    @Operation(summary = "Confirm and persist valid student rows into database")
    public ResponseEntity<BulkImportResultResponse> confirmImport(@RequestBody BulkImportConfirmRequest request) {
        return ResponseEntity.ok(bulkImportService.confirmImport(request));
    }

    @GetMapping("/template/csv")
    @Operation(summary = "Download sample CSV template for bulk student upload")
    public ResponseEntity<byte[]> downloadCsvTemplate() {
        byte[] data = CsvHelper.generateStudentSampleCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"students_import_template.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }

    @GetMapping("/template/excel")
    @Operation(summary = "Download sample Excel (.xlsx) template for bulk student upload")
    public ResponseEntity<byte[]> downloadExcelTemplate() {
        byte[] data = ExcelHelper.generateStudentSampleExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"students_import_template.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @PostMapping("/rejected-report")
    @Operation(summary = "Download CSV error report for rejected/failed import rows")
    public ResponseEntity<byte[]> downloadRejectedRowsReport(@RequestBody List<BulkImportRowPreview> rejectedRows) {
        byte[] data = CsvHelper.generateRejectedRowsCsv(rejectedRows);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"import_rejected_errors.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }
}
