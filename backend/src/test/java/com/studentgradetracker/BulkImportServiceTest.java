package com.studentgradetracker;

import com.studentgradetracker.dto.BulkImportConfirmRequest;
import com.studentgradetracker.dto.BulkImportPreviewResponse;
import com.studentgradetracker.dto.BulkImportResultResponse;
import com.studentgradetracker.repository.GradeScaleRuleRepository;
import com.studentgradetracker.repository.StudentRepository;
import com.studentgradetracker.service.BulkImportService;
import com.studentgradetracker.service.GradeCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkImportServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private GradeScaleRuleRepository gradeScaleRuleRepository;

    private BulkImportService bulkImportService;

    @BeforeEach
    void setUp() {
        lenient().when(gradeScaleRuleRepository.findAllByOrderBySortOrderAsc()).thenReturn(Collections.emptyList());
        GradeCalculationService gradeCalculationService = new GradeCalculationService(gradeScaleRuleRepository);
        bulkImportService = new BulkImportService(studentRepository, gradeCalculationService);
    }

    @Test
    @DisplayName("Test 14 & 17: CSV Import Preview with valid and duplicate rows")
    void testCsvImportPreviewWithDuplicatesAndErrors() {
        String csvContent = "studentId,name,className,marks\n" +
                "S101,Alice Johnson,10A,94.5\n" +
                "S102,Bob Smith,10A,82\n" +
                "S101,Duplicate Alice,10A,90\n" + // duplicate in file
                "S104,Invalid Marks,10A,150\n" +  // invalid marks > 100
                "S105,Non Numeric,10A,abc\n";     // non-numeric marks

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "students.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        when(studentRepository.existsByStudentId("S101")).thenReturn(false);
        when(studentRepository.existsByStudentId("S102")).thenReturn(false);

        BulkImportPreviewResponse preview = bulkImportService.previewImportFile(file);

        assertNotNull(preview);
        assertEquals(5, preview.getTotalRows());
        assertEquals(2, preview.getValidRows());
        assertEquals(1, preview.getDuplicateRows());
        assertEquals(2, preview.getInvalidRows());

        // Check row 1
        assertEquals("VALID", preview.getRows().get(0).getStatus());
        assertEquals(94.5, preview.getRows().get(0).getMarks());
        assertEquals("A", preview.getRows().get(0).getGrade());

        // Check row 3 (duplicate in file)
        assertEquals("DUPLICATE_IN_FILE", preview.getRows().get(2).getStatus());

        // Check row 4 (invalid marks > 100)
        assertEquals("INVALID", preview.getRows().get(3).getStatus());

        // Check row 5 (non-numeric)
        assertEquals("INVALID", preview.getRows().get(4).getStatus());
    }

    @Test
    @DisplayName("Test 18: Confirm import persists valid rows and rejects invalid ones")
    void testConfirmImport() {
        String csvContent = "studentId,name,className,marks\n" +
                "S101,Alice Johnson,10A,94.5\n" +
                "S102,Bob Smith,10A,82\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "students.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        when(studentRepository.existsByStudentId(anyString())).thenReturn(false);

        BulkImportPreviewResponse preview = bulkImportService.previewImportFile(file);

        BulkImportConfirmRequest confirmReq = new BulkImportConfirmRequest(preview.getRows(), true);
        BulkImportResultResponse result = bulkImportService.confirmImport(confirmReq);

        assertNotNull(result);
        assertEquals(2, result.getTotalRows());
        assertEquals(2, result.getImportedCount());
        assertEquals(0, result.getRejectedCount());
        verify(studentRepository, times(1)).saveAll(anyList());
    }
}
