package com.studentgradetracker.dto;

import java.util.ArrayList;
import java.util.List;

public class BulkImportConfirmRequest {
    private List<BulkImportRowPreview> rows = new ArrayList<>();
    private boolean skipInvalid = true;

    public BulkImportConfirmRequest() {
    }

    public BulkImportConfirmRequest(List<BulkImportRowPreview> rows, boolean skipInvalid) {
        this.rows = rows;
        this.skipInvalid = skipInvalid;
    }

    public List<BulkImportRowPreview> getRows() {
        return rows;
    }

    public void setRows(List<BulkImportRowPreview> rows) {
        this.rows = rows;
    }

    public boolean isSkipInvalid() {
        return skipInvalid;
    }

    public void setSkipInvalid(boolean skipInvalid) {
        this.skipInvalid = skipInvalid;
    }
}
