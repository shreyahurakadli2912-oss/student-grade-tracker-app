package com.studentgradetracker.dto;

import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

public class StudentRequest {

    @NotBlank(message = "Student ID cannot be empty")
    @Size(min = 1, max = 64, message = "Student ID must be between 1 and 64 characters")
    private String studentId;

    @NotBlank(message = "Student name cannot be empty")
    @Size(min = 2, max = 150, message = "Student name must be between 2 and 150 characters")
    private String name;

    @NotBlank(message = "Class or section cannot be empty")
    @Size(min = 1, max = 50, message = "Class name must be between 1 and 50 characters")
    private String className;

    @Email(message = "Please provide a valid email address")
    private String email;

    @DecimalMin(value = "0.0", message = "Marks cannot be negative")
    @DecimalMax(value = "100.0", message = "Overall marks cannot exceed 100")
    private Double marks;

    private List<SubjectMarkInput> subjectMarks = new ArrayList<>();

    public StudentRequest() {
    }

    public StudentRequest(String studentId, String name, String className, Double marks) {
        this.studentId = studentId;
        this.name = name;
        this.className = className;
        this.marks = marks;
    }

    public StudentRequest(String studentId, String name, String className, String email, Double marks) {
        this.studentId = studentId;
        this.name = name;
        this.className = className;
        this.email = email;
        this.marks = marks;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Double getMarks() {
        return marks;
    }

    public void setMarks(Double marks) {
        this.marks = marks;
    }

    public List<SubjectMarkInput> getSubjectMarks() {
        return subjectMarks;
    }

    public void setSubjectMarks(List<SubjectMarkInput> subjectMarks) {
        this.subjectMarks = subjectMarks;
    }
}
