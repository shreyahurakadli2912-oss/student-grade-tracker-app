import React, { useState } from 'react';
import { api } from '../services/api';

export default function BulkImportPage({ addToast, setActivePage }) {
  const [step, setStep] = useState(1); // 1: Upload, 2: Preview & Validate, 3: Completed
  const [selectedFile, setSelectedFile] = useState(null);
  const [isDragging, setIsDragging] = useState(false);
  const [loading, setLoading] = useState(false);

  // Preview Response
  const [previewData, setPreviewData] = useState(null);

  // Result Response
  const [importResult, setImportResult] = useState(null);

  const handleFileSelect = (file) => {
    if (!file) return;
    const name = file.name.toLowerCase();
    if (!name.endsWith('.csv') && !name.endsWith('.xlsx') && !name.endsWith('.xls')) {
      addToast('Invalid file type. Please select a CSV or Excel (.xlsx) file.', 'error');
      return;
    }
    setSelectedFile(file);
  };

  const handleDragOver = (e) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = () => {
    setIsDragging(false);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    setIsDragging(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFileSelect(e.dataTransfer.files[0]);
    }
  };

  const handleUploadAndPreview = async () => {
    if (!selectedFile) {
      addToast('Please select a file to upload.', 'error');
      return;
    }

    try {
      setLoading(true);
      const data = await api.previewImport(selectedFile);
      setPreviewData(data);
      setStep(2);
      addToast(`File parsed: ${data.validRows} valid rows, ${data.duplicateRows} duplicates, ${data.invalidRows} errors.`, 'info');
    } catch (err) {
      addToast(err.message || 'Failed to parse file for import', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleConfirmImport = async () => {
    if (!previewData || !previewData.rows) return;

    try {
      setLoading(true);
      const result = await api.confirmImport(previewData.rows, true);
      setImportResult(result);
      setStep(3);
      addToast(`Successfully imported ${result.importedCount} student record(s)!`, 'success');
    } catch (err) {
      addToast(err.message || 'Error executing import', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleCancelImport = () => {
    setPreviewData(null);
    setSelectedFile(null);
    setStep(1);
    addToast('Import cancelled. No records were saved.', 'info');
  };

  return (
    <div className="page-content">
      {/* Page Title */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '24px' }}>
        <div>
          <h2 style={{ fontSize: '20px', fontWeight: 800 }}>Bulk Student Import Wizard</h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '13px' }}>
            Upload entire classroom rosters with grades via CSV or Excel (.xlsx) spreadsheets
          </p>
        </div>

        {/* Template Downloads */}
        <div style={{ display: 'flex', gap: '10px' }}>
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={api.downloadCsvTemplate}
            title="Download CSV sample file"
          >
            <span>📄 Download CSV Template</span>
          </button>
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={api.downloadExcelTemplate}
            title="Download Excel XLSX sample file"
          >
            <span>📊 Download Excel Template</span>
          </button>
        </div>
      </div>

      {/* Step Wizard Steps Header */}
      <div className="steps-wizard">
        <div className={`step-item ${step >= 1 ? 'active' : ''} ${step > 1 ? 'completed' : ''}`}>
          <div className="step-circle">{step > 1 ? '✓' : '1'}</div>
          <span className="step-title">1. Select & Upload File</span>
        </div>

        <div style={{ flex: 1, height: '2px', background: step >= 2 ? 'var(--primary)' : 'var(--border)', margin: '0 12px' }} />

        <div className={`step-item ${step >= 2 ? 'active' : ''} ${step > 2 ? 'completed' : ''}`}>
          <div className="step-circle">{step > 2 ? '✓' : '2'}</div>
          <span className="step-title">2. Preview & Row Validation</span>
        </div>

        <div style={{ flex: 1, height: '2px', background: step >= 3 ? 'var(--success)' : 'var(--border)', margin: '0 12px' }} />

        <div className={`step-item ${step === 3 ? 'active completed' : ''}`}>
          <div className="step-circle">3</div>
          <span className="step-title">3. Final Import Summary</span>
        </div>
      </div>

      {/* STEP 1: Upload Dropzone */}
      {step === 1 && (
        <div className="card">
          <div
            className={`dropzone ${isDragging ? 'active' : ''}`}
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
            onClick={() => document.getElementById('file-upload-input').click()}
          >
            <input
              id="file-upload-input"
              type="file"
              accept=".csv,.xlsx,.xls"
              style={{ display: 'none' }}
              onChange={(e) => handleFileSelect(e.target.files[0])}
            />
            <div className="dropzone-icon">📁</div>
            <h3 style={{ fontSize: '16px', fontWeight: 700, marginBottom: '6px' }}>
              {selectedFile ? selectedFile.name : 'Choose a CSV or Excel file or drag & drop here'}
            </h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '13px', marginBottom: '16px' }}>
              Supported formats: .csv, .xlsx | Maximum file size: 15MB
            </p>

            {selectedFile ? (
              <div
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '8px',
                  background: 'var(--primary-light)',
                  padding: '6px 14px',
                  borderRadius: '20px',
                  fontSize: '12.5px',
                  color: '#60a5fa',
                  border: '1px solid rgba(37,99,235,0.4)',
                }}
              >
                <span>Selected: {selectedFile.name} ({(selectedFile.size / 1024).toFixed(1)} KB)</span>
              </div>
            ) : (
              <button type="button" className="btn btn-primary btn-sm">
                Browse Computer
              </button>
            )}
          </div>

          {selectedFile && (
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '20px' }}>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setSelectedFile(null)}
                disabled={loading}
              >
                Clear Selection
              </button>
              <button
                type="button"
                className="btn btn-primary"
                onClick={handleUploadAndPreview}
                disabled={loading}
              >
                {loading ? 'Validating File...' : 'Upload & Validate Rows →'}
              </button>
            </div>
          )}

          {/* Guidelines box */}
          <div
            style={{
              marginTop: '28px',
              padding: '16px',
              borderRadius: '8px',
              background: 'rgba(0,0,0,0.2)',
              border: '1px solid var(--border)',
            }}
          >
            <h4 style={{ fontSize: '13px', fontWeight: 700, marginBottom: '8px', color: '#60a5fa' }}>
              📋 Required Spreadsheet Columns:
            </h4>
            <p style={{ fontSize: '12.5px', color: 'var(--text-muted)', lineHeight: '1.6' }}>
              The file must contain a header row with these exact or equivalent columns:
              <br />
              <code>studentId</code> (unique ID), <code>name</code> (student full name), <code>className</code> (e.g. 10A), <code>marks</code> (numeric score between 0.0 and 100.0).
            </p>
          </div>
        </div>
      )}

      {/* STEP 2: Preview & Validation Table */}
      {step === 2 && previewData && (
        <div>
          {/* Validation Summary Stat Cards */}
          <div className="stat-grid" style={{ marginBottom: '20px' }}>
            <div className="stat-card">
              <div className="stat-icon-wrapper" style={{ background: 'rgba(37,99,235,0.15)', color: '#60a5fa' }}>
                📄
              </div>
              <div className="stat-info">
                <div className="stat-label">Total Rows in File</div>
                <div className="stat-value">{previewData.totalRows}</div>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon-wrapper" style={{ background: 'rgba(16,185,129,0.15)', color: '#34d399' }}>
                ✓
              </div>
              <div className="stat-info">
                <div className="stat-label">Valid for Import</div>
                <div className="stat-value" style={{ color: '#34d399' }}>{previewData.validRows}</div>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon-wrapper" style={{ background: 'rgba(245,158,11,0.15)', color: '#fbbf24' }}>
                ⚠️
              </div>
              <div className="stat-info">
                <div className="stat-label">Duplicate IDs</div>
                <div className="stat-value" style={{ color: '#fbbf24' }}>{previewData.duplicateRows}</div>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon-wrapper" style={{ background: 'rgba(239,68,68,0.15)', color: '#f87171' }}>
                ✕
              </div>
              <div className="stat-info">
                <div className="stat-label">Invalid Rows</div>
                <div className="stat-value" style={{ color: '#f87171' }}>{previewData.invalidRows}</div>
              </div>
            </div>
          </div>

          {/* Action bar for Preview */}
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              marginBottom: '16px',
              padding: '14px 18px',
              background: 'var(--bg-card)',
              borderRadius: '8px',
              border: '1px solid var(--border)',
            }}
          >
            <div style={{ fontSize: '13.5px', color: 'var(--text-muted)' }}>
              Ready to import <strong style={{ color: '#34d399' }}>{previewData.validRows} valid rows</strong>. Invalid or duplicate records will be skipped.
            </div>

            <div style={{ display: 'flex', gap: '10px' }}>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={handleCancelImport}
                disabled={loading}
              >
                Cancel Import
              </button>
              <button
                type="button"
                className="btn btn-success"
                onClick={handleConfirmImport}
                disabled={loading || previewData.validRows === 0}
              >
                {loading ? 'Importing Rows...' : `Confirm & Save ${previewData.validRows} Student(s)`}
              </button>
            </div>
          </div>

          {/* Preview Table */}
          <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
            <div className="table-container" style={{ maxHeight: '450px', border: 'none' }}>
              <table className="data-table">
                <thead>
                  <tr>
                    <th style={{ width: '60px' }}>Row</th>
                    <th>Status</th>
                    <th>Student ID</th>
                    <th>Name</th>
                    <th>Class</th>
                    <th style={{ textAlign: 'right' }}>Marks</th>
                    <th style={{ textAlign: 'center' }}>Projected Grade</th>
                    <th>Validation Details</th>
                  </tr>
                </thead>
                <tbody>
                  {previewData.rows.map((row) => (
                    <tr
                      key={row.rowNumber}
                      style={{
                        background:
                          row.status === 'VALID'
                            ? 'transparent'
                            : row.status.startsWith('DUPLICATE')
                            ? 'rgba(245, 158, 11, 0.05)'
                            : 'rgba(239, 68, 68, 0.05)',
                      }}
                    >
                      <td style={{ fontFamily: 'var(--font-mono)', color: 'var(--text-dim)' }}>
                        #{row.rowNumber}
                      </td>
                      <td>
                        <span
                          className={`badge ${
                            row.status === 'VALID'
                              ? 'badge-pass'
                              : row.status.startsWith('DUPLICATE')
                              ? 'badge-c'
                              : 'badge-fail'
                          }`}
                        >
                          {row.status}
                        </span>
                      </td>
                      <td style={{ fontFamily: 'var(--font-mono)', fontWeight: 600 }}>{row.studentId || '-'}</td>
                      <td>{row.name || '-'}</td>
                      <td>{row.className || '-'}</td>
                      <td style={{ textAlign: 'right', fontFamily: 'var(--font-mono)' }}>
                        {row.marks != null ? row.marks.toFixed(2) : '-'}
                      </td>
                      <td style={{ textAlign: 'center' }}>
                        {row.grade ? (
                          <span className={`badge badge-${row.grade.toLowerCase()}`}>{row.grade}</span>
                        ) : (
                          '-'
                        )}
                      </td>
                      <td>
                        {row.errors && row.errors.length > 0 ? (
                          <span style={{ color: '#f87171', fontSize: '12px' }}>
                            {row.errors.join('; ')}
                          </span>
                        ) : (
                          <span style={{ color: '#34d399', fontSize: '12px' }}>Valid record</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* STEP 3: Final Results Summary */}
      {step === 3 && importResult && (
        <div className="card" style={{ maxWidth: '640px', margin: '0 auto', textAlign: 'center', padding: '36px' }}>
          <div style={{ fontSize: '50px', marginBottom: '12px' }}>🎉</div>
          <h3 style={{ fontSize: '20px', fontWeight: 800, marginBottom: '8px' }}>Import Completed</h3>
          <p style={{ color: 'var(--text-muted)', fontSize: '14px', marginBottom: '24px' }}>
            {importResult.message}
          </p>

          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(3, 1fr)',
              gap: '12px',
              padding: '16px',
              borderRadius: '8px',
              background: 'rgba(0,0,0,0.2)',
              marginBottom: '24px',
            }}
          >
            <div>
              <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>IMPORTED</div>
              <div style={{ fontSize: '22px', fontWeight: 800, color: '#34d399' }}>
                {importResult.importedCount}
              </div>
            </div>
            <div>
              <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>DUPLICATES</div>
              <div style={{ fontSize: '22px', fontWeight: 800, color: '#fbbf24' }}>
                {importResult.duplicateCount}
              </div>
            </div>
            <div>
              <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>REJECTED</div>
              <div style={{ fontSize: '22px', fontWeight: 800, color: '#f87171' }}>
                {importResult.rejectedCount}
              </div>
            </div>
          </div>

          <div style={{ display: 'flex', justifyContent: 'center', gap: '12px' }}>
            {importResult.rejectedRows && importResult.rejectedRows.length > 0 && (
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => api.downloadRejectedRowsReport(importResult.rejectedRows)}
              >
                <span>📥 Download Rejected CSV Report</span>
              </button>
            )}

            <button
              type="button"
              className="btn btn-primary"
              onClick={() => setActivePage('students')}
            >
              <span>View Student Directory →</span>
            </button>

            <button
              type="button"
              className="btn btn-outline"
              onClick={() => {
                setStep(1);
                setSelectedFile(null);
                setPreviewData(null);
                setImportResult(null);
              }}
            >
              Upload Another File
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
