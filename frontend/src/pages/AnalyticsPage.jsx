import React, { useState, useEffect } from 'react';
import StatCard from '../components/StatCard';
import { GradeDistributionChart, PassFailDonutChart } from '../components/Charts';
import { api } from '../services/api';

export default function AnalyticsPage({ addToast }) {
  const [classes, setClasses] = useState([]);
  const [selectedClass, setSelectedClass] = useState('ALL');
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(false);

  const fetchAnalytics = async () => {
    try {
      setLoading(true);
      const [statsData, classList] = await Promise.all([
        api.getStatistics(selectedClass),
        api.getClassNames(),
      ]);
      setStats(statsData);
      setClasses(classList || []);
    } catch (err) {
      addToast(err.message || 'Error loading analytics', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAnalytics();
  }, [selectedClass]);

  return (
    <div className="page-content">
      {/* Title & Filter */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '22px' }}>
        <div>
          <h2 style={{ fontSize: '20px', fontWeight: 800 }}>Class Performance Analytics</h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '13px' }}>
            In-depth statistical breakdown, grade distribution metrics, and curriculum subject averages
          </p>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <label style={{ fontSize: '13px', fontWeight: 600 }}>Filter Class:</label>
          <select
            className="form-select"
            style={{ width: '160px' }}
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
      </div>

      {/* Metrics Grid */}
      <div className="stat-grid">
        <StatCard
          label="Total Sample Size"
          value={stats ? stats.totalStudents : 0}
          subText={selectedClass === 'ALL' ? 'Total enrolled in school' : `Enrolled in ${selectedClass}`}
          icon="👥"
          color="blue"
        />
        <StatCard
          label="Mean Percentage"
          value={stats ? `${stats.averagePercentage.toFixed(2)}%` : '0.00%'}
          subText="Class composite average"
          icon="📈"
          color="cyan"
        />
        <StatCard
          label="Highest Score"
          value={stats ? stats.highestMarks.toFixed(2) : '0.00'}
          subText={stats?.highestScoringStudents?.[0] ? `Leader: ${stats.highestScoringStudents[0]}` : 'N/A'}
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
          label="Pass Percentage"
          value={stats ? `${stats.passPercentage.toFixed(2)}%` : '0.00%'}
          subText={stats ? `${stats.totalPassed} Passed / ${stats.totalFailed} Failed` : '0 Passed'}
          icon="🎓"
          color={stats?.passPercentage >= 60 ? 'green' : 'red'}
        />
      </div>

      {/* Charts Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: '1.4fr 1fr', gap: '20px', marginBottom: '24px' }}>
        <div className="card">
          <div className="card-header">
            <div className="card-title">
              <span>📊</span> Grade Distribution Spectrum
            </div>
            <span style={{ fontSize: '12px', color: 'var(--text-dim)' }}>
              Total: {stats?.totalStudents || 0} students
            </span>
          </div>
          <GradeDistributionChart
            distribution={stats?.gradeDistribution || {}}
            totalStudents={stats?.totalStudents || 0}
          />
        </div>

        <div className="card">
          <div className="card-header">
            <div className="card-title">
              <span>🍩</span> Academic Clearance Rate
            </div>
          </div>
          <PassFailDonutChart
            totalPassed={stats?.totalPassed || 0}
            totalFailed={stats?.totalFailed || 0}
            passPercentage={stats?.passPercentage || 0}
          />
        </div>
      </div>

      {/* Grade Table & Subject Breakdown */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
        {/* Grade Distribution Detail Table */}
        <div className="card">
          <div className="card-header">
            <div className="card-title">
              <span>📑</span> Grade Distribution Table
            </div>
          </div>
          <div className="table-container" style={{ border: 'none' }}>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Grade</th>
                  <th>Standard Score Range</th>
                  <th style={{ textAlign: 'right' }}>Students</th>
                  <th style={{ textAlign: 'right' }}>Percentage</th>
                </tr>
              </thead>
              <tbody>
                {[
                  { grade: 'A', range: '90.0% to 100.0%', color: '#3b82f6' },
                  { grade: 'B', range: '80.0% to 89.99%', color: '#10b981' },
                  { grade: 'C', range: '70.0% to 79.99%', color: '#f59e0b' },
                  { grade: 'D', range: '60.0% to 69.99%', color: '#f97316' },
                  { grade: 'F', range: 'Below 60.0%', color: '#ef4444' },
                ].map((g) => {
                  const count = stats?.gradeDistribution?.[g.grade] || 0;
                  const pct = stats?.gradePercentages?.[g.grade] || 0;
                  return (
                    <tr key={g.grade}>
                      <td>
                        <span className={`badge badge-${g.grade.toLowerCase()}`}>
                          Grade {g.grade}
                        </span>
                      </td>
                      <td style={{ color: 'var(--text-muted)' }}>{g.range}</td>
                      <td style={{ textAlign: 'right', fontWeight: 700, fontFamily: 'var(--font-mono)' }}>
                        {count}
                      </td>
                      <td style={{ textAlign: 'right', fontFamily: 'var(--font-mono)' }}>
                        {pct.toFixed(1)}%
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>

        {/* Subject Averages Table */}
        <div className="card">
          <div className="card-header">
            <div className="card-title">
              <span>📚</span> Subject Performance Benchmarks
            </div>
          </div>

          {stats?.subjectAverages && Object.keys(stats.subjectAverages).length > 0 ? (
            <div className="table-container" style={{ border: 'none' }}>
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Subject Name</th>
                    <th style={{ textAlign: 'right' }}>Average Score</th>
                    <th style={{ textAlign: 'center' }}>Benchmark</th>
                  </tr>
                </thead>
                <tbody>
                  {Object.entries(stats.subjectAverages).map(([subName, avg]) => (
                    <tr key={subName}>
                      <td style={{ fontWeight: 600 }}>{subName}</td>
                      <td style={{ textAlign: 'right', fontFamily: 'var(--font-mono)', fontWeight: 700 }}>
                        {avg.toFixed(2)}
                      </td>
                      <td style={{ textAlign: 'center' }}>
                        <span className={`badge ${avg >= 60 ? 'badge-pass' : 'badge-fail'}`}>
                          {avg >= 75 ? 'Excellent' : avg >= 60 ? 'Passing' : 'Needs Attention'}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div style={{ padding: '36px', textAlign: 'center', color: 'var(--text-muted)' }}>
              No subject-wise records entered yet. Students currently graded in overall marks mode.
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
