import React from 'react';
import Modal from './Modal';

export default function ConfirmDialog({
  isOpen,
  onClose,
  onConfirm,
  title = 'Confirm Action',
  message,
  studentDetails,
  confirmText = 'Delete Permanently',
  confirmColor = 'danger',
  loading = false,
}) {
  return (
    <Modal isOpen={isOpen} onClose={onClose} title={title} maxWidth="480px">
      <div style={{ marginBottom: '20px' }}>
        <p style={{ color: 'var(--text-muted)', fontSize: '14px', marginBottom: '14px' }}>
          {message}
        </p>

        {studentDetails && (
          <div
            style={{
              background: 'rgba(239, 68, 68, 0.08)',
              border: '1px solid rgba(239, 68, 68, 0.25)',
              borderRadius: '8px',
              padding: '12px 16px',
              fontSize: '13.5px',
            }}
          >
            <div style={{ marginBottom: '4px' }}>
              <span style={{ color: 'var(--text-dim)', marginRight: '8px' }}>Student ID:</span>
              <strong style={{ color: '#fff', fontFamily: 'var(--font-mono)' }}>
                {studentDetails.studentId}
              </strong>
            </div>
            <div>
              <span style={{ color: 'var(--text-dim)', marginRight: '8px' }}>Full Name:</span>
              <strong style={{ color: '#fff' }}>{studentDetails.name}</strong>
            </div>
            {studentDetails.className && (
              <div style={{ marginTop: '4px' }}>
                <span style={{ color: 'var(--text-dim)', marginRight: '8px' }}>Class:</span>
                <span style={{ color: 'var(--text-muted)' }}>{studentDetails.className}</span>
              </div>
            )}
          </div>
        )}
      </div>

      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
        <button
          type="button"
          className="btn btn-secondary"
          onClick={onClose}
          disabled={loading}
        >
          Cancel
        </button>
        <button
          type="button"
          className={`btn btn-${confirmColor}`}
          onClick={onConfirm}
          disabled={loading}
        >
          {loading ? 'Processing...' : confirmText}
        </button>
      </div>
    </Modal>
  );
}
