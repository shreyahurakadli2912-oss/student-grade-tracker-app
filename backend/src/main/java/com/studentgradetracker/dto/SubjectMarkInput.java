package com.studentgradetracker.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class SubjectMarkInput {

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    @NotNull(message = "Marks obtained is required")
    @PositiveOrZero(message = "Marks obtained must be zero or positive")
    private Double marksObtained;

    public SubjectMarkInput() {
    }

    public SubjectMarkInput(Long subjectId, Double marksObtained) {
        this.subjectId = subjectId;
        this.marksObtained = marksObtained;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Double getMarksObtained() {
        return marksObtained;
    }

    public void setMarksObtained(Double marksObtained) {
        this.marksObtained = marksObtained;
    }
}
