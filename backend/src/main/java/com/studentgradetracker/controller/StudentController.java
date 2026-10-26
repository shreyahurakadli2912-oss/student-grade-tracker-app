package com.studentgradetracker.controller;

import com.studentgradetracker.dto.StudentRequest;
import com.studentgradetracker.dto.StudentResponse;
import com.studentgradetracker.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Students", description = "Endpoints for student creation, retrieval, updates, and deletion")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    @Operation(summary = "Create a new student with overall or subject-wise marks")
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody StudentRequest request) {
        StudentResponse response = studentService.createStudent(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all students with search, filters, rankings, and sorting")
    public ResponseEntity<List<StudentResponse>> getAllStudents(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "rank") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {
        List<StudentResponse> list = studentService.getAllStudentsRanked(query, className, grade, status, sortBy, sortDir);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get student details by database ID")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing student record and recalculate marks/grade")
    public ResponseEntity<StudentResponse> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
        return ResponseEntity.ok(studentService.updateStudent(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a student record permanently")
    public ResponseEntity<Map<String, String>> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        Map<String, String> res = new HashMap<>();
        res.put("message", "Student deleted successfully.");
        return ResponseEntity.ok(res);
    }

    @GetMapping("/classes")
    @Operation(summary = "Get all distinct class names for filtering")
    public ResponseEntity<List<String>> getClassNames() {
        return ResponseEntity.ok(studentService.getAllClassNames());
    }

    @PostMapping("/demo-data")
    @Operation(summary = "Load Phase 17 acceptance test dataset (5 students: S101-S105)")
    public ResponseEntity<Map<String, Object>> loadDemoData() {
        studentService.loadAcceptanceTestData();
        Map<String, Object> res = new HashMap<>();
        res.put("message", "Loaded 5 acceptance test students (S101-S105) successfully.");
        res.put("count", 5);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/all")
    @Operation(summary = "Clear all student records (Testing / Reset utility)")
    public ResponseEntity<Map<String, String>> clearAll() {
        studentService.clearAllStudents();
        Map<String, String> res = new HashMap<>();
        res.put("message", "All student records have been cleared.");
        return ResponseEntity.ok(res);
    }
}
