package com.studentgradetracker;

import com.studentgradetracker.dto.StudentRequest;
import com.studentgradetracker.dto.StudentResponse;
import com.studentgradetracker.entity.Student;
import com.studentgradetracker.exception.BadRequestException;
import com.studentgradetracker.exception.DuplicateResourceException;
import com.studentgradetracker.exception.ResourceNotFoundException;
import com.studentgradetracker.repository.GradeScaleRuleRepository;
import com.studentgradetracker.repository.StudentRepository;
import com.studentgradetracker.repository.StudentSubjectMarkRepository;
import com.studentgradetracker.repository.SubjectRepository;
import com.studentgradetracker.service.GradeCalculationService;
import com.studentgradetracker.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private StudentSubjectMarkRepository studentSubjectMarkRepository;
    @Mock
    private GradeScaleRuleRepository gradeScaleRuleRepository;

    private GradeCalculationService gradeCalculationService;
    private StudentService studentService;

    @BeforeEach
    void setUp() {
        lenient().when(gradeScaleRuleRepository.findAllByOrderBySortOrderAsc()).thenReturn(Collections.emptyList());
        gradeCalculationService = new GradeCalculationService(gradeScaleRuleRepository);
        studentService = new StudentService(studentRepository, subjectRepository, studentSubjectMarkRepository, gradeCalculationService);
    }

    @Test
    @DisplayName("Test 1: Student creation with valid overall marks")
    void testCreateStudentSuccess() {
        StudentRequest req = new StudentRequest("S101", "Alice Johnson", "10A", 94.5);

        when(studentRepository.existsByStudentId("S101")).thenReturn(false);

        Student saved = new Student("S101", "Alice Johnson", "10A", 94.5);
        saved.setId(1L);

        when(studentRepository.save(any(Student.class))).thenReturn(saved);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(saved));

        StudentResponse resp = studentService.createStudent(req);

        assertNotNull(resp);
        assertEquals("S101", resp.getStudentId());
        assertEquals("Alice Johnson", resp.getName());
        assertEquals(94.5, resp.getMarks());
        assertEquals("A", resp.getGrade());
        assertEquals("PASS", resp.getStatus());
    }

    @Test
    @DisplayName("Test 5: Duplicate student ID rejected")
    void testDuplicateStudentIdRejected() {
        StudentRequest req = new StudentRequest("S101", "Alice Johnson", "10A", 94.5);
        when(studentRepository.existsByStudentId("S101")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> studentService.createStudent(req));
    }

    @Test
    @DisplayName("Test 6: Empty name rejected")
    void testEmptyNameRejected() {
        StudentRequest req = new StudentRequest("S101", "   ", "10A", 80.0);
        assertThrows(BadRequestException.class, () -> studentService.createStudent(req));
    }

    @Test
    @DisplayName("Test 7: Invalid marks (< 0 or > 100) rejected")
    void testInvalidMarksRejected() {
        StudentRequest reqNegative = new StudentRequest("S101", "Alice", "10A", -5.0);
        assertThrows(BadRequestException.class, () -> studentService.createStudent(reqNegative));

        StudentRequest reqOverHundred = new StudentRequest("S101", "Alice", "10A", 105.0);
        assertThrows(BadRequestException.class, () -> studentService.createStudent(reqOverHundred));
    }

    @Test
    @DisplayName("Test 3: Student update and marks recalculation")
    void testUpdateStudent() {
        Student existing = new Student("S101", "Alice", "10A", 75.0);
        existing.setId(1L);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(studentRepository.existsByStudentIdAndIdNot("S101", 1L)).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenReturn(existing);

        StudentRequest updateReq = new StudentRequest("S101", "Alice Johnson Updated", "10A", 95.0);
        StudentResponse updated = studentService.updateStudent(1L, updateReq);

        assertEquals("Alice Johnson Updated", updated.getName());
        assertEquals(95.0, updated.getMarks());
        assertEquals("A", updated.getGrade());
    }

    @Test
    @DisplayName("Test 4: Student deletion")
    void testDeleteStudent() {
        Student existing = new Student("S101", "Alice", "10A", 75.0);
        existing.setId(1L);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(existing));

        studentService.deleteStudent(1L);
        verify(studentRepository, times(1)).delete(existing);
    }

    @Test
    @DisplayName("Test Ranking remains correct after filtering by class")
    void testRankingWithClassFiltering() {
        Student a1 = new Student("S1", "Alice", "10A", 95.0);
        Student a2 = new Student("S2", "Bob", "10A", 90.0);
        Student a3 = new Student("S3", "Charlie", "10A", 90.0);

        Student b1 = new Student("S4", "Daisy", "10B", 98.0);
        Student b2 = new Student("S5", "Edward", "10B", 90.0);

        List<Student> class10A = List.of(a1, a2, a3);
        List<Student> class10B = List.of(b1, b2);
        List<Student> all = List.of(a1, a2, a3, b1, b2);

        when(studentRepository.findByClassNameIgnoreCase("10A")).thenReturn(class10A);
        when(studentRepository.findByClassNameIgnoreCase("10B")).thenReturn(class10B);
        when(studentRepository.findAll()).thenReturn(all);

        // Class 10A filter: ranks within Class 10A
        List<StudentResponse> res10A = studentService.getAllStudentsRanked(null, "10A", null, null, "rank", "asc");
        assertEquals(3, res10A.size());
        assertEquals("Alice", res10A.get(0).getName());
        assertEquals(1, res10A.get(0).getRank());
        assertEquals("Bob", res10A.get(1).getName());
        assertEquals(2, res10A.get(1).getRank());
        assertEquals("Charlie", res10A.get(2).getName());
        assertEquals(2, res10A.get(2).getRank());

        // Class 10B filter: ranks within Class 10B
        List<StudentResponse> res10B = studentService.getAllStudentsRanked(null, "10B", null, null, "rank", "asc");
        assertEquals(2, res10B.size());
        assertEquals("Daisy", res10B.get(0).getName());
        assertEquals(1, res10B.get(0).getRank());
        assertEquals("Edward", res10B.get(1).getName());
        assertEquals(2, res10B.get(1).getRank());

        // Overall filter (ALL): ranks full selected student set together
        List<StudentResponse> resAll = studentService.getAllStudentsRanked(null, "ALL", null, null, "rank", "asc");
        assertEquals(5, resAll.size());
        // Daisy (98.0) -> Rank 1
        assertEquals("Daisy", resAll.get(0).getName());
        assertEquals(1, resAll.get(0).getRank());
        // Alice (95.0) -> Rank 2
        assertEquals("Alice", resAll.get(1).getName());
        assertEquals(2, resAll.get(1).getRank());
        // Bob, Charlie, Edward (90.0) -> Tied at Rank 3
        assertEquals(3, resAll.get(2).getRank());
        assertEquals(3, resAll.get(3).getRank());
        assertEquals(3, resAll.get(4).getRank());
    }
}
