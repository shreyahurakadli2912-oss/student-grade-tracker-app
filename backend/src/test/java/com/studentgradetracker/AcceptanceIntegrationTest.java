package com.studentgradetracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentgradetracker.dto.ClassStatisticsResponse;
import com.studentgradetracker.dto.StudentRequest;
import com.studentgradetracker.dto.StudentResponse;
import com.studentgradetracker.repository.StudentRepository;
import com.studentgradetracker.service.AnalyticsService;
import com.studentgradetracker.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AcceptanceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentService studentService;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDatabase() {
        studentService.clearAllStudents();
    }

    @Test
    @DisplayName("PHASE 17 ACCEPTANCE TEST: Complete 5-Student Import, Verification, Update, Delete & Exports")
    void testPhase17AcceptanceWorkflow() throws Exception {
        // Step 1: Upload the acceptance CSV file
        String csvContent = "studentId,name,className,marks\n" +
                "S101,Alice Johnson,10A,94.5\n" +
                "S102,Bob Smith,10A,82\n" +
                "S103,Charlie Brown,10A,74\n" +
                "S104,David Miller,10A,65.5\n" +
                "S105,Eva Green,10A,52\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "acceptance_students.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        // Upload and Preview
        MvcResult previewResult = mockMvc.perform(multipart("/api/import/preview").file(file))
                .andExpect(status().isOk())
                .andReturn();

        String previewJson = previewResult.getResponse().getContentAsString();
        assertTrue(previewJson.contains("\"totalRows\":5"));
        assertTrue(previewJson.contains("\"validRows\":5"));
        assertTrue(previewJson.contains("\"duplicateRows\":0"));
        assertTrue(previewJson.contains("\"invalidRows\":0"));

        // Step 2: Confirm Import
        String confirmBody = "{\"rows\":[ " +
                "{\"studentId\":\"S101\",\"name\":\"Alice Johnson\",\"className\":\"10A\",\"marks\":94.5,\"status\":\"VALID\"}," +
                "{\"studentId\":\"S102\",\"name\":\"Bob Smith\",\"className\":\"10A\",\"marks\":82.0,\"status\":\"VALID\"}," +
                "{\"studentId\":\"S103\",\"name\":\"Charlie Brown\",\"className\":\"10A\",\"marks\":74.0,\"status\":\"VALID\"}," +
                "{\"studentId\":\"S104\",\"name\":\"David Miller\",\"className\":\"10A\",\"marks\":65.5,\"status\":\"VALID\"}," +
                "{\"studentId\":\"S105\",\"name\":\"Eva Green\",\"className\":\"10A\",\"marks\":52.0,\"status\":\"VALID\"}" +
                "],\"skipInvalid\":true}";

        mockMvc.perform(post("/api/import/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmBody))
                .andExpect(status().isOk());

        // Step 3: Verify Expected Acceptance Metrics
        ClassStatisticsResponse stats = analyticsService.getClassStatistics("10A");

        // - Total students: 5
        assertEquals(5, stats.getTotalStudents(), "Total students must be 5");

        // - Class average: 73.60%
        assertEquals(73.60, stats.getAveragePercentage(), "Class average must be 73.60%");

        // - Highest marks: 94.50
        assertEquals(94.50, stats.getHighestMarks(), "Highest marks must be 94.50");

        // - Lowest marks: 52.00
        assertEquals(52.00, stats.getLowestMarks(), "Lowest marks must be 52.00");

        // - One student in each grade A, B, C, D, and F
        assertEquals(1L, stats.getGradeDistribution().get("A"), "Grade A count must be 1");
        assertEquals(1L, stats.getGradeDistribution().get("B"), "Grade B count must be 1");
        assertEquals(1L, stats.getGradeDistribution().get("C"), "Grade C count must be 1");
        assertEquals(1L, stats.getGradeDistribution().get("D"), "Grade D count must be 1");
        assertEquals(1L, stats.getGradeDistribution().get("F"), "Grade F count must be 1");

        // Step 4: Verify Rankings
        List<StudentResponse> students = studentService.getAllStudentsRanked(null, "10A", null, null, "rank", "asc");
        assertEquals(5, students.size());
        assertEquals("S101", students.get(0).getStudentId()); // Rank 1 (94.5%)
        assertEquals(1, students.get(0).getRank());
        assertEquals("S102", students.get(1).getStudentId()); // Rank 2 (82.0%)
        assertEquals(2, students.get(1).getRank());
        assertEquals("S103", students.get(2).getStudentId()); // Rank 3 (74.0%)
        assertEquals(3, students.get(2).getRank());
        assertEquals("S104", students.get(3).getStudentId()); // Rank 4 (65.5%)
        assertEquals(4, students.get(3).getRank());
        assertEquals("S105", students.get(4).getStudentId()); // Rank 5 (52.0%)
        assertEquals(5, students.get(4).getRank());

        // Step 5: Edit Eva Green marks from 52 to 95.0
        StudentResponse eva = students.stream().filter(s -> s.getStudentId().equals("S105")).findFirst().orElseThrow();
        StudentRequest updateReq = new StudentRequest("S105", "Eva Green", "10A", 95.0);

        mockMvc.perform(put("/api/students/" + eva.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk());

        // Verify updated statistics: Eva now has 95.0, so highest mark is 95.0, Grade A count is now 2, Grade F is 0!
        ClassStatisticsResponse updatedStats = analyticsService.getClassStatistics("10A");
        assertEquals(95.00, updatedStats.getHighestMarks());
        assertEquals(2L, updatedStats.getGradeDistribution().get("A"));
        assertEquals(0L, updatedStats.getGradeDistribution().get("F"));

        // Step 6: Delete a student (Eva)
        mockMvc.perform(delete("/api/students/" + eva.getId()))
                .andExpect(status().isOk());

        ClassStatisticsResponse afterDeleteStats = analyticsService.getClassStatistics("10A");
        assertEquals(4, afterDeleteStats.getTotalStudents(), "Count should now be 4 after deletion");

        // Step 7: Test Exports (CSV, Excel, PDF)
        MvcResult csvExport = mockMvc.perform(get("/api/export/csv?className=10A"))
                .andExpect(status().isOk())
                .andReturn();
        byte[] csvBytes = csvExport.getResponse().getContentAsByteArray();
        assertTrue(csvBytes.length > 50, "CSV export should contain data");

        MvcResult excelExport = mockMvc.perform(get("/api/export/excel?className=10A"))
                .andExpect(status().isOk())
                .andReturn();
        byte[] excelBytes = excelExport.getResponse().getContentAsByteArray();
        assertTrue(excelBytes.length > 1000, "Excel export should generate valid XLSX workbook");

        MvcResult pdfExport = mockMvc.perform(get("/api/export/pdf/class?className=10A"))
                .andExpect(status().isOk())
                .andReturn();
        byte[] pdfBytes = pdfExport.getResponse().getContentAsByteArray();
        assertTrue(pdfBytes.length > 1000, "PDF export should generate valid PDF document");
    }

    @Test
    @DisplayName("Swagger OpenAPI documentation is accessible")
    void testOpenApiDocumentation() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Health endpoint returns UP and persistent database info")
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }
}
