import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { SimulationProvider } from './context/SimulationContext';
import { CitizenLayout } from './layouts/CitizenLayout';
import { StaffLayout } from './layouts/StaffLayout';
import { Landing } from './pages/Landing';
import { CitizenHome } from './pages/citizen/Home';
import { BookingFlow } from './pages/citizen/BookingFlow';
import { QueueTracking } from './pages/citizen/QueueTracking';
import { StaffDashboard } from './pages/staff/Dashboard';
import { QueueManagement } from './pages/staff/QueueManagement';
import { FileProcessing } from './pages/staff/FileProcessing';
import { MyAppointments } from './pages/citizen/MyAppointments';
import { MyDocuments } from './pages/citizen/MyDocuments';
import { Profile } from './pages/citizen/Profile';
import { Feedback } from './pages/citizen/Feedback';
import { AdminLayout } from './layouts/AdminLayout';
import { AdminDashboard } from './pages/admin/Dashboard';
import { AccountManagement } from './pages/admin/AccountManagement';
import { CounterManagement } from './pages/admin/CounterManagement';
import { AdminReports } from './pages/admin/Reports';
import { AdminLogin } from './pages/admin/Login';

function App() {
  return (
    <SimulationProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Landing />} />

          {/* Citizen Routes */}
          <Route path="/citizen" element={<CitizenLayout />}>
            <Route index element={<CitizenHome />} />
            <Route path="booking/*" element={<BookingFlow />} />
            <Route path="queue/:id" element={<QueueTracking />} />
            <Route path="appointments" element={<MyAppointments />} />
            <Route path="documents" element={<MyDocuments />} />
            <Route path="profile" element={<Profile />} />
            <Route path="feedback" element={<Feedback />} />
          </Route>

          {/* Staff Routes */}
          <Route path="/staff" element={<StaffLayout />}>
            <Route index element={<Navigate to="/staff/dashboard" replace />} />
            <Route path="dashboard" element={<StaffDashboard />} />
            <Route path="queue" element={<QueueManagement />} />
            <Route path="documents" element={<FileProcessing />} />
            <Route path="*" element={<div className="p-6">Page Under Construction</div>} />
          </Route>

          {/* Admin Routes */}
          <Route path="/admin/login" element={<AdminLogin />} />
          <Route path="/admin" element={<AdminLayout />}>
            <Route index element={<Navigate to="/admin/dashboard" replace />} />
            <Route path="dashboard" element={<AdminDashboard />} />
            <Route path="accounts" element={<AccountManagement />} />
            <Route path="counters" element={<CounterManagement />} />
            <Route path="reports" element={<AdminReports />} />
            <Route path="*" element={<div className="p-6">Admin Page Under Construction</div>} />
          </Route>
        </Routes>
      </BrowserRouter>
    </SimulationProvider>
  );
}

export default App;
