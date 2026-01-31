import { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { resourcesApi } from '../api/resources';
import { useAuth } from '../context/AuthContext';
import type { CreateResourceRequest, ResourceType } from '../types';
import { getApiErrorMessage } from '../api/auth';
import './ResourceForm.css';

export function ResourceForm() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const isEdit = Boolean(id && id !== 'new');

  const [name, setName] = useState('');
  const [type, setType] = useState<ResourceType>('ROOM');
  const [description, setDescription] = useState('');
  const [location, setLocation] = useState('');
  const [capacity, setCapacity] = useState<number | ''>('');
  const [responsiblePerson, setResponsiblePerson] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const navigateTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    if (!isEdit || !id) return;
    resourcesApi
      .getById(id)
      .then((r) => {
        setName(r.name);
        setType(r.type);
        setDescription(r.description ?? '');
        setLocation(r.location ?? '');
        setCapacity(r.capacity ?? '');
        setResponsiblePerson(r.responsiblePerson ?? '');
      })
      .catch((err) => setError(getApiErrorMessage(err)));
  }, [id, isEdit]);

  useEffect(() => {
    return () => {
      if (navigateTimeoutRef.current) clearTimeout(navigateTimeoutRef.current);
    };
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    const body: CreateResourceRequest = {
      name,
      type,
      description: description || undefined,
      location: location || undefined,
      capacity: capacity === '' ? undefined : Number(capacity),
      ownerId: user?.userId,
      responsiblePerson: responsiblePerson || undefined,
    };
    try {
      if (isEdit && id) {
        await resourcesApi.update(id, {
          name: body.name,
          description: body.description,
          location: body.location,
          capacity: body.capacity,
        });
        setSuccessMessage('Resource updated successfully');
        navigateTimeoutRef.current = setTimeout(() => {
          navigate(`/resources/${id}`);
        }, 2000);
      } else {
        const created = await resourcesApi.create(body);
        setSuccessMessage('Resource created successfully');
        navigateTimeoutRef.current = setTimeout(() => {
          navigate(`/resources/${created.id}`);
        }, 2000);
      }
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
        <h1>{isEdit ? 'Edit resource' : 'Add resource'}</h1>
        <p>
          <Link to="/resources">← Back to resources</Link>
        </p>
      </div>
      {error && <div className="alert alert-error">{error}</div>}
      <div className="card">
        <form onSubmit={handleSubmit} className="resource-form">
          <div className="form-group">
            <label>Name *</label>
            <input value={name} onChange={(e) => setName(e.target.value)} required />
          </div>
          <div className="form-group">
            <label>Type</label>
            <select value={type} onChange={(e) => setType(e.target.value as ResourceType)}>
              <option value="ROOM">Room</option>
              <option value="LAB">Lab</option>
              <option value="HALL">Hall</option>
              <option value="EQUIPMENT">Equipment</option>
              <option value="CAFETERIA">Cafeteria</option>
              <option value="LIBRARY">Library</option>
              <option value="PARKING">Parking</option>
              <option value="SPORTS">Sports</option>
            </select>
          </div>
          <div className="form-group">
            <label>Description</label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={3}
            />
          </div>
          <div className="form-group">
            <label>Location</label>
            <input value={location} onChange={(e) => setLocation(e.target.value)} />
          </div>
          <div className="form-group">
            <label>Capacity</label>
            <input
              type="number"
              min={0}
              value={capacity}
              onChange={(e) => setCapacity(e.target.value === '' ? '' : Number(e.target.value))}
            />
          </div>
          <div className="form-group">
            <label>Responsible person</label>
            <input value={responsiblePerson} onChange={(e) => setResponsiblePerson(e.target.value)} />
          </div>
          <div className="form-actions">
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Saving...' : isEdit ? 'Save changes' : 'Create resource'}
            </button>
            <Link to={isEdit ? `/resources/${id}` : '/resources'} className="btn-secondary">
              Cancel
            </Link>
          </div>
        </form>
      </div>
    </div>
  );
}
