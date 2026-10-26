// ===================================================================
// Student Grade Management System - Central API Client
// Connects to Spring Boot backend at http://localhost:8080/api
// ===================================================================

// In production single-origin deployment, use relative '/api'
// In local dev, Vite proxies '/api' to backend on port 8080 or uses VITE_API_BASE_URL
const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

async function request(endpoint, options = {}) {
  const url = `${BASE_URL}${endpoint}`;
  const config = {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  };

  if (options.body instanceof FormData) {
    // Let browser set the multipart boundary automatically
    delete config.headers['Content-Type'];
  }

  try {
    const response = await fetch(url, config);

    // Handle binary / blob downloads (PDF, Excel, CSV)
    const contentType = response.headers.get('content-type') || '';
    if (
      contentType.includes('application/pdf') ||
      contentType.includes('application/vnd') ||
      contentType.includes('text/csv') ||
      contentType.includes('application/octet-stream')
    ) {
      if (!response.ok) {
        throw new Error(`Export download failed with status ${response.status}`);
      }
      return await response.blob();
    }

    const data = await response.json().catch(() => null);

    if (!response.ok) {
      const errorMessage = data?.message || data?.error || `Request failed with status ${response.status}`;
      throw new Error(errorMessage);
    }

    return data;
  } catch (error) {
    console.error(`API Error on [${options.method || 'GET'}] ${endpoint}:`, error);
    throw error;
  }
}

export const api = {
  // Students
  getStudents: (params = {}) => {
    const query = new URLSearchParams();
    if (params.query) query.append('query', params.query);
    if (params.className && params.className !== 'ALL') query.append('className', params.className);
    if (params.grade && params.grade !== 'ALL') query.append('grade', params.grade);
    if (params.status && params.status !== 'ALL') query.append('status', params.status);
    if (params.sortBy) query.append('sortBy', params.sortBy);
    if (params.sortDir) query.append('sortDir', params.sortDir);

    const qs = query.toString();
    return request(`/students${qs ? '?' + qs : ''}`);
  },

  getStudentById: (id) => request(`/students/${id}`),

  createStudent: (studentData) =>
    request('/students', {
      method: 'POST',
      body: JSON.stringify(studentData),
    }),

  updateStudent: (id, studentData) =>
    request(`/students/${id}`, {
      method: 'PUT',
      body: JSON.stringify(studentData),
    }),

  deleteStudent: (id) =>
    request(`/students/${id}`, {
      method: 'DELETE',
    }),

  getClassNames: () => request('/students/classes'),

  loadDemoData: () =>
    request('/students/demo-data', {
      method: 'POST',
    }),

  clearAllStudents: () =>
    request('/students/all', {
      method: 'DELETE',
    }),

  // Subjects
  getSubjects: () => request('/subjects'),

  createSubject: (subjectData) =>
    request('/subjects', {
      method: 'POST',
      body: JSON.stringify(subjectData),
    }),

  updateSubject: (id, subjectData) =>
    request(`/subjects/${id}`, {
      method: 'PUT',
      body: JSON.stringify(subjectData),
    }),

  deleteSubject: (id) =>
    request(`/subjects/${id}`, {
      method: 'DELETE',
    }),

  // Bulk Import
  previewImport: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return request('/import/preview', {
      method: 'POST',
      body: formData,
    });
  },

  confirmImport: (rows, skipInvalid = true) =>
    request('/import/confirm', {
      method: 'POST',
      body: JSON.stringify({ rows, skipInvalid }),
    }),

  downloadCsvTemplate: async () => {
    const blob = await request('/import/template/csv');
    triggerDownload(blob, 'students_import_template.csv');
  },

  downloadExcelTemplate: async () => {
    const blob = await request('/import/template/excel');
    triggerDownload(blob, 'students_import_template.xlsx');
  },

  downloadRejectedRowsReport: async (rejectedRows) => {
    const blob = await request('/import/rejected-report', {
      method: 'POST',
      body: JSON.stringify(rejectedRows),
    });
    triggerDownload(blob, 'import_rejected_errors.csv');
  },

  // Analytics & Statistics
  getStatistics: (className = 'ALL') => {
    const p = className && className !== 'ALL' ? `?className=${encodeURIComponent(className)}` : '';
    return request(`/analytics/statistics${p}`);
  },

  // Reports
  getClassReport: (params = {}) => {
    const query = new URLSearchParams();
    if (params.query) query.append('query', params.query);
    if (params.className && params.className !== 'ALL') query.append('className', params.className);
    if (params.grade && params.grade !== 'ALL') query.append('grade', params.grade);
    if (params.status && params.status !== 'ALL') query.append('status', params.status);

    const qs = query.toString();
    return request(`/reports/class${qs ? '?' + qs : ''}`);
  },

  getStudentReport: (id) => request(`/reports/student/${id}`),

  // Exports
  exportCsv: async (params = {}) => {
    const query = new URLSearchParams(params).toString();
    const blob = await request(`/export/csv${query ? '?' + query : ''}`);
    triggerDownload(blob, `grade_report_${params.className || 'all'}.csv`);
  },

  exportExcel: async (params = {}) => {
    const query = new URLSearchParams(params).toString();
    const blob = await request(`/export/excel${query ? '?' + query : ''}`);
    triggerDownload(blob, `grade_report_${params.className || 'all'}.xlsx`);
  },

  exportClassPdf: async (params = {}) => {
    const query = new URLSearchParams(params).toString();
    const blob = await request(`/export/pdf/class${query ? '?' + query : ''}`);
    triggerDownload(blob, `class_report_${params.className || 'all'}.pdf`);
  },

  exportStudentPdf: async (id, studentId = 'student') => {
    const blob = await request(`/export/pdf/student/${id}`);
    triggerDownload(blob, `report_card_${studentId}.pdf`);
  },

  // Grade Scale
  getGradeScale: () => request('/grade-scale'),

  resetGradeScale: () =>
    request('/grade-scale/reset', {
      method: 'POST',
    }),

  // System Health
  getHealth: () => request('/health'),
};

function triggerDownload(blob, filename) {
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  window.URL.revokeObjectURL(url);
}
