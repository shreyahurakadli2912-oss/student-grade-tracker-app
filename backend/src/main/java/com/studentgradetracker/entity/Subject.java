package com.studentgradetracker.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "subjects", indexes = {
    @Index(name = "idx_subject_code", columnList = "code", unique = true)
})
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "max_marks", nullable = false)
    private Double maxMarks;

    @Column(name = "pass_marks", nullable = false)
    private Double passMarks;

    @Column(name = "is_compulsory", nullable = false)
    private Boolean isCompulsory = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Subject() {
    }

    public Subject(String code, String name, Double maxMarks, Double passMarks, Boolean isCompulsory) {
        this.code = code;
        this.name = name;
        this.maxMarks = maxMarks;
        this.passMarks = passMarks;
        this.isCompulsory = isCompulsory != null ? isCompulsory : true;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isCompulsory == null) {
            this.isCompulsory = true;
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(Double maxMarks) {
        this.maxMarks = maxMarks;
    }

    public Double getPassMarks() {
        return passMarks;
    }

    public void setPassMarks(Double passMarks) {
        this.passMarks = passMarks;
    }

    public Boolean getIsCompulsory() {
        return isCompulsory;
    }

    public void setIsCompulsory(Boolean compulsory) {
        isCompulsory = compulsory;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
