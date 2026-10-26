package com.studentgradetracker.dto;

import java.util.ArrayList;
import java.util.List;

public class BulkImportRowPreview {
    private int rowNumber;
    private String studentId;
    private String name;
    private String className;
    private Double marks;
    private Double percentage;
    private String grade;
    private String status; // "VALID", "DUPLICATE_IN_FILE", "DUPLICATE_IN_DB", "INVALID"
    private List<String> errors = new ArrayList<>();

    public BulkImportRowPreview() {
    }

    public BulkImportRowPreview(int rowNumber, String studentId, String name, String className, Double marks) {
        this.rowNumber = rowNumber;
        this.studentId = studentId;
        this.name = name;
        this.className = className;
        this.marks = marks;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
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

    public Double getMarks() {
        return marks;
    }

    public void setMarks(Double marks) {
        this.marks = marks;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public void addError(String error) {
        this.errors.add(error);
    }
}
