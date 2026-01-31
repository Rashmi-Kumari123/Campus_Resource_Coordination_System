
import { useEffect, useState, useRef } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { resourcesApi } from '../api/resources';
import { bookingsApi } from '../api/bookings';
import type { Resource } from '../types';
import { getApiErrorMessage } from '../api/auth';

export function CreateBooking() {
  const navigate = useNavigate();
  const [resources, setResources] = useState<Resource[]>([]);
  const [resourceId, setResourceId] = useState('');
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  const [purpose, setPurpose] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const navigateTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    resourcesApi
      .getAvailable({ page: 0, size: 100 })
      .then((res) => setResources(res.content))
      .catch((err) => setError(getApiErrorMessage(err)));
  }, []);

  useEffect(() => {
    return () => {
      if (navigateTimeoutRef.current) clearTimeout(navigateTimeoutRef.current);
    };
  }, []);

  const toISO = (local: string) =>
    local && local.length === 16 ? `${local}:00` : local;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!resourceId || !startTime || !endTime) {
      setError('Resource, start time and end time are required.');
      return;
    }
    setError(null);
    setLoading(true);
    try {
      await bookingsApi.create({
        resourceId,
        startTime: toISO(startTime),
        endTime: toISO(endTime),
        purpose: purpose || undefined,
      });
      setSuccessMessage('Booking created successfully');
      navigateTimeoutRef.current = setTimeout(() => {
        navigate('/bookings');
      }, 2000);
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      {successMessage && (
        <div className="toast toast-success" role="status" aria-live="polite">
          {successMessage}
        </div>
      )}
      <div className="page-header">
        <h1>New booking</h1>
        <p>
          <Link to="/bookings">← Back to my bookings</Link>
        </p>
      </div>
      {error && <div className="alert alert-error">{error}</div>}
      <div className="card">
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Resource *</label>
            <select
              value={resourceId}
              onChange={(e) => setResourceId(e.target.value)}
              required
            >
              <option value="">Select a resource</option>
              {resources.map((r) => (
                <option key={r.id} value={r.id}>
                  {r.name} ({r.type}) — {r.location ?? 'N/A'}
                </option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label>Start time * (ISO format or use datetime-local)</label>
            <input
              type="datetime-local"
              value={startTime}
              onChange={(e) => setStartTime(e.target.value)}
              required
            />
          </div>
          <div className="form-group">
            <label>End time *</label>
            <input
              type="datetime-local"
              value={endTime}
              onChange={(e) => setEndTime(e.target.value)}
              required
            />
          </div>
          <div className="form-group">
            <label>Purpose</label>
            <input value={purpose} onChange={(e) => setPurpose(e.target.value)} placeholder="e.g. Team meeting" />
          </div>
          <div className="form-actions" style={{ display: 'flex', gap: '0.75rem', marginTop: '1rem' }}>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Creating...' : 'Create booking'}
            </button>
            <Link to="/bookings" className="btn-secondary">
              Cancel
            </Link>
          </div>
        </form>
      </div>
    </div>
  );
}
