package com.studentgradetracker;

import com.studentgradetracker.dto.StudentResponse;
import com.studentgradetracker.entity.Student;
import com.studentgradetracker.repository.GradeScaleRuleRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GradeCalculationServiceTest {

    @Mock
    private GradeScaleRuleRepository gradeScaleRuleRepository;

    private GradeCalculationService service;

    @BeforeEach
    void setUp() {
        when(gradeScaleRuleRepository.findAllByOrderBySortOrderAsc()).thenReturn(Collections.emptyList());
        service = new GradeCalculationService(gradeScaleRuleRepository);
    }

    @Test
    @DisplayName("Test 8: Grade boundaries - A (90-100), B (80-89.99), C (70-79.99), D (60-69.99), F (<60)")
    void testGradeBoundaries() {
        assertEquals("A", service.calculateGrade(100.0));
        assertEquals("A", service.calculateGrade(94.5));
        assertEquals("A", service.calculateGrade(90.0));

        assertEquals("B", service.calculateGrade(89.99));
        assertEquals("B", service.calculateGrade(85.0));
        assertEquals("B", service.calculateGrade(80.0));

        assertEquals("C", service.calculateGrade(79.99));
        assertEquals("C", service.calculateGrade(74.0));
        assertEquals("C", service.calculateGrade(70.0));

        assertEquals("D", service.calculateGrade(69.99));
        assertEquals("D", service.calculateGrade(65.5));
        assertEquals("D", service.calculateGrade(60.0));

        assertEquals("F", service.calculateGrade(59.99));
        assertEquals("F", service.calculateGrade(52.0));
        assertEquals("F", service.calculateGrade(0.0));
    }

    @Test
    @DisplayName("Test Mandatory Example: 99, 99, 95, 90, 90, 85 -> 1, 1, 2, 3, 3, 4")
    void testMandatoryDenseRankingExample() {
        Student s1 = new Student("S1", "Alice", "10A", 99.0);
        Student s2 = new Student("S2", "Bob", "10A", 99.0);
        Student s3 = new Student("S3", "Charlie", "10A", 95.0);
        Student s4 = new Student("S4", "David", "10A", 90.0);
        Student s5 = new Student("S5", "Eva", "10A", 90.0);
        Student s6 = new Student("S6", "Frank", "10A", 85.0);

        List<StudentResponse> responses = List.of(
                service.mapToStudentResponse(s1),
                service.mapToStudentResponse(s2),
                service.mapToStudentResponse(s3),
                service.mapToStudentResponse(s4),
                service.mapToStudentResponse(s5),
                service.mapToStudentResponse(s6)
        );

        List<StudentResponse> ranked = service.assignDenseRankings(responses);

        assertEquals(6, ranked.size());

        // Alice (99) -> Rank 1
        assertEquals("Alice", ranked.get(0).getName());
        assertEquals(99.0, ranked.get(0).getMarks());
        assertEquals(1, ranked.get(0).getRank());

        // Bob (99) -> Rank 1 (Tied with Alice)
        assertEquals("Bob", ranked.get(1).getName());
        assertEquals(99.0, ranked.get(1).getMarks());
        assertEquals(1, ranked.get(1).getRank());

        // Charlie (95) -> Rank 2 (Next consecutive rank)
        assertEquals("Charlie", ranked.get(2).getName());
        assertEquals(95.0, ranked.get(2).getMarks());
        assertEquals(2, ranked.get(2).getRank());

        // David (90) -> Rank 3 (Next consecutive rank)
        assertEquals("David", ranked.get(3).getName());
        assertEquals(90.0, ranked.get(3).getMarks());
        assertEquals(3, ranked.get(3).getRank());

        // Eva (90) -> Rank 3 (Tied with David)
        assertEquals("Eva", ranked.get(4).getName());
        assertEquals(90.0, ranked.get(4).getMarks());
        assertEquals(3, ranked.get(4).getRank());

        // Frank (85) -> Rank 4 (Next consecutive rank)
        assertEquals("Frank", ranked.get(5).getName());
        assertEquals(85.0, ranked.get(5).getMarks());
        assertEquals(4, ranked.get(5).getRank());
    }

    @Test
    @DisplayName("Test Dense Ranking: 99, 99, 95, 90, 85 -> 1, 1, 2, 3, 4")
    void testDenseRankingFiveStudents_99_99_95_90_85() {
        Student s1 = new Student("S1", "Alice", "10A", 99.0);
        Student s2 = new Student("S2", "Bob", "10A", 99.0);
        Student s3 = new Student("S3", "Charlie", "10A", 95.0);
        Student s4 = new Student("S4", "David", "10A", 90.0);
        Student s5 = new Student("S5", "Frank", "10A", 85.0);

        List<StudentResponse> responses = List.of(
                service.mapToStudentResponse(s1),
                service.mapToStudentResponse(s2),
                service.mapToStudentResponse(s3),
                service.mapToStudentResponse(s4),
                service.mapToStudentResponse(s5)
        );

        List<StudentResponse> ranked = service.assignDenseRankings(responses);

        assertEquals(5, ranked.size());
        assertEquals("Alice", ranked.get(0).getName());
        assertEquals(99.0, ranked.get(0).getMarks());
        assertEquals(1, ranked.get(0).getRank());

        assertEquals("Bob", ranked.get(1).getName());
        assertEquals(99.0, ranked.get(1).getMarks());
        assertEquals(1, ranked.get(1).getRank()); // Tie with Alice

        assertEquals("Charlie", ranked.get(2).getName());
        assertEquals(95.0, ranked.get(2).getMarks());
        assertEquals(2, ranked.get(2).getRank()); // Consecutive 2

        assertEquals("David", ranked.get(3).getName());
        assertEquals(90.0, ranked.get(3).getMarks());
        assertEquals(3, ranked.get(3).getRank()); // Consecutive 3

        assertEquals("Frank", ranked.get(4).getName());
        assertEquals(85.0, ranked.get(4).getMarks());
        assertEquals(4, ranked.get(4).getRank()); // Consecutive 4
    }

    @Test
    @DisplayName("Test Additional Dense Ranking Examples: ties, all identical, and all distinct")
    void testAdditionalDenseRankingExamples() {
        // Example 1: 100, 95, 95, 80 -> 1, 2, 2, 3
        Student a1 = new Student("A1", "A", "10A", 100.0);
        Student a2 = new Student("A2", "B", "10A", 95.0);
        Student a3 = new Student("A3", "C", "10A", 95.0);
        Student a4 = new Student("A4", "D", "10A", 80.0);

        List<StudentResponse> ranked1 = service.assignDenseRankings(List.of(
                service.mapToStudentResponse(a1),
                service.mapToStudentResponse(a2),
                service.mapToStudentResponse(a3),
                service.mapToStudentResponse(a4)
        ));
        assertEquals(1, ranked1.get(0).getRank());
        assertEquals(2, ranked1.get(1).getRank());
        assertEquals(2, ranked1.get(2).getRank());
        assertEquals(3, ranked1.get(3).getRank());

        // Example 2: 90, 90, 90 -> 1, 1, 1 (All students identical receive rank 1)
        Student b1 = new Student("B1", "A", "10A", 90.0);
        Student b2 = new Student("B2", "B", "10A", 90.0);
        Student b3 = new Student("B3", "C", "10A", 90.0);

        List<StudentResponse> ranked2 = service.assignDenseRankings(List.of(
                service.mapToStudentResponse(b1),
                service.mapToStudentResponse(b2),
                service.mapToStudentResponse(b3)
        ));
        assertEquals(1, ranked2.get(0).getRank());
        assertEquals(1, ranked2.get(1).getRank());
        assertEquals(1, ranked2.get(2).getRank());

        // Example 3: 75, 60, 45 -> 1, 2, 3 (Distinct marks receive consecutive ranks)
        Student c1 = new Student("C1", "A", "10A", 75.0);
        Student c2 = new Student("C2", "B", "10A", 60.0);
        Student c3 = new Student("C3", "C", "10A", 45.0);

        List<StudentResponse> ranked3 = service.assignDenseRankings(List.of(
                service.mapToStudentResponse(c1),
                service.mapToStudentResponse(c2),
                service.mapToStudentResponse(c3)
        ));
        assertEquals(1, ranked3.get(0).getRank());
        assertEquals(2, ranked3.get(1).getRank());
        assertEquals(3, ranked3.get(2).getRank());
    }

    @Test
    @DisplayName("Test Visual Ordering for Tied Students Does Not Alter Rank")
    void testVisualOrderingDoesNotAlterRank() {
        // Students with identical marks but different names/IDs
        Student zack = new Student("S99", "Zack", "10A", 92.0);
        Student adam = new Student("S01", "Adam", "10A", 92.0);

        List<StudentResponse> ranked = service.assignDenseRankings(List.of(
                service.mapToStudentResponse(zack),
                service.mapToStudentResponse(adam)
        ));

        // Alphabetically Adam comes before Zack
        assertEquals("Adam", ranked.get(0).getName());
        assertEquals(1, ranked.get(0).getRank());
        assertEquals("Zack", ranked.get(1).getName());
        assertEquals(1, ranked.get(1).getRank());
    }

    @Test
    @DisplayName("Test Empty and Null Student Lists Handled Safely")
    void testEmptyAndNullStudentLists() {
        assertTrue(service.assignDenseRankings(Collections.emptyList()).isEmpty());
        assertTrue(service.assignDenseRankings(null).isEmpty());

        Student single = new Student("S1", "Solo", "10A", 88.0);
        List<StudentResponse> rankedSingle = service.assignDenseRankings(List.of(service.mapToStudentResponse(single)));
        assertEquals(1, rankedSingle.size());
        assertEquals(1, rankedSingle.get(0).getRank());
    }

    @Test
    @DisplayName("Test Pass/Fail threshold for overall marks")
    void testPassFailThreshold() {
        Student passStudent = new Student("S1", "Pass", "10A", 60.0);
        Student failStudent = new Student("S2", "Fail", "10A", 59.9);

        StudentResponse passRes = service.mapToStudentResponse(passStudent);
        StudentResponse failRes = service.mapToStudentResponse(failStudent);

        assertEquals("PASS", passRes.getStatus());
        assertEquals("FAIL", failRes.getStatus());
    }
}
