import React, { useState, useEffect } from 'react';
import Modal from './Modal';

export default function StudentFormModal({
  isOpen,
  onClose,
  onSave,
  student = null,
  subjects = [],
  loading = false,
}) {
  const [formData, setFormData] = useState({
    studentId: '',
    name: '',
    className: '',
    email: '',
    marks: '',
    mode: 'overall', // 'overall' or 'subject'
    subjectMarks: {},
  });

  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (student) {
      const hasSubjectMarks = student.subjectMarks && student.subjectMarks.length > 0;
      const subMarkMap = {};
      if (hasSubjectMarks) {
        student.subjectMarks.forEach((sm) => {
          subMarkMap[sm.subjectId] = sm.marksObtained;
        });
      }

      setFormData({
        studentId: student.studentId || '',
        name: student.name || '',
        className: student.className || '',
        email: student.email || '',
        marks: student.marks !== null && student.marks !== undefined ? String(student.marks) : '',
        mode: hasSubjectMarks ? 'subject' : 'overall',
        subjectMarks: subMarkMap,
      });
    } else {
      // Default empty form
      const defaultSubMarks = {};
      subjects.forEach((s) => {
        defaultSubMarks[s.id] = '';
      });

      setFormData({
        studentId: '',
        name: '',
        className: '10A',
        email: '',
        marks: '',
        mode: 'overall',
        subjectMarks: defaultSubMarks,
      });
    }
    setErrors({});
  }, [student, subjects, isOpen]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: null }));
    }
  };

  const handleSubjectMarkChange = (subjectId, value) => {
    setFormData((prev) => ({
      ...prev,
      subjectMarks: {
        ...prev.subjectMarks,
        [subjectId]: value,
      },
    }));
  };

  const validate = () => {
    const newErrors = {};
    if (!formData.studentId.trim()) {
      newErrors.studentId = 'Student ID is required.';
    }
    if (!formData.name.trim()) {
      newErrors.name = 'Full name is required.';
    }
    if (!formData.className.trim()) {
      newErrors.className = 'Class or section is required.';
    }

    if (formData.mode === 'overall') {
      if (formData.marks === '' || formData.marks === null) {
        newErrors.marks = 'Marks are required.';
      } else {
        const num = parseFloat(formData.marks);
        if (isNaN(num)) {
          newErrors.marks = 'Marks must be a valid number.';
        } else if (num < 0 || num > 100) {
          newErrors.marks = 'Marks must be between 0 and 100.';
        }
      }
    } else {
      // Subject mode validation
      for (const sub of subjects) {
        const val = formData.subjectMarks[sub.id];
        if (val === '' || val === undefined) {
          newErrors[`subject_${sub.id}`] = `Marks required for ${sub.name}`;
        } else {
          const num = parseFloat(val);
          if (isNaN(num) || num < 0 || num > sub.maxMarks) {
            newErrors[`subject_${sub.id}`] = `Must be between 0 and ${sub.maxMarks}`;
          }
        }
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!validate()) return;

    const payload = {
      studentId: formData.studentId.trim(),
      name: formData.name.trim(),
      className: formData.className.trim(),
      email: formData.email.trim() || null,
    };

    if (formData.mode === 'overall') {
      payload.marks = parseFloat(formData.marks);
      payload.subjectMarks = [];
    } else {
      payload.marks = null;
      payload.subjectMarks = Object.entries(formData.subjectMarks).map(([subId, marksVal]) => ({
        subjectId: parseInt(subId, 10),
        marksObtained: parseFloat(marksVal),
      }));
    }

    onSave(payload);
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={student ? `Edit Student: ${student.studentId}` : 'Add New Student'}
      maxWidth="560px"
    >
      <form onSubmit={handleSubmit}>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px' }}>
          <div className="form-group">
            <label className="form-label">Student ID / Roll No *</label>
            <input
              type="text"
              name="studentId"
              className="form-input"
              placeholder="e.g. S101"
              value={formData.studentId}
              onChange={handleChange}
              disabled={loading}
            />
            {errors.studentId && <div className="form-error">{errors.studentId}</div>}
          </div>

          <div className="form-group">
            <label className="form-label">Class / Section *</label>
            <input
              type="text"
              name="className"
              className="form-input"
              placeholder="e.g. 10A"
              value={formData.className}
              onChange={handleChange}
              disabled={loading}
            />
            {errors.className && <div className="form-error">{errors.className}</div>}
          </div>
        </div>

        <div className="form-group">
          <label className="form-label">Student Full Name *</label>
          <input
            type="text"
            name="name"
            className="form-input"
            placeholder="e.g. Alice Johnson"
            value={formData.name}
            onChange={handleChange}
            disabled={loading}
          />
          {errors.name && <div className="form-error">{errors.name}</div>}
        </div>

        <div className="form-group">
          <label className="form-label">Email Address (Optional)</label>
          <input
            type="email"
            name="email"
            className="form-input"
            placeholder="e.g. student@school.edu"
            value={formData.email}
            onChange={handleChange}
            disabled={loading}
          />
        </div>

        {/* Grading Mode Toggle */}
        <div style={{ margin: '18px 0 14px' }}>
          <div style={{ display: 'flex', gap: '8px', marginBottom: '12px' }}>
            <button
              type="button"
              className={`btn btn-sm ${formData.mode === 'overall' ? 'btn-primary' : 'btn-secondary'}`}
              onClick={() => setFormData((p) => ({ ...p, mode: 'overall' }))}
            >
              Overall Marks (0-100)
            </button>
            <button
              type="button"
              className={`btn btn-sm ${formData.mode === 'subject' ? 'btn-primary' : 'btn-secondary'}`}
              onClick={() => setFormData((p) => ({ ...p, mode: 'subject' }))}
              disabled={subjects.length === 0}
            >
              Subject-Wise Breakdown {subjects.length === 0 && '(No Subjects)'}
            </button>
          </div>

          {formData.mode === 'overall' ? (
            <div className="form-group">
              <label className="form-label">Overall Marks (0.0 to 100.0) *</label>
              <input
                type="number"
                step="0.1"
                min="0"
                max="100"
                name="marks"
                className="form-input"
                placeholder="e.g. 94.5"
                value={formData.marks}
                onChange={handleChange}
                disabled={loading}
              />
              {errors.marks && <div className="form-error">{errors.marks}</div>}
            </div>
          ) : (
            <div style={{ background: 'rgba(0,0,0,0.2)', padding: '12px', borderRadius: '8px' }}>
              <p style={{ fontSize: '12px', color: 'var(--text-muted)', marginBottom: '10px' }}>
                Enter marks obtained for each curriculum subject:
              </p>
              {subjects.map((sub) => (
                <div
                  key={sub.id}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    marginBottom: '8px',
                    gap: '12px',
                  }}
                >
                  <span style={{ fontSize: '13px' }}>
                    <strong>{sub.code}</strong> - {sub.name} (Max: {sub.maxMarks}, Pass: {sub.passMarks})
                  </span>
                  <div style={{ width: '100px' }}>
                    <input
                      type="number"
                      step="0.5"
                      min="0"
                      max={sub.maxMarks}
                      className="form-input"
                      placeholder="0"
                      value={formData.subjectMarks[sub.id] || ''}
                      onChange={(e) => handleSubjectMarkChange(sub.id, e.target.value)}
                    />
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '20px' }}>
          <button type="button" className="btn btn-secondary" onClick={onClose} disabled={loading}>
            Cancel
          </button>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Saving Record...' : student ? 'Update Student' : 'Save Student'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
