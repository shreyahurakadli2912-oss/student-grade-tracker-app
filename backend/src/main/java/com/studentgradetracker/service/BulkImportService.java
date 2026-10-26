package com.studentgradetracker.service;

import com.studentgradetracker.dto.BulkImportConfirmRequest;
import com.studentgradetracker.dto.BulkImportPreviewResponse;
import com.studentgradetracker.dto.BulkImportResultResponse;
import com.studentgradetracker.dto.BulkImportRowPreview;
import com.studentgradetracker.entity.Student;
import com.studentgradetracker.exception.BadRequestException;
import com.studentgradetracker.repository.StudentRepository;
import com.studentgradetracker.util.CsvHelper;
import com.studentgradetracker.util.ExcelHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class BulkImportService {

    private final StudentRepository studentRepository;
    private final GradeCalculationService gradeCalculationService;

    public BulkImportService(StudentRepository studentRepository, GradeCalculationService gradeCalculationService) {
        this.studentRepository = studentRepository;
        this.gradeCalculationService = gradeCalculationService;
    }

    public BulkImportPreviewResponse previewImportFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty. Please select a valid CSV or Excel file.");
        }

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        List<String[]> rawRows;
        String fileType;

        if (originalName.endsWith(".csv")) {
            fileType = "CSV";
            try {
                rawRows = CsvHelper.parseCsv(file.getInputStream());
            } catch (IOException e) {
                throw new BadRequestException("Could not read uploaded CSV: " + e.getMessage());
            }
        } else if (originalName.endsWith(".xlsx") || originalName.endsWith(".xls")) {
            fileType = "EXCEL";
            try {
                rawRows = ExcelHelper.parseExcel(file.getInputStream());
            } catch (IOException e) {
                throw new BadRequestException("Could not read uploaded Excel file: " + e.getMessage());
            }
        } else {
            throw new BadRequestException("Unsupported file type. Please upload a .csv or .xlsx file.");
        }

        if (rawRows.isEmpty()) {
            throw new BadRequestException("Uploaded file contains no rows.");
        }

        // Parse Header
        String[] header = rawRows.get(0);
        int studentIdIdx = -1;
        int nameIdx = -1;
        int classIdx = -1;
        int marksIdx = -1;

        for (int i = 0; i < header.length; i++) {
            String col = header[i].trim().toLowerCase().replaceAll("[\\s_-]", "");
            if (col.equals("studentid") || col.equals("rollno") || col.equals("id")) {
                studentIdIdx = i;
            } else if (col.equals("name") || col.equals("fullname") || col.equals("studentname")) {
                nameIdx = i;
            } else if (col.equals("classname") || col.equals("class") || col.equals("section")) {
                classIdx = i;
            } else if (col.equals("marks") || col.equals("score") || col.equals("mark")) {
                marksIdx = i;
            }
        }

        List<String> missingCols = new ArrayList<>();
        if (studentIdIdx == -1) missingCols.add("studentId");
        if (nameIdx == -1) missingCols.add("name");
        if (classIdx == -1) missingCols.add("className");
        if (marksIdx == -1) missingCols.add("marks");

        if (!missingCols.isEmpty()) {
            throw new BadRequestException("Missing required column headers: " + String.join(", ", missingCols) +
                    ". Expected headers: studentId, name, className, marks.");
        }

        // Validate Rows
        List<BulkImportRowPreview> previewRows = new ArrayList<>();
        Set<String> seenStudentIdsInFile = new HashSet<>();
        int validCount = 0;
        int dupCount = 0;
        int invalidCount = 0;

        for (int r = 1; r < rawRows.size(); r++) {
            String[] row = rawRows.get(r);
            int rowNumber = r + 1; // 1-based row number

            String studentId = row.length > studentIdIdx ? row[studentIdIdx].trim() : "";
            String name = row.length > nameIdx ? row[nameIdx].trim() : "";
            String className = row.length > classIdx ? row[classIdx].trim() : "";
            String marksStr = row.length > marksIdx ? row[marksIdx].trim() : "";

            BulkImportRowPreview rowPreview = new BulkImportRowPreview(rowNumber, studentId, name, className, null);
            boolean rowValid = true;

            // Student ID validation
            if (studentId.isEmpty()) {
                rowPreview.addError("Row " + rowNumber + ": Student ID is missing.");
                rowValid = false;
            } else {
                String normalizedId = studentId.toUpperCase();
                if (seenStudentIdsInFile.contains(normalizedId)) {
                    rowPreview.addError("Row " + rowNumber + ": Duplicate Student ID '" + studentId + "' within this file.");
                    rowPreview.setStatus("DUPLICATE_IN_FILE");
                    rowValid = false;
                } else {
                    seenStudentIdsInFile.add(normalizedId);
                    if (studentRepository.existsByStudentId(studentId)) {
                        rowPreview.addError("Row " + rowNumber + ": Student ID '" + studentId + "' already exists in database.");
                        rowPreview.setStatus("DUPLICATE_IN_DB");
                        rowValid = false;
                    }
                }
            }

            // Name validation
            if (name.isEmpty()) {
                rowPreview.addError("Row " + rowNumber + ": Student Name is missing.");
                rowValid = false;
            }

            // Class validation
            if (className.isEmpty()) {
                rowPreview.addError("Row " + rowNumber + ": Class Name is missing.");
                rowValid = false;
            }

            // Marks validation
            if (marksStr.isEmpty()) {
                rowPreview.addError("Row " + rowNumber + ": Marks are missing.");
                rowValid = false;
            } else {
                try {
                    double marks = Double.parseDouble(marksStr);
                    if (marks < 0.0 || marks > 100.0) {
                        rowPreview.addError("Row " + rowNumber + ": Marks must be between 0 and 100 (got " + marks + ").");
                        rowValid = false;
                    } else {
                        double roundedMarks = GradeCalculationService.roundTwoDecimals(marks);
                        rowPreview.setMarks(roundedMarks);
                        rowPreview.setPercentage(roundedMarks);
                        rowPreview.setGrade(gradeCalculationService.calculateGrade(roundedMarks));
                    }
                } catch (NumberFormatException e) {
                    rowPreview.addError("Row " + rowNumber + ": Marks must be numeric (got '" + marksStr + "').");
                    rowValid = false;
                }
            }

            if (rowValid) {
                rowPreview.setStatus("VALID");
                validCount++;
            } else {
                if (rowPreview.getStatus() == null) {
                    rowPreview.setStatus("INVALID");
                    invalidCount++;
                } else if (rowPreview.getStatus().startsWith("DUPLICATE")) {
                    dupCount++;
                } else {
                    invalidCount++;
                }
            }

            previewRows.add(rowPreview);
        }

        BulkImportPreviewResponse response = new BulkImportPreviewResponse();
        response.setFileName(file.getOriginalFilename());
        response.setFileType(fileType);
        response.setTotalRows(previewRows.size());
        response.setValidRows(validCount);
        response.setDuplicateRows(dupCount);
        response.setInvalidRows(invalidCount);
        response.setRows(previewRows);

        return response;
    }

    @Transactional
    public BulkImportResultResponse confirmImport(BulkImportConfirmRequest request) {
        if (request == null || request.getRows() == null || request.getRows().isEmpty()) {
            throw new BadRequestException("No rows provided for import.");
        }

        List<Student> studentsToSave = new ArrayList<>();
        List<BulkImportRowPreview> rejectedRows = new ArrayList<>();
        int importedCount = 0;
        int duplicateCount = 0;
        int rejectedCount = 0;

        for (BulkImportRowPreview row : request.getRows()) {
            if ("VALID".equalsIgnoreCase(row.getStatus())) {
                // Double-check DB uniqueness before persisting
                if (studentRepository.existsByStudentId(row.getStudentId().trim())) {
                    row.setStatus("DUPLICATE_IN_DB");
                    row.addError("Student ID already exists in database.");
                    rejectedRows.add(row);
                    duplicateCount++;
                } else {
                    Student student = new Student(
                            row.getStudentId().trim(),
                            row.getName().trim(),
                            row.getClassName().trim(),
                            row.getMarks()
                    );
                    studentsToSave.add(student);
                    importedCount++;
                }
            } else {
                if (row.getStatus() != null && row.getStatus().startsWith("DUPLICATE")) {
                    duplicateCount++;
                } else {
                    rejectedCount++;
                }
                rejectedRows.add(row);
            }
        }

        if (!studentsToSave.isEmpty()) {
            studentRepository.saveAll(studentsToSave);
        }

        String msg = String.format("Successfully imported %d student(s). %d duplicate(s), %d rejected row(s).",
                importedCount, duplicateCount, rejectedCount);

        BulkImportResultResponse result = new BulkImportResultResponse(
                request.getRows().size(),
                importedCount,
                duplicateCount,
                rejectedCount,
                msg
        );
        result.setRejectedRows(rejectedRows);
        return result;
    }
}
