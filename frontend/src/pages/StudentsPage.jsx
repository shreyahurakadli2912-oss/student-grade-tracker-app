import React, { useState, useEffect } from 'react';
import StudentFormModal from '../components/StudentFormModal';
import ConfirmDialog from '../components/ConfirmDialog';
import { api } from '../services/api';

export default function StudentsPage({ addToast, onOpenAddModal }) {
  const [students, setStudents] = useState([]);
  const [classes, setClasses] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [loading, setLoading] = useState(false);

  // Filters & Search
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedClass, setSelectedClass] = useState('ALL');
  const [selectedGrade, setSelectedGrade] = useState('ALL');
  const [selectedStatus, setSelectedStatus] = useState('ALL');
  const [sortBy, setSortBy] = useState('rank');
  const [sortDir, setSortDir] = useState('asc');

  // Pagination
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);

  // Modals state
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingStudent, setEditingStudent] = useState(null);
  const [deleteConfirmStudent, setDeleteConfirmStudent] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);

  const fetchStudents = async () => {
    try {
      setLoading(true);
      const data = await api.getStudents({
        query: searchQuery,
        className: selectedClass,
        grade: selectedGrade,
        status: selectedStatus,
        sortBy,
        sortDir,
      });
      setStudents(data || []);
      setCurrentPage(1); // reset to page 1 on filter changes
    } catch (err) {
      addToast(err.message || 'Error fetching student directory', 'error');
    } finally {
      setLoading(false);
    }
  };

  const fetchMeta = async () => {
    try {
      const [classList, subjectList] = await Promise.all([
        api.getClassNames(),
        api.getSubjects(),
      ]);
      setClasses(classList || []);
      setSubjects(subjectList || []);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    fetchMeta();
  }, []);

  useEffect(() => {
    const timer = setTimeout(() => {
      fetchStudents();
    }, 200);
    return () => clearTimeout(timer);
  }, [searchQuery, selectedClass, selectedGrade, selectedStatus, sortBy, sortDir]);

  const handleSaveStudent = async (payload) => {
    try {
      setActionLoading(true);
      if (editingStudent) {
        await api.updateStudent(editingStudent.id, payload);
        addToast(`Student '${payload.name}' updated successfully!`, 'success');
      } else {
        await api.createStudent(payload);
        addToast(`Student '${payload.name}' added successfully!`, 'success');
      }
      setIsFormOpen(false);
      setEditingStudent(null);
      fetchStudents();
      fetchMeta();
    } catch (err) {
      addToast(err.message || 'Failed to save student record', 'error');
    } finally {
      setActionLoading(false);
    }
  };

  const handleDeleteStudent = async () => {
    if (!deleteConfirmStudent) return;
    try {
      setActionLoading(true);
      await api.deleteStudent(deleteConfirmStudent.id);
      addToast(`Student '${deleteConfirmStudent.name}' deleted successfully.`, 'success');
      setDeleteConfirmStudent(null);
      fetchStudents();
      fetchMeta();
    } catch (err) {
      addToast(err.message || 'Failed to delete student', 'error');
    } finally {
      setActionLoading(false);
    }
  };

  // Pagination calculations
  const totalItems = students.length;
  const totalPages = Math.ceil(totalItems / pageSize) || 1;
  const startIndex = (currentPage - 1) * pageSize;
  const currentStudents = students.slice(startIndex, startIndex + pageSize);

  return (
    <div className="page-content">
      {/* Page Title & Add Button */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '20px' }}>
        <div>
          <h2 style={{ fontSize: '20px', fontWeight: 800 }}>Student Grade Directory</h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '13px' }}>
            Showing {totalItems} enrolled student{totalItems !== 1 ? 's' : ''} with calculated dense rankings
          </p>
        </div>

        <div style={{ display: 'flex', gap: '10px' }}>
          <button
            type="button"
            className="btn btn-primary"
            onClick={() => {
              setEditingStudent(null);
              setIsFormOpen(true);
            }}
          >
            <span>+ Add Student</span>
          </button>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="card" style={{ marginBottom: '20px', padding: '16px' }}>
        <div className="filter-bar" style={{ margin: 0 }}>
          {/* Search box */}
          <div className="search-box">
            <span className="search-icon">🔍</span>
            <input
              type="text"
              className="form-input"
              placeholder="Search by ID or Name..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>

          {/* Class filter */}
          <div style={{ width: '150px' }}>
            <select
              className="form-select"
              value={selectedClass}
              onChange={(e) => setSelectedClass(e.target.value)}
            >
              <option value="ALL">All Classes</option>
              {classes.map((c) => (
                <option key={c} value={c}>
                  Class {c}
                </option>
              ))}
            </select>
          </div>

          {/* Grade filter */}
          <div style={{ width: '130px' }}>
            <select
              className="form-select"
              value={selectedGrade}
              onChange={(e) => setSelectedGrade(e.target.value)}
            >
              <option value="ALL">All Grades</option>
              <option value="A">Grade A (90+)</option>
              <option value="B">Grade B (80-89)</option>
              <option value="C">Grade C (70-79)</option>
              <option value="D">Grade D (60-69)</option>
              <option value="F">Grade F (&lt;60)</option>
            </select>
          </div>

          {/* Status filter */}
          <div style={{ width: '130px' }}>
            <select
              className="form-select"
              value={selectedStatus}
              onChange={(e) => setSelectedStatus(e.target.value)}
            >
              <option value="ALL">All Status</option>
              <option value="PASS">Pass Only</option>
              <option value="FAIL">Fail Only</option>
            </select>
          </div>

          {/* Sort By */}
          <div style={{ width: '160px' }}>
            <select
              className="form-select"
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
            >
              <option value="rank">Sort by Rank</option>
              <option value="name">Sort by Name</option>
              <option value="percentage">Sort by Percentage</option>
              <option value="marks">Sort by Marks</option>
              <option value="studentId">Sort by Student ID</option>
              <option value="className">Sort by Class</option>
            </select>
          </div>

          {/* Asc/Desc toggle */}
          <button
            type="button"
            className="btn btn-secondary btn-icon"
            onClick={() => setSortDir((prev) => (prev === 'asc' ? 'desc' : 'asc'))}
            title={sortDir === 'asc' ? 'Ascending Order' : 'Descending Order'}
          >
            {sortDir === 'asc' ? '↑' : '↓'}
          </button>
        </div>
      </div>

      {/* Student Table */}
      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <div className="table-container" style={{ border: 'none' }}>
          <table className="data-table">
            <thead>
              <tr>
                <th style={{ width: '70px', textAlign: 'center' }}>Rank</th>
                <th>Student ID</th>
                <th>Full Name</th>
                <th>Class</th>
                <th style={{ textAlign: 'right' }}>Marks</th>
                <th style={{ textAlign: 'right' }}>Max</th>
                <th style={{ textAlign: 'right' }}>Percentage</th>
                <th style={{ textAlign: 'center' }}>Grade</th>
                <th style={{ textAlign: 'center' }}>Status</th>
                <th style={{ textAlign: 'center', width: '130px' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="10" style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }}>
                    Loading student records from database...
                  </td>
                </tr>
              ) : currentStudents.length > 0 ? (
                currentStudents.map((s) => (
                  <tr key={s.id}>
                    <td style={{ textAlign: 'center' }}>
                      <span className={`rank-badge rank-${s.rank <= 3 ? s.rank : 'other'}`}>
                        {s.rank || '-'}
                      </span>
                    </td>
                    <td style={{ fontFamily: 'var(--font-mono)', fontWeight: 600 }}>
                      {s.studentId}
                    </td>
                    <td>
                      <div style={{ fontWeight: 600 }}>{s.name}</div>
                      {s.email && <div style={{ fontSize: '11px', color: 'var(--text-dim)' }}>{s.email}</div>}
                    </td>
                    <td>{s.className}</td>
                    <td style={{ textAlign: 'right', fontFamily: 'var(--font-mono)' }}>
                      {s.marks != null ? s.marks.toFixed(2) : '-'}
                    </td>
                    <td style={{ textAlign: 'right', fontFamily: 'var(--font-mono)', color: 'var(--text-dim)' }}>
                      {s.totalMaxMarks != null ? s.totalMaxMarks.toFixed(2) : '100.00'}
                    </td>
                    <td style={{ textAlign: 'right', fontWeight: 700, fontFamily: 'var(--font-mono)' }}>
                      {s.percentage.toFixed(2)}%
                    </td>
                    <td style={{ textAlign: 'center' }}>
                      <span className={`badge badge-${s.grade.toLowerCase()}`}>
                        {s.grade}
                      </span>
                    </td>
                    <td style={{ textAlign: 'center' }}>
                      <span className={`badge badge-${s.status.toLowerCase()}`}>
                        {s.status}
                      </span>
                    </td>
                    <td style={{ textAlign: 'center' }}>
                      <div style={{ display: 'inline-flex', gap: '6px' }}>
                        <button
                          type="button"
                          className="btn btn-secondary btn-sm"
                          onClick={() => {
                            setEditingStudent(s);
                            setIsFormOpen(true);
                          }}
                          title="Edit student marks & details"
                        >
                          ✏️ Edit
                        </button>
                        <button
                          type="button"
                          className="btn btn-danger btn-sm"
                          onClick={() => setDeleteConfirmStudent(s)}
                          title="Delete student permanently"
                        >
                          🗑️
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="10" style={{ textAlign: 'center', padding: '50px 20px', color: 'var(--text-muted)' }}>
                    <div style={{ fontSize: '32px', marginBottom: '8px' }}>🔍</div>
                    <div style={{ fontSize: '15px', fontWeight: 600, color: '#fff', marginBottom: '4px' }}>
                      No students found
                    </div>
                    <div style={{ fontSize: '13px' }}>
                      No records matched the filter query '{searchQuery || selectedClass || selectedGrade}'.
                    </div>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination Bar */}
        {totalItems > 0 && (
          <div
            style={{
              padding: '14px 20px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              borderTop: '1px solid var(--border)',
              background: 'rgba(0, 0, 0, 0.2)',
            }}
          >
            <div style={{ fontSize: '12.5px', color: 'var(--text-muted)' }}>
              Showing {startIndex + 1} to {Math.min(startIndex + pageSize, totalItems)} of {totalItems} students
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                disabled={currentPage === 1}
                onClick={() => setCurrentPage((p) => Math.max(p - 1, 1))}
              >
                ◀ Prev
              </button>

              <span style={{ fontSize: '12.5px', color: '#fff', fontWeight: 600, padding: '0 8px' }}>
                Page {currentPage} of {totalPages}
              </span>

              <button
                type="button"
                className="btn btn-secondary btn-sm"
                disabled={currentPage >= totalPages}
                onClick={() => setCurrentPage((p) => Math.min(p + 1, totalPages))}
              >
                Next ▶
              </button>

              <select
                className="form-select"
                style={{ width: '90px', padding: '4px 8px', fontSize: '12px', marginLeft: '10px' }}
                value={pageSize}
                onChange={(e) => {
                  setPageSize(Number(e.target.value));
                  setCurrentPage(1);
                }}
              >
                <option value={5}>5 / page</option>
                <option value={10}>10 / page</option>
                <option value={25}>25 / page</option>
                <option value={50}>50 / page</option>
              </select>
            </div>
          </div>
        )}
      </div>

      {/* Add / Edit Student Modal */}
      {isFormOpen && (
        <StudentFormModal
          isOpen={isFormOpen}
          onClose={() => {
            setIsFormOpen(false);
            setEditingStudent(null);
          }}
          onSave={handleSaveStudent}
          student={editingStudent}
          subjects={subjects}
          loading={actionLoading}
        />
      )}

      {/* Delete Confirmation Modal */}
      {deleteConfirmStudent && (
        <ConfirmDialog
          isOpen={Boolean(deleteConfirmStudent)}
          onClose={() => setDeleteConfirmStudent(null)}
          onConfirm={handleDeleteStudent}
          title="Delete Student Record"
          message="Are you sure you want to permanently delete this student record? This action cannot be undone."
          studentDetails={{
            studentId: deleteConfirmStudent.studentId,
            name: deleteConfirmStudent.name,
            className: deleteConfirmStudent.className,
          }}
          confirmText="Yes, Delete Permanently"
          confirmColor="danger"
          loading={actionLoading}
        />
      )}
    </div>
  );
}
