package com.studentgradetracker.dto;

public class SubjectResponse {
    private Long id;
    private String code;
    private String name;
    private Double maxMarks;
    private Double passMarks;
    private Boolean isCompulsory;

    public SubjectResponse() {
    }

    public SubjectResponse(Long id, String code, String name, Double maxMarks, Double passMarks, Boolean isCompulsory) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.maxMarks = maxMarks;
        this.passMarks = passMarks;
        this.isCompulsory = isCompulsory;
    }

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
}
