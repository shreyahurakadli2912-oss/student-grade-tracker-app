import React, { useState, useEffect } from 'react';
import ConfirmDialog from '../components/ConfirmDialog';
import { api } from '../services/api';

export default function SettingsPage({ addToast, setActivePage }) {
  const [health, setHealth] = useState(null);
  const [loading, setLoading] = useState(false);
  const [isClearConfirmOpen, setIsClearConfirmOpen] = useState(false);

  const fetchHealth = async () => {
    try {
      const data = await api.getHealth();
      setHealth(data);
    } catch (err) {
      setHealth({ status: 'OFFLINE', error: err.message });
    }
  };

  useEffect(() => {
    fetchHealth();
  }, []);

  const handleLoadAcceptanceData = async () => {
    try {
      setLoading(true);
      await api.loadDemoData();
      addToast('Phase 17 acceptance dataset loaded: 5 students (S101-S105) successfully initialized!', 'success');
      setActivePage('dashboard');
    } catch (err) {
      addToast(err.message || 'Failed to load acceptance data', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleClearAll = async () => {
    try {
      setLoading(true);
      await api.clearAllStudents();
      addToast('All student records and grades cleared from database.', 'info');
      setIsClearConfirmOpen(false);
      setActivePage('students');
    } catch (err) {
      addToast(err.message || 'Failed to clear students', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-content">
      <div style={{ marginBottom: '24px' }}>
        <h2 style={{ fontSize: '20px', fontWeight: 800 }}>System Settings & Diagnostics</h2>
        <p style={{ color: 'var(--text-muted)', fontSize: '13px' }}>
          Server health verification, persistent storage parameters, and evaluation dataset controls
        </p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '24px' }}>
        {/* System Diagnostics Card */}
        <div className="card">
          <div className="card-header">
            <div className="card-title">
              <span>🖥️</span> Application Environment Status
            </div>
            <button type="button" className="btn btn-outline btn-sm" onClick={fetchHealth}>
              Check Status
            </button>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '14px', fontSize: '13.5px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', paddingBottom: '8px', borderBottom: '1px solid var(--border)' }}>
              <span style={{ color: 'var(--text-dim)' }}>Backend Server:</span>
              <strong>
                <span className={`badge ${health?.status === 'UP' ? 'badge-pass' : 'badge-fail'}`}>
                  {health?.status === 'UP' ? 'ONLINE' : 'DISCONNECTED'}
                </span>
              </strong>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', paddingBottom: '8px', borderBottom: '1px solid var(--border)' }}>
              <span style={{ color: 'var(--text-dim)' }}>Database Engine:</span>
              <span style={{ color: '#fff', fontWeight: 600 }}>{health?.database || 'Persistent Database'}</span>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', paddingBottom: '8px', borderBottom: '1px solid var(--border)' }}>
              <span style={{ color: 'var(--text-dim)' }}>Storage Location:</span>
              <span style={{ color: '#60a5fa', fontFamily: 'var(--font-mono)' }}>{health?.persistenceMode || './data/studentgradedb.mv.db'}</span>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', paddingBottom: '8px', borderBottom: '1px solid var(--border)' }}>
              <span style={{ color: 'var(--text-dim)' }}>Swagger / OpenAPI:</span>
              <a href="/swagger-ui.html" target="_blank" rel="noreferrer" style={{ fontWeight: 600 }}>
                /swagger-ui.html ↗
              </a>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', paddingBottom: '8px', borderBottom: '1px solid var(--border)' }}>
              <span style={{ color: 'var(--text-dim)' }}>H2 Web Console:</span>
              <a href="/h2-console" target="_blank" rel="noreferrer" style={{ fontWeight: 600 }}>
                /h2-console ↗
              </a>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-dim)' }}>Formula Injection Shield:</span>
              <span className="badge badge-pass">ENABLED (OWASP Sanitizer)</span>
            </div>
          </div>
        </div>

        {/* Demo & Reset Controls Card */}
        <div className="card">
          <div className="card-header">
            <div className="card-title">
              <span>⚡</span> Internship Evaluation & Demo Tools
            </div>
          </div>

          <p style={{ color: 'var(--text-muted)', fontSize: '13px', lineHeight: '1.6', marginBottom: '20px' }}>
            Instant testing utilities to seed standard acceptance test records or reset database tables during presentation:
          </p>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            {/* Load Phase 17 Acceptance Test Data */}
            <div
              style={{
                background: 'rgba(37, 99, 235, 0.08)',
                border: '1px solid rgba(37, 99, 235, 0.3)',
                borderRadius: '8px',
                padding: '14px',
              }}
            >
              <h4 style={{ fontSize: '13.5px', fontWeight: 700, color: '#60a5fa', marginBottom: '4px' }}>
                Load Phase 17 Acceptance Dataset
              </h4>
              <p style={{ fontSize: '12px', color: 'var(--text-muted)', marginBottom: '10px' }}>
                Populates the 5 students (Alice S101, Bob S102, Charlie S103, David S104, Eva S105) with exact expected averages (73.60%) and full grade spectrum (A, B, C, D, F).
              </p>
              <button
                type="button"
                className="btn btn-primary btn-sm"
                onClick={handleLoadAcceptanceData}
                disabled={loading}
              >
                <span>⚡ Load 5 Acceptance Students</span>
              </button>
            </div>

            {/* Clear All Records */}
            <div
              style={{
                background: 'rgba(239, 68, 68, 0.06)',
                border: '1px solid rgba(239, 68, 68, 0.25)',
                borderRadius: '8px',
                padding: '14px',
              }}
            >
              <h4 style={{ fontSize: '13.5px', fontWeight: 700, color: '#f87171', marginBottom: '4px' }}>
                Clear Student Records
              </h4>
              <p style={{ fontSize: '12px', color: 'var(--text-muted)', marginBottom: '10px' }}>
                Resets the student database to empty state. Subjects and grade scale configurations will be preserved.
              </p>
              <button
                type="button"
                className="btn btn-danger btn-sm"
                onClick={() => setIsClearConfirmOpen(true)}
                disabled={loading}
              >
                <span>🗑️ Clear All Student Data</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Clear Confirmation Modal */}
      {isClearConfirmOpen && (
        <ConfirmDialog
          isOpen={isClearConfirmOpen}
          onClose={() => setIsClearConfirmOpen(false)}
          onConfirm={handleClearAll}
          title="Reset All Student Data"
          message="Are you sure you want to clear all student records from the database? This is typically used for test resets."
          confirmText="Yes, Clear All Data"
          confirmColor="danger"
          loading={loading}
        />
      )}
    </div>
  );
}
