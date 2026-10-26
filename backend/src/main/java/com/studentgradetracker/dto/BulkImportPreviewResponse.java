package com.studentgradetracker.dto;

import java.util.ArrayList;
import java.util.List;

public class BulkImportPreviewResponse {
    private String fileName;
    private String fileType;
    private int totalRows;
    private int validRows;
    private int duplicateRows;
    private int invalidRows;
    private List<BulkImportRowPreview> rows = new ArrayList<>();

    public BulkImportPreviewResponse() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getValidRows() {
        return validRows;
    }

    public void setValidRows(int validRows) {
        this.validRows = validRows;
    }

    public int getDuplicateRows() {
        return duplicateRows;
    }

    public void setDuplicateRows(int duplicateRows) {
        this.duplicateRows = duplicateRows;
    }

    public int getInvalidRows() {
        return invalidRows;
    }

    public void setInvalidRows(int invalidRows) {
        this.invalidRows = invalidRows;
    }

    public List<BulkImportRowPreview> getRows() {
        return rows;
    }

    public void setRows(List<BulkImportRowPreview> rows) {
        this.rows = rows;
    }
}
