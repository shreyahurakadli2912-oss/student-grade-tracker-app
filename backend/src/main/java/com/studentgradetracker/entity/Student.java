package com.studentgradetracker.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "students", indexes = {
    @Index(name = "idx_student_student_id", columnList = "student_id", unique = true),
    @Index(name = "idx_student_class_name", columnList = "class_name")
})
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false, unique = true, length = 64)
    private String studentId;

    @Column(name = "full_name", nullable = false, length = 150)
    private String name;

    @Column(name = "class_name", nullable = false, length = 50)
    private String className;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "overall_marks")
    private Double marks;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<StudentSubjectMark> subjectMarks = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Student() {
    }

    public Student(String studentId, String name, String className, Double marks) {
        this.studentId = studentId;
        this.name = name;
        this.className = className;
        this.marks = marks;
    }

    public Student(String studentId, String name, String className, String email, Double marks) {
        this.studentId = studentId;
        this.name = name;
        this.className = className;
        this.email = email;
        this.marks = marks;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<StudentSubjectMark> getSubjectMarks() {
        return subjectMarks;
    }

    public void setSubjectMarks(List<StudentSubjectMark> subjectMarks) {
        this.subjectMarks = subjectMarks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void addSubjectMark(StudentSubjectMark mark) {
        subjectMarks.add(mark);
        mark.setStudent(this);
    }

    public void removeSubjectMark(StudentSubjectMark mark) {
        subjectMarks.remove(mark);
        mark.setStudent(null);
    }
}
