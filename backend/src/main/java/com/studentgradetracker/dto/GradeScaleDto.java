package com.studentgradetracker.dto;

public class GradeScaleDto {
    private Long id;
    private String grade;
    private Double minPercentage;
    private Double maxPercentage;
    private String description;
    private Integer sortOrder;

    public GradeScaleDto() {
    }

    public GradeScaleDto(Long id, String grade, Double minPercentage, Double maxPercentage, String description, Integer sortOrder) {
        this.id = id;
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
