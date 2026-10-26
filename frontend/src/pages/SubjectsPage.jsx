import React, { useState, useEffect } from 'react';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import { api } from '../services/api';

export default function SubjectsPage({ addToast }) {
  const [subjects, setSubjects] = useState([]);
  const [loading, setLoading] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingSubject, setEditingSubject] = useState(null);
  const [deleteConfirmSubject, setDeleteConfirmSubject] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);

  const [formData, setFormData] = useState({
    code: '',
    name: '',
    maxMarks: '100',
    passMarks: '40',
    isCompulsory: true,
  });
  const [errors, setErrors] = useState({});

  const fetchSubjects = async () => {
    try {
      setLoading(true);
      const data = await api.getSubjects();
      setSubjects(data || []);
    } catch (err) {
      addToast(err.message || 'Failed to load subjects', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSubjects();
  }, []);

  const openAddModal = () => {
    setEditingSubject(null);
    setFormData({
      code: '',
      name: '',
      maxMarks: '100',
      passMarks: '40',
      isCompulsory: true,
    });
    setErrors({});
    setIsModalOpen(true);
  };

  const openEditModal = (sub) => {
    setEditingSubject(sub);
    setFormData({
      code: sub.code,
      name: sub.name,
      maxMarks: String(sub.maxMarks),
      passMarks: String(sub.passMarks),
      isCompulsory: sub.isCompulsory,
    });
    setErrors({});
    setIsModalOpen(true);
  };

  const validate = () => {
    const errs = {};
    if (!formData.code.trim()) errs.code = 'Subject code is required.';
    if (!formData.name.trim()) errs.name = 'Subject name is required.';
    const max = parseFloat(formData.maxMarks);
    const pass = parseFloat(formData.passMarks);

    if (isNaN(max) || max <= 0) errs.maxMarks = 'Max marks must be greater than 0.';
    if (isNaN(pass) || pass <= 0) errs.passMarks = 'Pass marks must be greater than 0.';
    if (pass > max) errs.passMarks = 'Pass marks cannot exceed max marks.';

    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSave = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    const payload = {
      code: formData.code.trim().toUpperCase(),
      name: formData.name.trim(),
      maxMarks: parseFloat(formData.maxMarks),
      passMarks: parseFloat(formData.passMarks),
      isCompulsory: formData.isCompulsory,
    };

    try {
      setActionLoading(true);
      if (editingSubject) {
        await api.updateSubject(editingSubject.id, payload);
        addToast(`Subject '${payload.name}' updated!`, 'success');
      } else {
        await api.createSubject(payload);
        addToast(`Subject '${payload.name}' created!`, 'success');
      }
      setIsModalOpen(false);
      fetchSubjects();
    } catch (err) {
      addToast(err.message || 'Failed to save subject', 'error');
    } finally {
      setActionLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteConfirmSubject) return;
    try {
      setActionLoading(true);
      await api.deleteSubject(deleteConfirmSubject.id);
      addToast(`Subject '${deleteConfirmSubject.name}' deleted!`, 'success');
      setDeleteConfirmSubject(null);
      fetchSubjects();
    } catch (err) {
      addToast(err.message || 'Cannot delete subject', 'error');
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <div className="page-content">
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '22px' }}>
        <div>
          <h2 style={{ fontSize: '20px', fontWeight: 800 }}>Curriculum Subjects & Passing Criteria</h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '13px' }}>
            Configure maximum marks, minimum passing benchmarks, and compulsory requirements
          </p>
        </div>

        <button type="button" className="btn btn-primary" onClick={openAddModal}>
          <span>+ Add Subject</span>
        </button>
      </div>

      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <div className="table-container" style={{ border: 'none' }}>
          <table className="data-table">
            <thead>
              <tr>
                <th style={{ width: '120px' }}>Subject Code</th>
                <th>Subject Name</th>
                <th style={{ textAlign: 'right' }}>Max Marks</th>
                <th style={{ textAlign: 'right' }}>Passing Marks</th>
                <th style={{ textAlign: 'center' }}>Requirement</th>
                <th style={{ textAlign: 'center', width: '140px' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="6" style={{ textAlign: 'center', padding: '36px', color: 'var(--text-muted)' }}>
                    Loading subjects...
                  </td>
                </tr>
              ) : subjects.length > 0 ? (
                subjects.map((sub) => (
                  <tr key={sub.id}>
                    <td style={{ fontFamily: 'var(--font-mono)', fontWeight: 700, color: '#60a5fa' }}>
                      {sub.code}
                    </td>
                    <td style={{ fontWeight: 600 }}>{sub.name}</td>
                    <td style={{ textAlign: 'right', fontFamily: 'var(--font-mono)' }}>{sub.maxMarks.toFixed(1)}</td>
                    <td style={{ textAlign: 'right', fontFamily: 'var(--font-mono)' }}>{sub.passMarks.toFixed(1)}</td>
                    <td style={{ textAlign: 'center' }}>
                      <span className={`badge ${sub.isCompulsory ? 'badge-a' : 'badge-b'}`}>
                        {sub.isCompulsory ? 'Compulsory' : 'Elective'}
                      </span>
                    </td>
                    <td style={{ textAlign: 'center' }}>
                      <div style={{ display: 'inline-flex', gap: '6px' }}>
                        <button
                          type="button"
                          className="btn btn-secondary btn-sm"
                          onClick={() => openEditModal(sub)}
                        >
                          ✏️ Edit
                        </button>
                        <button
                          type="button"
                          className="btn btn-danger btn-sm"
                          onClick={() => setDeleteConfirmSubject(sub)}
                        >
                          🗑️
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="6" style={{ textAlign: 'center', padding: '36px', color: 'var(--text-muted)' }}>
                    No subjects configured yet. Click "+ Add Subject" to create one.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Add / Edit Modal */}
      {isModalOpen && (
        <Modal
          isOpen={isModalOpen}
          onClose={() => setIsModalOpen(false)}
          title={editingSubject ? `Edit Subject: ${editingSubject.code}` : 'Add New Subject'}
          maxWidth="480px"
        >
          <form onSubmit={handleSave}>
            <div className="form-group">
              <label className="form-label">Subject Code *</label>
              <input
                type="text"
                className="form-input"
                placeholder="e.g. ENG101"
                value={formData.code}
                onChange={(e) => setFormData((p) => ({ ...p, code: e.target.value }))}
                disabled={actionLoading}
              />
              {errors.code && <div className="form-error">{errors.code}</div>}
            </div>

            <div className="form-group">
              <label className="form-label">Subject Name *</label>
              <input
                type="text"
                className="form-input"
                placeholder="e.g. English Language"
                value={formData.name}
                onChange={(e) => setFormData((p) => ({ ...p, name: e.target.value }))}
                disabled={actionLoading}
              />
              {errors.name && <div className="form-error">{errors.name}</div>}
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px' }}>
              <div className="form-group">
                <label className="form-label">Max Marks *</label>
                <input
                  type="number"
                  step="1"
                  min="1"
                  className="form-input"
                  value={formData.maxMarks}
                  onChange={(e) => setFormData((p) => ({ ...p, maxMarks: e.target.value }))}
                  disabled={actionLoading}
                />
                {errors.maxMarks && <div className="form-error">{errors.maxMarks}</div>}
              </div>

              <div className="form-group">
                <label className="form-label">Passing Marks *</label>
                <input
                  type="number"
                  step="1"
                  min="1"
                  className="form-input"
                  value={formData.passMarks}
                  onChange={(e) => setFormData((p) => ({ ...p, passMarks: e.target.value }))}
                  disabled={actionLoading}
                />
                {errors.passMarks && <div className="form-error">{errors.passMarks}</div>}
              </div>
            </div>

            <div className="form-group">
              <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', fontSize: '13px' }}>
                <input
                  type="checkbox"
                  checked={formData.isCompulsory}
                  onChange={(e) => setFormData((p) => ({ ...p, isCompulsory: e.target.checked }))}
                />
                <span>Compulsory Subject (Students must pass this subject to pass overall)</span>
              </label>
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '16px' }}>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setIsModalOpen(false)}
                disabled={actionLoading}
              >
                Cancel
              </button>
              <button type="submit" className="btn btn-primary" disabled={actionLoading}>
                {actionLoading ? 'Saving...' : editingSubject ? 'Update Subject' : 'Save Subject'}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {/* Delete Confirmation */}
      {deleteConfirmSubject && (
        <ConfirmDialog
          isOpen={Boolean(deleteConfirmSubject)}
          onClose={() => setDeleteConfirmSubject(null)}
          onConfirm={handleDelete}
          title="Delete Subject"
          message={`Are you sure you want to delete subject '${deleteConfirmSubject.name}' (${deleteConfirmSubject.code})? It can only be deleted if no student marks are recorded.`}
          confirmText="Yes, Delete Subject"
          confirmColor="danger"
          loading={actionLoading}
        />
      )}
    </div>
  );
}
