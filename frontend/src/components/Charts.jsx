import React from 'react';

export function GradeDistributionChart({ distribution = {}, totalStudents = 0 }) {
  const grades = ['A', 'B', 'C', 'D', 'F'];
  const colors = {
    A: '#3b82f6',
    B: '#10b981',
    C: '#f59e0b',
    D: '#f97316',
    F: '#ef4444',
  };

  const counts = grades.map((g) => distribution[g] || 0);
  const maxCount = Math.max(...counts, 1);

  return (
    <div style={{ width: '100%', padding: '10px 0' }}>
      <div style={{ display: 'flex', alignItems: 'flex-end', height: '170px', gap: '16px', paddingBottom: '24px', borderBottom: '1px solid var(--border)' }}>
        {grades.map((grade) => {
          const count = distribution[grade] || 0;
          const heightPct = (count / maxCount) * 100;
          const pctOfTotal = totalStudents > 0 ? ((count / totalStudents) * 100).toFixed(1) : 0;

          return (
            <div
              key={grade}
              style={{
                flex: 1,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                height: '100%',
                justifyContent: 'flex-end',
              }}
            >
              <div style={{ fontSize: '11px', color: '#fff', fontWeight: 700, marginBottom: '6px' }}>
                {count}
              </div>
              <div
                style={{
                  width: '100%',
                  maxWidth: '48px',
                  height: `${Math.max(heightPct, 4)}%`,
                  background: colors[grade],
                  borderRadius: '6px 6px 0 0',
                  boxShadow: `0 2px 8px ${colors[grade]}40`,
                  transition: 'height 0.4s ease-out',
                }}
                title={`Grade ${grade}: ${count} student(s) (${pctOfTotal}%)`}
              />
              <div style={{ marginTop: '8px', textAlign: 'center' }}>
                <span
                  style={{
                    display: 'inline-block',
                    padding: '2px 8px',
                    borderRadius: '4px',
                    background: `${colors[grade]}20`,
                    color: colors[grade],
                    fontWeight: 700,
                    fontSize: '12px',
                  }}
                >
                  Grade {grade}
                </span>
                <div style={{ fontSize: '10px', color: 'var(--text-dim)', marginTop: '2px' }}>
                  {pctOfTotal}%
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export function PassFailDonutChart({ totalPassed = 0, totalFailed = 0, passPercentage = 0 }) {
  const total = totalPassed + totalFailed;
  const radius = 60;
  const circumference = 2 * Math.PI * radius;
  const strokeDashoffset = total > 0 ? circumference - (passPercentage / 100) * circumference : circumference;

  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '28px', padding: '16px 0' }}>
      <div style={{ position: 'relative', width: '150px', height: '150px' }}>
        <svg width="150" height="150" viewBox="0 0 150 150">
          <circle
            cx="75"
            cy="75"
            r={radius}
            fill="transparent"
            stroke="#1e293b"
            strokeWidth="16"
          />
          {total > 0 && (
            <>
              {/* Fail base circle */}
              <circle
                cx="75"
                cy="75"
                r={radius}
                fill="transparent"
                stroke="#ef4444"
                strokeWidth="16"
              />
              {/* Pass arc */}
              <circle
                cx="75"
                cy="75"
                r={radius}
                fill="transparent"
                stroke="#10b981"
                strokeWidth="16"
                strokeDasharray={circumference}
                strokeDashoffset={strokeDashoffset}
                strokeLinecap="round"
                transform="rotate(-90 75 75)"
                style={{ transition: 'stroke-dashoffset 0.6s ease' }}
              />
            </>
          )}
        </svg>

        <div
          style={{
            position: 'absolute',
            top: 0,
            left: 0,
            width: '100%',
            height: '100%',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <span style={{ fontSize: '20px', fontWeight: 800, color: '#fff', fontFamily: 'var(--font-mono)' }}>
            {passPercentage.toFixed(1)}%
          </span>
          <span style={{ fontSize: '10px', color: 'var(--text-muted)', textTransform: 'uppercase' }}>
            Pass Rate
          </span>
        </div>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div style={{ width: '12px', height: '12px', borderRadius: '3px', background: '#10b981' }} />
          <div>
            <div style={{ fontSize: '13px', fontWeight: 600, color: '#fff' }}>Passed: {totalPassed}</div>
            <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Score ≥ 60.0%</div>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div style={{ width: '12px', height: '12px', borderRadius: '3px', background: '#ef4444' }} />
          <div>
            <div style={{ fontSize: '13px', fontWeight: 600, color: '#fff' }}>Failed: {totalFailed}</div>
            <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Score &lt; 60.0%</div>
          </div>
        </div>
      </div>
    </div>
  );
}
