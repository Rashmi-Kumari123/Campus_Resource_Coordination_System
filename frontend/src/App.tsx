import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import { Layout } from './components/Layout';
import { Landing } from './pages/Landing';
import { Login } from './pages/Login';
import { Signup } from './pages/Signup';
import { Dashboard } from './pages/Dashboard';
import { Resources } from './pages/Resources';
import { ResourceDetail } from './pages/ResourceDetail';
import { ResourceForm } from './pages/ResourceForm';
import { Bookings } from './pages/Bookings';
import { CreateBooking } from './pages/CreateBooking';
import { PendingBookings } from './pages/PendingBookings';
import { ManageResources } from './pages/ManageResources';
import { Users } from './pages/Users';
function HomeRedirect() {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? <Navigate to="/dashboard" replace /> : <Landing />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<HomeRedirect />} />
      <Route path="/login" element={<Login />} />
      <Route path="/signup" element={<Signup />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route path="dashboard" element={<Dashboard />} />
        <Route path="resources" element={<Resources />} />
        <Route
          path="resources/manage"
          element={
            <ProtectedRoute allowedRoles={['ADMIN', 'RESOURCE_MANAGER', 'FACILITY_MANAGER']}>
              <ManageResources />
            </ProtectedRoute>
          }
        />
        <Route path="resources/:id" element={<ResourceDetail />} />
        <Route
          path="resources/manage/new"
          element={
            <ProtectedRoute allowedRoles={['ADMIN', 'RESOURCE_MANAGER', 'FACILITY_MANAGER']}>
              <ResourceForm />
            </ProtectedRoute>
          }
        />
        <Route
          path="resources/manage/:id"
          element={
            <ProtectedRoute allowedRoles={['ADMIN', 'RESOURCE_MANAGER', 'FACILITY_MANAGER']}>
              <ResourceForm />
            </ProtectedRoute>
          }
        />
        <Route path="bookings" element={<Bookings />} />
        <Route path="bookings/new" element={<CreateBooking />} />
        <Route
          path="bookings/pending"
          element={
            <ProtectedRoute allowedRoles={['ADMIN', 'FACILITY_MANAGER']}>
              <PendingBookings />
            </ProtectedRoute>
          }
        />
        <Route
          path="users"
          element={
            <ProtectedRoute allowedRoles={['ADMIN']}>
              <Users />
            </ProtectedRoute>
          }
        />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
