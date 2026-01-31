import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { bookingsApi } from '../api/bookings';
import { resourcesApi } from '../api/resources';
import type { Booking } from '../types';
import { getApiErrorMessage } from '../api/auth';
import './Dashboard.css';

export function Dashboard() {
  const { user } = useAuth();
  const [recentBookings, setRecentBookings] = useState<Booking[]>([]);
  const [availableCount, setAvailableCount] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!user?.userId) return;
    Promise.all([
      bookingsApi.getUserBookings(user.userId, { page: 0, size: 5 }),
      resourcesApi.getAvailable({ page: 0, size: 1 }),
    ])
      .then(([bookingsRes, resourcesRes]) => {
        setRecentBookings(bookingsRes.content);
        setAvailableCount(resourcesRes.totalElements);
      })
      .catch((err) => setError(getApiErrorMessage(err)));
  }, [user?.userId]);

  return (
    <div className="dashboard-page">
      <div className="page-header">
        <h1>Dashboard</h1>
        <p>Welcome back, {user?.name || user?.email}. Role: {user?.role}</p>
      </div>

      {error && (
        <div className="alert alert-error">{error}</div>
      )}

      <div className="dashboard-grid">
        <div className="card dashboard-card">
          <h3>My recent bookings</h3>
          {recentBookings.length === 0 ? (
            <p className="muted">No bookings yet.</p>
          ) : (
            <ul className="dashboard-list">
              {recentBookings.map((b) => (
                <li key={b.id}>
                  <Link to={`/bookings`}>
                    {b.resourceName} — {new Date(b.startTime).toLocaleDateString()} (
                    <span className={`badge badge-${b.status.toLowerCase()}`}>{b.status}</span>)
                  </Link>
                </li>
              ))}
            </ul>
          )}
          <Link to="/bookings" className="btn-secondary" style={{ marginTop: '0.75rem', display: 'inline-block' }}>
            View all bookings
          </Link>
        </div>
        <div className="card dashboard-card">
          <h3>Resources</h3>
          <p className="muted">
            {availableCount !== null ? `${availableCount} available resources` : 'Loading...'}
          </p>
          <Link to="/resources" className="btn-primary" style={{ marginTop: '0.75rem', display: 'inline-block' }}>
            Browse resources
          </Link>
        </div>
      </div>
    </div>
  );
}
