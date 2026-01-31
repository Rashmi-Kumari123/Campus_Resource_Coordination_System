import { Link } from 'react-router-dom';
import './Landing.css';

export function Landing() {
  return (
    <div className="landing">
      <header className="landing-header">
        <Link to="/" className="landing-brand">
          <span className="landing-brand-icon">◉</span>
          CRCS
        </Link>
        <nav className="landing-nav">
          <Link to="/login" className="landing-link">
            Login
          </Link>
          <Link to="/signup" className="landing-cta-link">
            Sign up
          </Link>
        </nav>
      </header>

      <main className="landing-main">
        <section className="landing-hero">
          <h1 className="landing-title">
            Campus Resource Coordination System
          </h1>
          <p className="landing-tagline">
            Book rooms, labs, and equipment in one place. See real-time availability and get notified when your booking is confirmed or cancelled.
          </p>
          <Link to="/signup" className="landing-btn">
            Get started
          </Link>
        </section>

        <section className="landing-features">
          <div className="landing-feature">
            <span className="landing-feature-icon">📋</span>
            <h3>Browse & book</h3>
            <p>View and book rooms, labs, halls, and equipment. Pick a slot and submit your request.</p>
          </div>
          <div className="landing-feature">
            <span className="landing-feature-icon">✓</span>
            <h3>Real-time availability</h3>
            <p>See which resources are available when. No double-booking, no guesswork.</p>
          </div>
          <div className="landing-feature">
            <span className="landing-feature-icon">✉</span>
            <h3>Email notifications</h3>
            <p>Get notified when your booking is confirmed or cancelled. Stay in the loop.</p>
          </div>
        </section>
      </main>
    </div>
  );
}
