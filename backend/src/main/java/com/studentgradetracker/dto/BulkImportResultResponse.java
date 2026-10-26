package com.studentgradetracker.dto;

import java.util.ArrayList;
import java.util.List;

public class BulkImportResultResponse {
    private int totalRows;
    private int importedCount;
    private int duplicateCount;
    private int rejectedCount;
    private List<BulkImportRowPreview> rejectedRows = new ArrayList<>();
    private String message;

    public BulkImportResultResponse() {
    }

    public BulkImportResultResponse(int totalRows, int importedCount, int duplicateCount, int rejectedCount, String message) {
        this.totalRows = totalRows;
        this.importedCount = importedCount;
        this.duplicateCount = duplicateCount;
        this.rejectedCount = rejectedCount;
        this.message = message;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getImportedCount() {
        return importedCount;
    }

    public void setImportedCount(int importedCount) {
        this.importedCount = importedCount;
    }

    public int getDuplicateCount() {
        return duplicateCount;
    }

    public void setDuplicateCount(int duplicateCount) {
        this.duplicateCount = duplicateCount;
    }

    public int getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(int rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    public List<BulkImportRowPreview> getRejectedRows() {
        return rejectedRows;
    }

    public void setRejectedRows(List<BulkImportRowPreview> rejectedRows) {
        this.rejectedRows = rejectedRows;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
