package com.studentgradetracker.dto;

import jakarta.validation.constraints.*;

public class SubjectRequest {

    @NotBlank(message = "Subject code cannot be empty")
    @Size(min = 1, max = 20, message = "Subject code must be between 1 and 20 characters")
    private String code;

    @NotBlank(message = "Subject name cannot be empty")
    @Size(min = 2, max = 100, message = "Subject name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Maximum marks is required")
    @Positive(message = "Maximum marks must be greater than zero")
    private Double maxMarks;

    @NotNull(message = "Passing marks is required")
    @Positive(message = "Passing marks must be greater than zero")
    private Double passMarks;

    private Boolean isCompulsory = true;

    public SubjectRequest() {
    }

    public SubjectRequest(String code, String name, Double maxMarks, Double passMarks, Boolean isCompulsory) {
        this.code = code;
        this.name = name;
        this.maxMarks = maxMarks;
        this.passMarks = passMarks;
        this.isCompulsory = isCompulsory;
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
}
