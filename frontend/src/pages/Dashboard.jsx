import React, { useState, useEffect } from 'react';
import StatCard from '../components/StatCard';
import { GradeDistributionChart, PassFailDonutChart } from '../components/Charts';
import { api } from '../services/api';

export default function Dashboard({ setActivePage, onAddStudent, addToast }) {
  const [stats, setStats] = useState(null);
  const [classes, setClasses] = useState([]);
  const [selectedClass, setSelectedClass] = useState('ALL');
  const [loading, setLoading] = useState(true);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      const [statsData, classList] = await Promise.all([
        api.getStatistics(selectedClass),
        api.getClassNames(),
      ]);
      setStats(statsData);
      setClasses(classList || []);
    } catch (err) {
      addToast(err.message || 'Failed to load dashboard data', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, [selectedClass]);

  return (
    <div className="page-content">
      {/* Top Controls: Filter by Class */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '22px' }}>
        <div>
          <h2 style={{ fontSize: '20px', fontWeight: 800 }}>Academic Performance Overview</h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '13px' }}>
            Real-time grades, rankings, and distribution metrics from database
          </p>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <label style={{ fontSize: '13px', color: 'var(--text-muted)', fontWeight: 600 }}>Filter Class:</label>
          <select
            className="form-select"
            style={{ width: '160px' }}
            value={selectedClass}
            onChange={(e) => setSelectedClass(e.target.value)}
          >
            <option value="ALL">All Classes</option>
            {classes.map((cls) => (
              <option key={cls} value={cls}>
                Class {cls}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* KPI Cards Grid */}
      <div className="stat-grid">
        <StatCard
          label="Total Enrolled"
          value={stats ? stats.totalStudents : 0}
          subText={selectedClass === 'ALL' ? 'Across all classes' : `Enrolled in ${selectedClass}`}
          icon="👥"
          color="blue"
        />
        <StatCard
          label="Class Average"
          value={stats ? `${stats.averagePercentage.toFixed(2)}%` : '0.00%'}
          subText="Mean overall score"
          icon="🎯"
          color="cyan"
        />
        <StatCard
          label="Highest Score"
          value={stats ? stats.highestMarks.toFixed(2) : '0.00'}
          subText={stats?.highestScoringStudents?.[0] ? `Top: ${stats.highestScoringStudents[0]}` : 'N/A'}
          icon="🏆"
          color="green"
        />
        <StatCard
          label="Lowest Score"
          value={stats ? stats.lowestMarks.toFixed(2) : '0.00'}
          subText={stats?.lowestScoringStudents?.[0] ? `Lowest: ${stats.lowestScoringStudents[0]}` : 'N/A'}
          icon="📉"
          color="amber"
        />
        <StatCard
          label="Overall Pass Rate"
          value={stats ? `${stats.passPercentage.toFixed(2)}%` : '0.00%'}
          subText={stats ? `${stats.totalPassed} Passed, ${stats.totalFailed} Failed` : '0 Passed'}
          icon="✅"
          color={stats?.passPercentage >= 60 ? 'green' : 'red'}
        />
      </div>

      {/* Charts Row */}
      <div style={{ display: 'grid', gridTemplateColumns: '1.4fr 1fr', gap: '20px', marginBottom: '24px' }}>
        <div className="card">
          <div className="card-header">
            <div className="card-title">
              <span>📊</span> Letter Grade Distribution
            </div>
            <span style={{ fontSize: '11px', color: 'var(--text-dim)' }}>A (90+) | B (80+) | C (70+) | D (60+) | F (&lt;60)</span>
          </div>
          {loading ? (
            <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>Loading distribution...</div>
          ) : (
            <GradeDistributionChart
              distribution={stats?.gradeDistribution || {}}
              totalStudents={stats?.totalStudents || 0}
            />
          )}
        </div>

        <div className="card">
          <div className="card-header">
            <div className="card-title">
              <span>🍩</span> Pass vs. Fail Ratio
            </div>
            <span style={{ fontSize: '11px', color: 'var(--text-dim)' }}>Passing Threshold: 60.0%</span>
          </div>
          {loading ? (
            <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>Loading ratio...</div>
          ) : (
            <PassFailDonutChart
              totalPassed={stats?.totalPassed || 0}
              totalFailed={stats?.totalFailed || 0}
              passPercentage={stats?.passPercentage || 0}
            />
          )}
        </div>
      </div>

      {/* Leaderboard & Quick Action Banner */}
      <div style={{ display: 'grid', gridTemplateColumns: '1.5fr 1fr', gap: '20px' }}>
        {/* Top Performers Table */}
        <div className="card">
          <div className="card-header">
            <div className="card-title">
              <span>⭐</span> Top Performing Students
            </div>
            <button
              type="button"
              className="btn btn-outline btn-sm"
              onClick={() => setActivePage('students')}
            >
              View Full Directory →
            </button>
          </div>

          {stats?.topPerformers && stats.topPerformers.length > 0 ? (
            <div className="table-container">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Rank</th>
                    <th>Student ID</th>
                    <th>Name</th>
                    <th>Class</th>
                    <th>Score</th>
                    <th>Grade</th>
                  </tr>
                </thead>
                <tbody>
                  {stats.topPerformers.map((s) => (
                    <tr key={s.id}>
                      <td>
                        <span className={`rank-badge rank-${s.rank <= 3 ? s.rank : 'other'}`}>
                          {s.rank}
                        </span>
                      </td>
                      <td style={{ fontFamily: 'var(--font-mono)', fontWeight: 600 }}>{s.studentId}</td>
                      <td style={{ fontWeight: 600 }}>{s.name}</td>
                      <td>{s.className}</td>
                      <td>{s.marks != null ? s.marks.toFixed(2) : '-'} ({s.percentage.toFixed(1)}%)</td>
                      <td>
                        <span className={`badge badge-${s.grade.toLowerCase()}`}>
                          {s.grade}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div style={{ padding: '36px', textAlign: 'center', color: 'var(--text-muted)' }}>
              No student records in database yet. Add students or import a class list.
            </div>
          )}
        </div>

        {/* Quick Actions Card */}
        <div className="card" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
          <div>
            <div className="card-header">
              <div className="card-title">
                <span>⚡</span> Quick Management Actions
              </div>
            </div>
            <p style={{ fontSize: '13px', color: 'var(--text-muted)', marginBottom: '18px' }}>
              Frequently used tools for instructors to manage student scores, generate official records, and conduct imports:
            </p>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              <button
                type="button"
                className="btn btn-primary"
                onClick={onAddStudent}
                style={{ width: '100%', justifyContent: 'flex-start' }}
              >
                <span>➕</span> Add New Student Record
              </button>

              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setActivePage('import')}
                style={{ width: '100%', justifyContent: 'flex-start' }}
              >
                <span>📁</span> Upload Class List (CSV / Excel)
              </button>

              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setActivePage('reports')}
                style={{ width: '100%', justifyContent: 'flex-start' }}
              >
                <span>📑</span> Generate Official Transcripts
              </button>

              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => api.exportCsv({ className: selectedClass })}
                style={{ width: '100%', justifyContent: 'flex-start' }}
              >
                <span>📥</span> Export Current Class as CSV
              </button>
            </div>
          </div>

          <div
            style={{
              marginTop: '18px',
              padding: '12px',
              borderRadius: '8px',
              background: 'var(--bg-subtle)',
              border: '1px solid var(--border)',
              fontSize: '12px',
              color: 'var(--text-dim)',
            }}
          >
            🔒 <strong>Data Persistence:</strong> All student records and grades are automatically committed to the persistent H2 database file.
          </div>
        </div>
      </div>
    </div>
  );
}
