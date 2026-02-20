import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import { RideProvider } from './context/RideContext'
import Login from './pages/Login'
import RiderDashboard from './pages/RiderDashboard'
import DriverDashboard from './pages/DriverDashboard'
import ProtectedRoute from './components/shared/ProtectedRoute'

function App() {
  return (
    <Router>
      <AuthProvider>
        <RideProvider>
          <Routes>
            <Route path="/" element={<Login />} />
            <Route 
              path="/rider" 
              element={
                <ProtectedRoute role="rider">
                  <RiderDashboard />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/driver" 
              element={
                <ProtectedRoute role="driver">
                  <DriverDashboard />
                </ProtectedRoute>
              } 
            />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </RideProvider>
      </AuthProvider>
    </Router>
  )
}

export default App
