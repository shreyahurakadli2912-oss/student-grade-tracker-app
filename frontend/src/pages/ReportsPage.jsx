import React, { useState, useEffect } from 'react';
import { api } from '../services/api';

export default function ReportsPage({ addToast }) {
  const [tab, setTab] = useState('class'); // 'class' or 'individual'
  const [students, setStudents] = useState([]);
  const [classes, setClasses] = useState([]);
  const [selectedClass, setSelectedClass] = useState('ALL');
  const [selectedGrade, setSelectedGrade] = useState('ALL');
  const [selectedStatus, setSelectedStatus] = useState('ALL');
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(false);

  // Individual Report state
  const [selectedStudentId, setSelectedStudentId] = useState('');
  const [studentReport, setStudentReport] = useState(null);

  const fetchClassReportData = async () => {
    try {
      setLoading(true);
      const [reportData, classList] = await Promise.all([
        api.getClassReport({
          className: selectedClass,
          grade: selectedGrade,
          status: selectedStatus,
        }),
        api.getClassNames(),
      ]);

      setStudents(reportData?.students || []);
      setStats(reportData?.statistics || null);
      setClasses(classList || []);

      if (reportData?.students && reportData.students.length > 0 && !selectedStudentId) {
        setSelectedStudentId(reportData.students[0].id);
      }
    } catch (err) {
      addToast(err.message || 'Error generating class report', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchClassReportData();
  }, [selectedClass, selectedGrade, selectedStatus]);

  useEffect(() => {
    if (selectedStudentId) {
      api.getStudentReport(selectedStudentId)
        .then(setStudentReport)
        .catch((err) => addToast(err.message, 'error'));
    }
  }, [selectedStudentId]);

  return (
    <div className="page-content">
      {/* Top Banner & Tab Controls */}
      <div className="no-print" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '22px' }}>
        <div>
          <h2 style={{ fontSize: '20px', fontWeight: 800 }}>Official Grade Reports & Transcripts</h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '13px' }}>
            Generate, print, and export individual student report cards and comprehensive class transcripts
          </p>
        </div>

        {/* Tab switch */}
        <div style={{ display: 'flex', gap: '8px', background: 'var(--bg-card)', padding: '4px', borderRadius: '8px', border: '1px solid var(--border)' }}>
          <button
            type="button"
            className={`btn btn-sm ${tab === 'class' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setTab('class')}
          >
            📊 Class Grade Report
          </button>
          <button
            type="button"
            className={`btn btn-sm ${tab === 'individual' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setTab('individual')}
          >
            🎓 Individual Student Transcript
          </button>
        </div>
      </div>

      {/* TAB 1: CLASS GRADE REPORT */}
      {tab === 'class' && (
        <div>
          {/* Controls & Export Buttons Bar */}
          <div className="card no-print" style={{ marginBottom: '20px', padding: '16px' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '14px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }}>
                {/* Class selector */}
                <select
                  className="form-select"
                  style={{ width: '150px' }}
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

                {/* Grade selector */}
                <select
                  className="form-select"
                  style={{ width: '130px' }}
                  value={selectedGrade}
                  onChange={(e) => setSelectedGrade(e.target.value)}
                >
                  <option value="ALL">All Grades</option>
                  <option value="A">Grade A</option>
                  <option value="B">Grade B</option>
                  <option value="C">Grade C</option>
                  <option value="D">Grade D</option>
                  <option value="F">Grade F</option>
                </select>

                {/* Status selector */}
                <select
                  className="form-select"
                  style={{ width: '130px' }}
                  value={selectedStatus}
                  onChange={(e) => setSelectedStatus(e.target.value)}
                >
                  <option value="ALL">All Status</option>
                  <option value="PASS">Pass Only</option>
                  <option value="FAIL">Fail Only</option>
                </select>
              </div>

              {/* Real Export actions */}
              <div style={{ display: 'flex', gap: '8px' }}>
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  onClick={() => api.exportClassPdf({ className: selectedClass, grade: selectedGrade, status: selectedStatus })}
                >
                  <span>📕 Download PDF</span>
                </button>
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  onClick={() => api.exportExcel({ className: selectedClass, grade: selectedGrade, status: selectedStatus })}
                >
                  <span>📊 Export Excel</span>
                </button>
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  onClick={() => api.exportCsv({ className: selectedClass, grade: selectedGrade, status: selectedStatus })}
                >
                  <span>📄 Export CSV</span>
                </button>
                <button
                  type="button"
                  className="btn btn-primary btn-sm"
                  onClick={() => window.print()}
                >
                  <span>🖨️ Print Report</span>
                </button>
              </div>
            </div>
          </div>

          {/* Printable Report Header */}
          <div style={{ padding: '16px 0', textAlign: 'center', marginBottom: '16px' }}>
            <h1 style={{ fontSize: '22px', fontWeight: 800, color: '#fff', textTransform: 'uppercase' }}>
              Student Grade Management System
            </h1>
            <p style={{ fontSize: '13.5px', color: 'var(--text-muted)' }}>
              Comprehensive Academic Result & Performance Record — {selectedClass === 'ALL' ? 'All Classes' : `Class ${selectedClass}`}
            </p>
          </div>

          {/* Statistics summary bar */}
          {stats && (
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(5, 1fr)',
                gap: '12px',
                marginBottom: '20px',
                padding: '14px',
                background: 'rgba(30, 41, 59, 0.6)',
                borderRadius: '8px',
                border: '1px solid var(--border)',
                textAlign: 'center',
              }}
            >
              <div>
                <div style={{ fontSize: '11px', color: 'var(--text-dim)' }}>TOTAL STUDENTS</div>
                <div style={{ fontSize: '18px', fontWeight: 700, color: '#fff' }}>{stats.totalStudents}</div>
              </div>
              <div>
                <div style={{ fontSize: '11px', color: 'var(--text-dim)' }}>CLASS AVERAGE</div>
                <div style={{ fontSize: '18px', fontWeight: 700, color: '#60a5fa' }}>{stats.averagePercentage.toFixed(2)}%</div>
              </div>
              <div>
                <div style={{ fontSize: '11px', color: 'var(--text-dim)' }}>HIGHEST SCORE</div>
                <div style={{ fontSize: '18px', fontWeight: 700, color: '#34d399' }}>{stats.highestMarks.toFixed(2)}</div>
              </div>
              <div>
                <div style={{ fontSize: '11px', color: 'var(--text-dim)' }}>LOWEST SCORE</div>
                <div style={{ fontSize: '18px', fontWeight: 700, color: '#f87171' }}>{stats.lowestMarks.toFixed(2)}</div>
              </div>
              <div>
                <div style={{ fontSize: '11px', color: 'var(--text-dim)' }}>PASS RATE</div>
                <div style={{ fontSize: '18px', fontWeight: 700, color: '#fbbf24' }}>{stats.passPercentage.toFixed(1)}%</div>
              </div>
            </div>
          )}

          {/* Class Results Table */}
          <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
            <div className="table-container" style={{ border: 'none' }}>
              <table className="data-table">
                <thead>
                  <tr>
                    <th style={{ width: '60px', textAlign: 'center' }}>Rank</th>
                    <th>Student ID</th>
                    <th>Full Name</th>
                    <th>Class</th>
                    <th style={{ textAlign: 'right' }}>Marks</th>
                    <th style={{ textAlign: 'right' }}>Max</th>
                    <th style={{ textAlign: 'right' }}>Percentage</th>
                    <th style={{ textAlign: 'center' }}>Grade</th>
                    <th style={{ textAlign: 'center' }}>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {loading ? (
                    <tr>
                      <td colSpan="9" style={{ textAlign: 'center', padding: '36px' }}>
                        Loading class report...
                      </td>
                    </tr>
                  ) : students.length > 0 ? (
                    students.map((s) => (
                      <tr key={s.id}>
                        <td style={{ textAlign: 'center' }}>
                          <span className={`rank-badge rank-${s.rank <= 3 ? s.rank : 'other'}`}>
                            {s.rank || '-'}
                          </span>
                        </td>
                        <td style={{ fontFamily: 'var(--font-mono)', fontWeight: 600 }}>{s.studentId}</td>
                        <td style={{ fontWeight: 600 }}>{s.name}</td>
                        <td>{s.className}</td>
                        <td style={{ textAlign: 'right', fontFamily: 'var(--font-mono)' }}>
                          {s.marks != null ? s.marks.toFixed(2) : '-'}
                        </td>
                        <td style={{ textAlign: 'right', fontFamily: 'var(--font-mono)' }}>
                          {s.totalMaxMarks != null ? s.totalMaxMarks.toFixed(2) : '100.00'}
                        </td>
                        <td style={{ textAlign: 'right', fontWeight: 700, fontFamily: 'var(--font-mono)' }}>
                          {s.percentage.toFixed(2)}%
                        </td>
                        <td style={{ textAlign: 'center' }}>
                          <span className={`badge badge-${s.grade.toLowerCase()}`}>{s.grade}</span>
                        </td>
                        <td style={{ textAlign: 'center' }}>
                          <span className={`badge badge-${s.status.toLowerCase()}`}>{s.status}</span>
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan="9" style={{ textAlign: 'center', padding: '36px', color: 'var(--text-muted)' }}>
                        No students found for selected criteria.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* TAB 2: INDIVIDUAL STUDENT REPORT CARD */}
      {tab === 'individual' && (
        <div>
          {/* Student Selector Card */}
          <div className="card no-print" style={{ marginBottom: '24px', padding: '16px' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '14px', flexWrap: 'wrap' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <label style={{ fontSize: '13px', fontWeight: 600 }}>Select Student:</label>
                <select
                  className="form-select"
                  style={{ width: '260px' }}
                  value={selectedStudentId}
                  onChange={(e) => setSelectedStudentId(e.target.value)}
                >
                  {students.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.studentId} - {s.name} ({s.className})
                    </option>
                  ))}
                </select>
              </div>

              {studentReport && (
                <div style={{ display: 'flex', gap: '10px' }}>
                  <button
                    type="button"
                    className="btn btn-secondary btn-sm"
                    onClick={() => api.exportStudentPdf(studentReport.id, studentReport.studentId)}
                  >
                    <span>📕 Download PDF Transcript</span>
                  </button>
                  <button
                    type="button"
                    className="btn btn-primary btn-sm"
                    onClick={() => window.print()}
                  >
                    <span>🖨️ Print Transcript</span>
                  </button>
                </div>
              )}
            </div>
          </div>

          {/* Official Report Card Canvas */}
          {studentReport ? (
            <div
              className="card"
              style={{
                maxWidth: '750px',
                margin: '0 auto',
                padding: '36px',
                background: 'linear-gradient(180deg, rgba(30, 41, 59, 0.8), rgba(15, 23, 42, 0.95))',
                border: '1px solid rgba(255, 255, 255, 0.15)',
                borderRadius: '12px',
                boxShadow: 'var(--shadow-lg)',
              }}
            >
              {/* Report Header */}
              <div style={{ textAlign: 'center', borderBottom: '2px solid var(--primary)', paddingBottom: '20px', marginBottom: '24px' }}>
                <div style={{ fontSize: '32px', marginBottom: '4px' }}>🎓</div>
                <h1 style={{ fontSize: '24px', fontWeight: 800, color: '#fff', letterSpacing: '0.03em' }}>
                  STUDENT GRADE REPORT
                </h1>
                <p style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
                  Academic Performance Evaluation & Official Progress Transcript
                </p>
              </div>

              {/* Student Metadata Box */}
              <div
                style={{
                  display: 'grid',
                  gridTemplateColumns: '1fr 1fr',
                  gap: '12px',
                  background: 'rgba(0, 0, 0, 0.25)',
                  padding: '18px',
                  borderRadius: '8px',
                  marginBottom: '24px',
                  fontSize: '13.5px',
                }}
              >
                <div>
                  <span style={{ color: 'var(--text-dim)', marginRight: '8px' }}>Student ID:</span>
                  <strong style={{ fontFamily: 'var(--font-mono)', color: '#60a5fa' }}>{studentReport.studentId}</strong>
                </div>
                <div>
                  <span style={{ color: 'var(--text-dim)', marginRight: '8px' }}>Full Name:</span>
                  <strong style={{ color: '#fff' }}>{studentReport.name}</strong>
                </div>
                <div>
                  <span style={{ color: 'var(--text-dim)', marginRight: '8px' }}>Class / Section:</span>
                  <span style={{ color: '#fff' }}>{studentReport.className}</span>
                </div>
                <div>
                  <span style={{ color: 'var(--text-dim)', marginRight: '8px' }}>Email:</span>
                  <span style={{ color: '#fff' }}>{studentReport.email || 'N/A'}</span>
                </div>
                <div>
                  <span style={{ color: 'var(--text-dim)', marginRight: '8px' }}>Class Standing:</span>
                  <strong style={{ color: '#fbbf24' }}>Rank #{studentReport.rank || 1}</strong>
                </div>
                <div>
                  <span style={{ color: 'var(--text-dim)', marginRight: '8px' }}>Date Issued:</span>
                  <span style={{ color: '#fff' }}>{new Date().toLocaleDateString()}</span>
                </div>
              </div>

              {/* Subject Breakdown if exists */}
              {studentReport.subjectMarks && studentReport.subjectMarks.length > 0 ? (
                <div style={{ marginBottom: '24px' }}>
                  <h4 style={{ fontSize: '14px', fontWeight: 700, marginBottom: '10px', color: '#60a5fa' }}>
                    Subject-Wise Breakdown
                  </h4>
                  <div className="table-container" style={{ border: 'none' }}>
                    <table className="data-table">
                      <thead>
                        <tr>
                          <th>Code</th>
                          <th>Subject Name</th>
                          <th style={{ textAlign: 'right' }}>Max</th>
                          <th style={{ textAlign: 'right' }}>Pass Mark</th>
                          <th style={{ textAlign: 'right' }}>Obtained</th>
                          <th style={{ textAlign: 'center' }}>Result</th>
                        </tr>
                      </thead>
                      <tbody>
                        {studentReport.subjectMarks.map((sm) => (
                          <tr key={sm.subjectId}>
                            <td style={{ fontFamily: 'var(--font-mono)', color: '#60a5fa' }}>{sm.subjectCode}</td>
                            <td>{sm.subjectName}</td>
                            <td style={{ textAlign: 'right', fontFamily: 'var(--font-mono)' }}>{sm.maxMarks.toFixed(1)}</td>
                            <td style={{ textAlign: 'right', fontFamily: 'var(--font-mono)' }}>{sm.passMarks.toFixed(1)}</td>
                            <td style={{ textAlign: 'right', fontFamily: 'var(--font-mono)', fontWeight: 600 }}>{sm.marksObtained.toFixed(1)}</td>
                            <td style={{ textAlign: 'center' }}>
                              <span className={`badge ${sm.isPassed ? 'badge-pass' : 'badge-fail'}`}>
                                {sm.isPassed ? 'PASS' : 'FAIL'}
                              </span>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              ) : null}

              {/* Final Scorecard Banner */}
              <div
                style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(4, 1fr)',
                  gap: '12px',
                  padding: '16px',
                  background: 'rgba(37, 99, 235, 0.08)',
                  border: '1px solid rgba(37, 99, 235, 0.3)',
                  borderRadius: '10px',
                  textAlign: 'center',
                  marginBottom: '32px',
                }}
              >
                <div>
                  <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>TOTAL SCORE</div>
                  <div style={{ fontSize: '18px', fontWeight: 800, color: '#fff' }}>
                    {studentReport.marks != null ? studentReport.marks.toFixed(2) : '-'} / {studentReport.totalMaxMarks || 100}
                  </div>
                </div>
                <div>
                  <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>PERCENTAGE</div>
                  <div style={{ fontSize: '18px', fontWeight: 800, color: '#60a5fa' }}>
                    {studentReport.percentage.toFixed(2)}%
                  </div>
                </div>
                <div>
                  <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>FINAL GRADE</div>
                  <div style={{ fontSize: '20px', fontWeight: 800, color: '#fff' }}>
                    <span className={`badge badge-${studentReport.grade.toLowerCase()}`} style={{ fontSize: '15px' }}>
                      Grade {studentReport.grade}
                    </span>
                  </div>
                </div>
                <div>
                  <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>ACADEMIC STATUS</div>
                  <div style={{ fontSize: '18px', fontWeight: 800 }}>
                    <span className={`badge badge-${studentReport.status.toLowerCase()}`} style={{ fontSize: '14px' }}>
                      {studentReport.status}
                    </span>
                  </div>
                </div>
              </div>

              {/* Signatures */}
              <div
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  paddingTop: '36px',
                  borderTop: '1px solid var(--border)',
                }}
              >
                <div style={{ textAlign: 'center' }}>
                  <div style={{ width: '180px', borderBottom: '1px solid var(--text-dim)', marginBottom: '6px' }} />
                  <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Class Teacher Signature</span>
                </div>

                <div style={{ textAlign: 'center' }}>
                  <div style={{ width: '180px', borderBottom: '1px solid var(--text-dim)', marginBottom: '6px' }} />
                  <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Principal / Academic Head</span>
                </div>
              </div>
            </div>
          ) : (
            <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
              Select a student to view their official report card.
            </div>
          )}
        </div>
      )}
    </div>
  );
}
