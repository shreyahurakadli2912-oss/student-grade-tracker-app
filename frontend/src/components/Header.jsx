import React from 'react';

const PAGE_TITLES = {
  dashboard: 'Executive Dashboard & Overview',
  students: 'Student Records & Grade Directory',
  import: 'Bulk Student Upload (CSV & Excel)',
  subjects: 'Curriculum Subjects & Marks',
  reports: 'Official Grade Reports & Transcripts',
  analytics: 'Class Analytics & Performance Metrics',
  gradescale: 'Grade Boundaries & Scaling Configuration',
  settings: 'System Diagnostics & Demo Controls',
};

export default function Header({ activePage, onQuickAdd, onLoadDemo, isOnline }) {
  return (
    <header className="top-header">
      <div className="header-title">
        <h2>{PAGE_TITLES[activePage] || 'Student Grade Tracker'}</h2>
        <span className="header-badge">H2 File DB (Persistent)</span>
      </div>

      <div className="header-actions">
        <button
          type="button"
          className="btn btn-secondary btn-sm"
          onClick={onLoadDemo}
          title="Preload 5 students (S101-S105) matching Phase 17 acceptance criteria"
        >
          <span>⚡ Load Demo (5 Students)</span>
        </button>

        {activePage === 'students' && (
          <button
            type="button"
            className="btn btn-primary btn-sm"
            onClick={onQuickAdd}
          >
            <span>+ Add Student</span>
          </button>
        )}
      </div>
    </header>
  );
}
