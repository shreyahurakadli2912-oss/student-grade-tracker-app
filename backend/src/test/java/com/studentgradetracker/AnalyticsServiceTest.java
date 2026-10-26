package com.studentgradetracker;

import com.studentgradetracker.dto.ClassStatisticsResponse;
import com.studentgradetracker.entity.Student;
import com.studentgradetracker.repository.GradeScaleRuleRepository;
import com.studentgradetracker.repository.StudentRepository;
import com.studentgradetracker.service.AnalyticsService;
import com.studentgradetracker.service.GradeCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private GradeScaleRuleRepository gradeScaleRuleRepository;

    private GradeCalculationService gradeCalculationService;
    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        lenient().when(gradeScaleRuleRepository.findAllByOrderBySortOrderAsc()).thenReturn(Collections.emptyList());
        gradeCalculationService = new GradeCalculationService(gradeScaleRuleRepository);
        analyticsService = new AnalyticsService(studentRepository, gradeCalculationService);
    }

    @Test
    @DisplayName("Test 13: Empty class statistics - avoids division by zero")
    void testEmptyClassStatistics() {
        when(studentRepository.findAll()).thenReturn(Collections.emptyList());

        ClassStatisticsResponse stats = analyticsService.getClassStatistics("ALL");

        assertNotNull(stats);
        assertEquals(0, stats.getTotalStudents());
        assertEquals(0.0, stats.getAveragePercentage());
        assertEquals(0.0, stats.getHighestMarks());
        assertEquals(0.0, stats.getLowestMarks());
        assertEquals(0.0, stats.getPassPercentage());
        assertEquals(0, stats.getTotalPassed());
        assertEquals(0, stats.getTotalFailed());
    }

    @Test
    @DisplayName("Test 9, 10, 11: Phase 17 Five Students - Average, Highest, Lowest, Distribution")
    void testFiveStudentsStatistics() {
        List<Student> fiveStudents = List.of(
                new Student("S101", "Alice Johnson", "10A", 94.5),
                new Student("S102", "Bob Smith", "10A", 82.0),
                new Student("S103", "Charlie Brown", "10A", 74.0),
                new Student("S104", "David Miller", "10A", 65.5),
                new Student("S105", "Eva Green", "10A", 52.0)
        );

        when(studentRepository.findByClassNameIgnoreCase("10A")).thenReturn(fiveStudents);

        ClassStatisticsResponse stats = analyticsService.getClassStatistics("10A");

        // Total: 5
        assertEquals(5, stats.getTotalStudents());

        // Average: (94.5 + 82 + 74 + 65.5 + 52) / 5 = 368 / 5 = 73.60%
        assertEquals(73.60, stats.getAveragePercentage());

        // Highest: 94.50
        assertEquals(94.50, stats.getHighestMarks());
        assertTrue(stats.getHighestScoringStudents().get(0).contains("Alice Johnson"));

        // Lowest: 52.00
        assertEquals(52.00, stats.getLowestMarks());
        assertTrue(stats.getLowestScoringStudents().get(0).contains("Eva Green"));

        // Passed (>=60%): S101(A), S102(B), S103(C), S104(D) -> 4 passed
        assertEquals(4, stats.getTotalPassed());

        // Failed (<60%): S105(F) -> 1 failed
        assertEquals(1, stats.getTotalFailed());

        // Pass rate: 4/5 * 100 = 80.00%
        assertEquals(80.00, stats.getPassPercentage());

        // Exactly one in each grade: A, B, C, D, F
        assertEquals(1L, stats.getGradeDistribution().get("A"));
        assertEquals(1L, stats.getGradeDistribution().get("B"));
        assertEquals(1L, stats.getGradeDistribution().get("C"));
        assertEquals(1L, stats.getGradeDistribution().get("D"));
        assertEquals(1L, stats.getGradeDistribution().get("F"));
    }
}
