import React from 'react';

const NAV_ITEMS = [
  { id: 'dashboard', label: 'Dashboard', icon: '📊' },
  { id: 'students', label: 'Students', icon: '🎓' },
  { id: 'import', label: 'Bulk Import', icon: '📁' },
  { id: 'subjects', label: 'Subjects & Marks', icon: '📚' },
  { id: 'reports', label: 'Grade Reports', icon: '📑' },
  { id: 'analytics', label: 'Class Analytics', icon: '📈' },
  { id: 'gradescale', label: 'Grade Scale', icon: '⚖️' },
  { id: 'settings', label: 'Settings & Demo', icon: '⚙️' },
];

export default function Sidebar({ activePage, setActivePage, isOnline = true }) {
  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <div className="logo-badge">
          🎓
        </div>
        <div className="logo-text">
          <h1>GradeTracker</h1>
          <span>Java Full-Stack</span>
        </div>
      </div>

      <nav className="sidebar-nav">
        {NAV_ITEMS.map((item) => (
          <button
            key={item.id}
            type="button"
            className={`nav-link ${activePage === item.id ? 'active' : ''}`}
            onClick={() => setActivePage(item.id)}
          >
            <span className="nav-icon">{item.icon}</span>
            <span>{item.label}</span>
          </button>
        ))}
      </nav>

      <div className="sidebar-footer">
        <div className="system-status">
          <div className="status-dot" style={{ background: isOnline ? '#10b981' : '#ef4444' }}></div>
          <span>{isOnline ? 'Backend Connected' : 'Connecting to Server...'}</span>
        </div>
      </div>
    </aside>
  );
}
