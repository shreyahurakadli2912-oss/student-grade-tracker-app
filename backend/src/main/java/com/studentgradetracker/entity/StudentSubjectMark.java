package com.studentgradetracker.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "student_subject_marks", uniqueConstraints = {
    @UniqueConstraint(name = "uk_student_subject", columnNames = {"student_id", "subject_id"})
})
public class StudentSubjectMark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "marks_obtained", nullable = false)
    private Double marksObtained;

    @Column(name = "max_marks", nullable = false)
    private Double maxMarks;

    @Column(name = "percentage", nullable = false)
    private Double percentage;

    @Column(name = "is_passed", nullable = false)
    private Boolean isPassed;

    public StudentSubjectMark() {
    }

    public StudentSubjectMark(Student student, Subject subject, Double marksObtained, Double maxMarks) {
        this.student = student;
        this.subject = subject;
        this.marksObtained = marksObtained;
        this.maxMarks = maxMarks;
        recalculate();
    }

    public void recalculate() {
        if (this.maxMarks != null && this.maxMarks > 0 && this.marksObtained != null) {
            this.percentage = Math.round((this.marksObtained / this.maxMarks * 100.0) * 100.0) / 100.0;
            Double requiredPassMarks = (this.subject != null && this.subject.getPassMarks() != null) 
                    ? this.subject.getPassMarks() : 40.0;
            this.isPassed = this.marksObtained >= requiredPassMarks;
        } else {
            this.percentage = 0.0;
            this.isPassed = false;
        }
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        recalculate();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Double getMarksObtained() {
        return marksObtained;
    }

    public void setMarksObtained(Double marksObtained) {
        this.marksObtained = marksObtained;
        recalculate();
    }

    public Double getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(Double maxMarks) {
        this.maxMarks = maxMarks;
        recalculate();
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public Boolean getIsPassed() {
        return isPassed;
    }

    public void setIsPassed(Boolean passed) {
        isPassed = passed;
    }
}
