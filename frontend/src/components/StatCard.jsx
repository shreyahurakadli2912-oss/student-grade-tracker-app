import React from 'react';

export default function StatCard({ label, value, subText, icon, color = 'blue' }) {
  const colorMap = {
    blue: { bg: 'rgba(37, 99, 235, 0.15)', text: '#60a5fa' },
    green: { bg: 'rgba(16, 185, 129, 0.15)', text: '#34d399' },
    amber: { bg: 'rgba(245, 158, 11, 0.15)', text: '#fbbf24' },
    red: { bg: 'rgba(239, 68, 68, 0.15)', text: '#f87171' },
    cyan: { bg: 'rgba(6, 182, 212, 0.15)', text: '#22d3ee' },
    indigo: { bg: 'rgba(99, 102, 241, 0.15)', text: '#818cf8' },
  };

  const scheme = colorMap[color] || colorMap.blue;

  return (
    <div className="stat-card">
      <div className="stat-icon-wrapper" style={{ background: scheme.bg, color: scheme.text }}>
        {icon}
      </div>
      <div className="stat-info">
        <div className="stat-label">{label}</div>
        <div className="stat-value">{value}</div>
        {subText && <div className="stat-sub">{subText}</div>}
      </div>
    </div>
  );
}
