import React, { useState, useEffect } from 'react';
import { api } from '../services/api';

export default function GradeScalePage({ addToast }) {
  const [rules, setRules] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetchRules = async () => {
    try {
      setLoading(true);
      const data = await api.getGradeScale();
      setRules(data || []);
    } catch (err) {
      addToast(err.message || 'Error fetching grade scale', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRules();
  }, []);

  const handleReset = async () => {
    try {
      setLoading(true);
      const data = await api.resetGradeScale();
      setRules(data || []);
      addToast('Grade scale reset to standard defaults!', 'success');
    } catch (err) {
      addToast(err.message || 'Failed to reset grade scale', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-content">
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '22px' }}>
        <div>
          <h2 style={{ fontSize: '20px', fontWeight: 800 }}>Grade Scale & Boundaries</h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '13px' }}>
            Official percentage intervals and evaluation thresholds applied across all calculations
          </p>
        </div>

        <button
          type="button"
          className="btn btn-secondary btn-sm"
          onClick={handleReset}
          disabled={loading}
        >
          <span>↺ Reset to Standard Scale</span>
        </button>
      </div>

      <div className="card" style={{ marginBottom: '24px', padding: 0, overflow: 'hidden' }}>
        <div className="table-container" style={{ border: 'none' }}>
          <table className="data-table">
            <thead>
              <tr>
                <th style={{ width: '80px', textAlign: 'center' }}>Grade</th>
                <th style={{ textAlign: 'right' }}>Min Percentage</th>
                <th style={{ textAlign: 'right' }}>Max Percentage</th>
                <th>Academic Distinction</th>
                <th style={{ textAlign: 'center' }}>Result</th>
              </tr>
            </thead>
            <tbody>
              {rules.map((rule) => {
                const isPass = rule.grade !== 'F';
                return (
                  <tr key={rule.id || rule.grade}>
                    <td style={{ textAlign: 'center' }}>
                      <span className={`badge badge-${rule.grade.toLowerCase()}`} style={{ fontSize: '14px' }}>
                        {rule.grade}
                      </span>
                    </td>
                    <td style={{ textAlign: 'right', fontFamily: 'var(--font-mono)', fontWeight: 600 }}>
                      {rule.minPercentage.toFixed(1)}%
                    </td>
                    <td style={{ textAlign: 'right', fontFamily: 'var(--font-mono)', fontWeight: 600 }}>
                      {rule.maxPercentage.toFixed(2)}%
                    </td>
                    <td>{rule.description}</td>
                    <td style={{ textAlign: 'center' }}>
                      <span className={`badge ${isPass ? 'badge-pass' : 'badge-fail'}`}>
                        {isPass ? 'PASS' : 'FAIL'}
                      </span>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      {/* Policy details card */}
      <div className="card">
        <h4 style={{ fontSize: '15px', fontWeight: 700, marginBottom: '12px', color: '#60a5fa' }}>
          ⚖️ Evaluation & Pass/Fail Rules
        </h4>
        <ul style={{ color: 'var(--text-muted)', fontSize: '13.5px', lineHeight: '1.8', paddingLeft: '20px' }}>
          <li>
            <strong>Overall Marks Mode:</strong> Percentage equals marks obtained out of 100.0. A score of 60.0% or above is marked as <strong>PASS</strong>. Scores below 60.0% receive Grade F and <strong>FAIL</strong> status.
          </li>
          <li>
            <strong>Multiple Subjects Mode:</strong> Overall percentage is calculated as <code>(Total Marks Obtained / Total Maximum Marks) * 100.0</code> with consistent half-up rounding.
          </li>
          <li>
            <strong>Subject Requirement Policy:</strong> In subject-wise mode, a student must achieve at least the passing mark on every compulsory subject. Failing any compulsory subject results in overall status <strong>FAIL</strong> even if the total percentage is high.
          </li>
          <li>
            <strong>Student Rankings:</strong> Dense ranking is enforced throughout the system (students with identical marks receive equal ranks, and the next distinct mark increments the rank by exactly 1 without skipping).
          </li>
        </ul>
      </div>
    </div>
  );
}
