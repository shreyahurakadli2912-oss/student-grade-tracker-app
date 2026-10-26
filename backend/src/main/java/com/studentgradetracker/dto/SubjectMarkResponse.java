package com.studentgradetracker.dto;

public class SubjectMarkResponse {
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private Double marksObtained;
    private Double maxMarks;
    private Double passMarks;
    private Double percentage;
    private Boolean isPassed;
    private Boolean isCompulsory;

    public SubjectMarkResponse() {
    }

    public SubjectMarkResponse(Long subjectId, String subjectCode, String subjectName,
                               Double marksObtained, Double maxMarks, Double passMarks,
                               Double percentage, Boolean isPassed, Boolean isCompulsory) {
        this.subjectId = subjectId;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.marksObtained = marksObtained;
        this.maxMarks = maxMarks;
        this.passMarks = passMarks;
        this.percentage = percentage;
        this.isPassed = isPassed;
        this.isCompulsory = isCompulsory;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Double getMarksObtained() {
        return marksObtained;
    }

    public void setMarksObtained(Double marksObtained) {
        this.marksObtained = marksObtained;
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

    public Boolean getIsCompulsory() {
        return isCompulsory;
    }

    public void setIsCompulsory(Boolean compulsory) {
        isCompulsory = compulsory;
    }
}
