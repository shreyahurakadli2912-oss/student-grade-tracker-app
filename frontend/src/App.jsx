import React, { useState, useEffect } from 'react';
import Sidebar from './components/Sidebar';
import Header from './components/Header';
import Toast from './components/Toast';
import StudentFormModal from './components/StudentFormModal';
import Dashboard from './pages/Dashboard';
import StudentsPage from './pages/StudentsPage';
import BulkImportPage from './pages/BulkImportPage';
import SubjectsPage from './pages/SubjectsPage';
import ReportsPage from './pages/ReportsPage';
import AnalyticsPage from './pages/AnalyticsPage';
import GradeScalePage from './pages/GradeScalePage';
import SettingsPage from './pages/SettingsPage';
import { api } from './services/api';

const VALID_PAGES = ['dashboard', 'students', 'import', 'subjects', 'reports', 'analytics', 'gradescale', 'settings'];

function getPageFromPath() {
  const path = window.location.pathname.replace(/^\/+|\/+$/g, '').toLowerCase();
  return VALID_PAGES.includes(path) ? path : 'dashboard';
}

export default function App() {
  const [activePage, setActivePageState] = useState(getPageFromPath);
  const [toasts, setToasts] = useState([]);
  const [isOnline, setIsOnline] = useState(true);

  // Sync activePage state with browser URL and history
  const setActivePage = (page) => {
    if (VALID_PAGES.includes(page)) {
      setActivePageState(page);
      const targetPath = page === 'dashboard' ? '/' : `/${page}`;
      if (window.location.pathname !== targetPath) {
        window.history.pushState({ page }, '', targetPath);
      }
    }
  };

  useEffect(() => {
    const handlePopState = () => {
      setActivePageState(getPageFromPath());
    };
    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, []);

  // Quick Add Student Modal State
  const [isQuickAddOpen, setIsQuickAddOpen] = useState(false);
  const [subjects, setSubjects] = useState([]);
  const [quickAddLoading, setQuickAddLoading] = useState(false);

  const addToast = (message, type = 'info') => {
    const id = Date.now() + Math.random().toString(36).substring(2, 6);
    setToasts((prev) => [...prev, { id, message, type }]);
    setTimeout(() => {
      dismissToast(id);
    }, 4500);
  };

  const dismissToast = (id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  };

  const checkServerStatus = async () => {
    try {
      await api.getHealth();
      setIsOnline(true);
    } catch {
      setIsOnline(false);
    }
  };

  const fetchSubjects = async () => {
    try {
      const data = await api.getSubjects();
      setSubjects(data || []);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    checkServerStatus();
    fetchSubjects();
    const interval = setInterval(checkServerStatus, 15000);
    return () => clearInterval(interval);
  }, []);

  const handleQuickSaveStudent = async (payload) => {
    try {
      setQuickAddLoading(true);
      await api.createStudent(payload);
      addToast(`Student '${payload.name}' added successfully!`, 'success');
      setIsQuickAddOpen(false);
      setActivePage('students');
    } catch (err) {
      addToast(err.message || 'Failed to add student', 'error');
    } finally {
      setQuickAddLoading(false);
    }
  };

  const handleLoadDemoData = async () => {
    try {
      await api.loadDemoData();
      addToast('Phase 17 acceptance dataset loaded: 5 students (S101-S105) successfully initialized!', 'success');
      setActivePage('dashboard');
    } catch (err) {
      addToast(err.message || 'Failed to load demo data', 'error');
    }
  };

  return (
    <div className="app-layout">
      {/* Fixed Sidebar */}
      <Sidebar activePage={activePage} setActivePage={setActivePage} isOnline={isOnline} />

      {/* Main Container */}
      <div className="main-wrapper">
        <Header
          activePage={activePage}
          onQuickAdd={() => setIsQuickAddOpen(true)}
          onLoadDemo={handleLoadDemoData}
          isOnline={isOnline}
        />

        <main>
          {activePage === 'dashboard' && (
            <Dashboard
              setActivePage={setActivePage}
              onAddStudent={() => setIsQuickAddOpen(true)}
              addToast={addToast}
            />
          )}

          {activePage === 'students' && (
            <StudentsPage
              addToast={addToast}
              onOpenAddModal={() => setIsQuickAddOpen(true)}
            />
          )}

          {activePage === 'import' && (
            <BulkImportPage addToast={addToast} setActivePage={setActivePage} />
          )}

          {activePage === 'subjects' && <SubjectsPage addToast={addToast} />}

          {activePage === 'reports' && <ReportsPage addToast={addToast} />}

          {activePage === 'analytics' && <AnalyticsPage addToast={addToast} />}

          {activePage === 'gradescale' && <GradeScalePage addToast={addToast} />}

          {activePage === 'settings' && (
            <SettingsPage addToast={addToast} setActivePage={setActivePage} />
          )}
        </main>
      </div>

      {/* Global Quick Add Modal */}
      {isQuickAddOpen && (
        <StudentFormModal
          isOpen={isQuickAddOpen}
          onClose={() => setIsQuickAddOpen(false)}
          onSave={handleQuickSaveStudent}
          student={null}
          subjects={subjects}
          loading={quickAddLoading}
        />
      )}

      {/* Global Toast Alerts */}
      <Toast toasts={toasts} onDismiss={dismissToast} />
    </div>
  );
}
