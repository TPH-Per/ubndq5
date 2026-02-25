import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { SimulationProvider } from './context/SimulationContext';
import { CitizenLayout } from './layouts/CitizenLayout';
import { Landing } from './pages/Landing';
import { CitizenHome } from './pages/citizen/Home';
import { BookingFlow } from './pages/citizen/BookingFlow';
import { QueueTracking } from './pages/citizen/QueueTracking';
import { MyAppointments } from './pages/citizen/MyAppointments';
import { MyDocuments } from './pages/citizen/MyDocuments';
import { Profile } from './pages/citizen/Profile';
import { Feedback } from './pages/citizen/Feedback';

function App() {
  return (
    <SimulationProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Navigate to="/citizen" replace />} />

          {/* Citizen Routes - Zalo Mini App */}
          <Route path="/citizen" element={<CitizenLayout />}>
            <Route index element={<CitizenHome />} />
            <Route path="booking/*" element={<BookingFlow />} />
            <Route path="queue/:id" element={<QueueTracking />} />
            <Route path="appointments" element={<MyAppointments />} />
            <Route path="documents" element={<MyDocuments />} />
            <Route path="profile" element={<Profile />} />
            <Route path="feedback" element={<Feedback />} />
          </Route>

          {/* Redirect any other routes to citizen home */}
          <Route path="*" element={<Navigate to="/citizen" replace />} />
        </Routes>
      </BrowserRouter>
    </SimulationProvider>
  );
}

export default App;
