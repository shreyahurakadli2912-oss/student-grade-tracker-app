package com.studentgradetracker.service;

import com.studentgradetracker.dto.StudentRequest;
import com.studentgradetracker.dto.StudentResponse;
import com.studentgradetracker.dto.SubjectMarkInput;
import com.studentgradetracker.entity.Student;
import com.studentgradetracker.entity.StudentSubjectMark;
import com.studentgradetracker.entity.Subject;
import com.studentgradetracker.exception.BadRequestException;
import com.studentgradetracker.exception.DuplicateResourceException;
import com.studentgradetracker.exception.ResourceNotFoundException;
import com.studentgradetracker.repository.StudentRepository;
import com.studentgradetracker.repository.StudentSubjectMarkRepository;
import com.studentgradetracker.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final StudentSubjectMarkRepository studentSubjectMarkRepository;
    private final GradeCalculationService gradeCalculationService;

    public StudentService(StudentRepository studentRepository,
                          SubjectRepository subjectRepository,
                          StudentSubjectMarkRepository studentSubjectMarkRepository,
                          GradeCalculationService gradeCalculationService) {
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.studentSubjectMarkRepository = studentSubjectMarkRepository;
        this.gradeCalculationService = gradeCalculationService;
    }

    public StudentResponse createStudent(StudentRequest request) {
        validateStudentRequest(request);

        if (studentRepository.existsByStudentId(request.getStudentId().trim())) {
            throw new DuplicateResourceException("A student with ID '" + request.getStudentId() + "' already exists.");
        }

        Student student = new Student();
        student.setStudentId(request.getStudentId().trim());
        student.setName(request.getName().trim());
        student.setClassName(request.getClassName().trim());
        student.setEmail(request.getEmail() != null && !request.getEmail().isBlank() ? request.getEmail().trim() : null);
        student.setMarks(request.getMarks());

        Student saved = studentRepository.save(student);

        // Process subject marks if provided
        if (request.getSubjectMarks() != null && !request.getSubjectMarks().isEmpty()) {
            saveSubjectMarks(saved, request.getSubjectMarks());
        }

        // Fetch again to have populated relations
        Student reloaded = studentRepository.findById(saved.getId()).orElse(saved);
        return gradeCalculationService.mapToStudentResponse(reloaded);
    }

    public StudentResponse updateStudent(Long id, StudentRequest request) {
        validateStudentRequest(request);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student with ID " + id + " not found."));

        if (studentRepository.existsByStudentIdAndIdNot(request.getStudentId().trim(), id)) {
            throw new DuplicateResourceException("Another student with ID '" + request.getStudentId() + "' already exists.");
        }

        student.setStudentId(request.getStudentId().trim());
        student.setName(request.getName().trim());
        student.setClassName(request.getClassName().trim());
        student.setEmail(request.getEmail() != null && !request.getEmail().isBlank() ? request.getEmail().trim() : null);
        student.setMarks(request.getMarks());

        // Update subject marks if provided
        if (request.getSubjectMarks() != null) {
            student.getSubjectMarks().clear();
            studentSubjectMarkRepository.deleteByStudentId(student.getId());
            studentRepository.flush();

            if (!request.getSubjectMarks().isEmpty()) {
                saveSubjectMarks(student, request.getSubjectMarks());
            }
        }

        Student updated = studentRepository.save(student);
        return gradeCalculationService.mapToStudentResponse(updated);
    }

    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student with ID " + id + " not found."));
        studentRepository.delete(student);
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student with ID " + id + " not found."));

        // Assign rank within student's class using dense ranking
        List<Student> classMates = studentRepository.findByClassNameIgnoreCase(student.getClassName());
        List<StudentResponse> responses = classMates.stream()
                .map(gradeCalculationService::mapToStudentResponse)
                .collect(Collectors.toList());

        List<StudentResponse> ranked = gradeCalculationService.assignDenseRankings(responses);

        return ranked.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElse(gradeCalculationService.mapToStudentResponse(student));
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudentsRanked(String query, String className, String grade, String status, String sortBy, String sortDir) {
        boolean isClassFiltered = (className != null && !className.isBlank() && !className.equalsIgnoreCase("ALL"));

        // Fetch students for the ranking group (class-specific or overall)
        List<Student> rawStudents;
        if (isClassFiltered) {
            rawStudents = studentRepository.findByClassNameIgnoreCase(className.trim());
        } else {
            rawStudents = studentRepository.findAll();
        }

        // Convert all to responses
        List<StudentResponse> responses = rawStudents.stream()
                .map(gradeCalculationService::mapToStudentResponse)
                .collect(Collectors.toList());

        // Assign dense rankings within the intended ranking group:
        // - Class-specific reports rank students within the selected class
        // - Overall reports rank the full selected student set
        responses = gradeCalculationService.assignDenseRankings(responses);

        // Apply search query filter if provided
        if (query != null && !query.isBlank()) {
            String q = query.trim().toLowerCase();
            responses = responses.stream()
                    .filter(s -> (s.getStudentId() != null && s.getStudentId().toLowerCase().contains(q)) ||
                                 (s.getName() != null && s.getName().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }

        // Apply grade filter
        if (grade != null && !grade.isBlank() && !grade.equalsIgnoreCase("ALL")) {
            responses = responses.stream()
                    .filter(s -> s.getGrade().equalsIgnoreCase(grade.trim()))
                    .collect(Collectors.toList());
        }

        // Apply status filter
        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            responses = responses.stream()
                    .filter(s -> s.getStatus().equalsIgnoreCase(status.trim()))
                    .collect(Collectors.toList());
        }

        // Apply sorting
        final String field = (sortBy != null && !sortBy.isBlank()) ? sortBy.toLowerCase() : "rank";
        final boolean isAsc = (sortDir == null || sortDir.equalsIgnoreCase("asc"));

        responses.sort((a, b) -> {
            int cmp = 0;
            switch (field) {
                case "name":
                    cmp = a.getName().compareToIgnoreCase(b.getName());
                    break;
                case "studentid":
                    cmp = a.getStudentId().compareToIgnoreCase(b.getStudentId());
                    break;
                case "marks":
                    cmp = Double.compare(GradeCalculationService.getEffectiveMarks(a),
                                         GradeCalculationService.getEffectiveMarks(b));
                    if (cmp == 0) {
                        String n1 = a.getName() != null ? a.getName() : "";
                        String n2 = b.getName() != null ? b.getName() : "";
                        cmp = n1.compareToIgnoreCase(n2);
                    }
                    break;
                case "percentage":
                    cmp = Double.compare(a.getPercentage() != null ? a.getPercentage() : 0.0,
                                         b.getPercentage() != null ? b.getPercentage() : 0.0);
                    if (cmp == 0) {
                        String n1 = a.getName() != null ? a.getName() : "";
                        String n2 = b.getName() != null ? b.getName() : "";
                        cmp = n1.compareToIgnoreCase(n2);
                    }
                    break;
                case "classname":
                    cmp = a.getClassName().compareToIgnoreCase(b.getClassName());
                    break;
                case "grade":
                    cmp = a.getGrade().compareTo(b.getGrade());
                    break;
                case "rank":
                default:
                    int rankA = a.getRank() != null ? a.getRank() : Integer.MAX_VALUE;
                    int rankB = b.getRank() != null ? b.getRank() : Integer.MAX_VALUE;
                    cmp = Integer.compare(rankA, rankB);
                    if (cmp == 0) {
                        String n1 = a.getName() != null ? a.getName() : "";
                        String n2 = b.getName() != null ? b.getName() : "";
                        cmp = n1.compareToIgnoreCase(n2);
                        if (cmp == 0) {
                            String id1 = a.getStudentId() != null ? a.getStudentId() : "";
                            String id2 = b.getStudentId() != null ? b.getStudentId() : "";
                            cmp = id1.compareToIgnoreCase(id2);
                        }
                    }
                    break;
            }
            return isAsc ? cmp : -cmp;
        });

        return responses;
    }

    @Transactional(readOnly = true)
    public List<String> getAllClassNames() {
        return studentRepository.findDistinctClassNames();
    }

    public void clearAllStudents() {
        studentSubjectMarkRepository.deleteAll();
        studentRepository.deleteAll();
    }

    public void loadAcceptanceTestData() {
        clearAllStudents();

        // 5 records from Acceptance Test:
        // S101,Alice Johnson,10A,94.5
        // S102,Bob Smith,10A,82
        // S103,Charlie Brown,10A,74
        // S104,David Miller,10A,65.5
        // S105,Eva Green,10A,52
        List<Student> sample = List.of(
                new Student("S101", "Alice Johnson", "10A", "alice@example.com", 94.5),
                new Student("S102", "Bob Smith", "10A", "bob@example.com", 82.0),
                new Student("S103", "Charlie Brown", "10A", "charlie@example.com", 74.0),
                new Student("S104", "David Miller", "10A", "david@example.com", 65.5),
                new Student("S105", "Eva Green", "10A", "eva@example.com", 52.0)
        );
        studentRepository.saveAll(sample);
    }

    private void validateStudentRequest(StudentRequest request) {
        if (request.getStudentId() == null || request.getStudentId().trim().isEmpty()) {
            throw new BadRequestException("Student ID cannot be empty.");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Student Name cannot be empty.");
        }
        if (request.getClassName() == null || request.getClassName().trim().isEmpty()) {
            throw new BadRequestException("Class name cannot be empty.");
        }
        if (request.getMarks() != null) {
            if (request.getMarks() < 0.0 || request.getMarks() > 100.0) {
                throw new BadRequestException("Marks must be between 0 and 100.");
            }
        }
    }

    private void saveSubjectMarks(Student student, List<SubjectMarkInput> inputs) {
        for (SubjectMarkInput input : inputs) {
            Subject subject = subjectRepository.findById(input.getSubjectId())
                    .orElseThrow(() -> new BadRequestException("Subject with ID " + input.getSubjectId() + " does not exist."));

            if (input.getMarksObtained() < 0 || input.getMarksObtained() > subject.getMaxMarks()) {
                throw new BadRequestException("Marks for " + subject.getName() + " must be between 0 and " + subject.getMaxMarks());
            }

            StudentSubjectMark mark = new StudentSubjectMark(
                    student,
                    subject,
                    input.getMarksObtained(),
                    subject.getMaxMarks()
            );
            studentSubjectMarkRepository.save(mark);
            student.getSubjectMarks().add(mark);
        }
    }
}
