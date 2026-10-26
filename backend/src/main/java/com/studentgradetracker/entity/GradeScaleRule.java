package com.studentgradetracker.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "grade_scale_rules")
public class GradeScaleRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "grade", nullable = false, length = 10)
    private String grade;

    @Column(name = "min_percentage", nullable = false)
    private Double minPercentage;

    @Column(name = "max_percentage", nullable = false)
    private Double maxPercentage;

    @Column(name = "description", length = 100)
    private String description;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    public GradeScaleRule() {
    }

    public GradeScaleRule(String grade, Double minPercentage, Double maxPercentage, String description, Integer sortOrder) {
        this.grade = grade;
        this.minPercentage = minPercentage;
        this.maxPercentage = maxPercentage;
        this.description = description;
        this.sortOrder = sortOrder;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public Double getMinPercentage() {
        return minPercentage;
    }

    public void setMinPercentage(Double minPercentage) {
        this.minPercentage = minPercentage;
    }

    public Double getMaxPercentage() {
        return maxPercentage;
    }

    public void setMaxPercentage(Double maxPercentage) {
        this.maxPercentage = maxPercentage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
