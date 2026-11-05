import { useState } from 'react';
import type { FormEvent } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Code2, ArrowRight, Loader2 } from 'lucide-react';
import { authService } from '../services/authService';

function Register() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await authService.register(formData);
      // Automatically navigate to login page on success
      navigate('/login', { state: { message: 'Registration successful! Please login.' } });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <section className="auth-header">
          <div className="auth-logo"><Code2 size={25} /></div>
          <h1 className="auth-title">Prove the code.<br />Join the platform.</h1>
          <p className="auth-subtitle">Create your engineering account to participate in assessments, run code in real-time, and view AI reviews.</p>
          <div className="auth-protocol"><span /> candidate / assessment / review</div>
        </section>
        <section className="auth-form-panel">
          <div className="auth-kicker">Get started</div>
          <h2 className="auth-form-title">Create account.</h2>
          <p className="auth-form-copy">Register to begin your technical evaluations.</p>
          <form onSubmit={handleSubmit} className="mt-8 flex flex-col gap-4">
            {error && <div className="auth-message auth-message-error" role="alert">{error}</div>}
            <div className="form-group">
              <label htmlFor="name" className="form-label">Full Name</label>
              <input
                id="name"
                type="text"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                required
                minLength={2}
                className="form-input"
                placeholder="John Doe"
              />
            </div>
            <div className="form-group">
              <label htmlFor="email" className="form-label">Work or Personal Email</label>
              <input
                id="email"
                type="email"
                autoComplete="email"
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                required
                className="form-input"
                placeholder="name@example.com"
              />
            </div>
            <div className="form-group">
              <label htmlFor="password" className="form-label">Password</label>
              <input
                id="password"
                type="password"
                autoComplete="new-password"
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                required
                minLength={8}
                className="form-input"
                placeholder="Minimum 8 characters"
              />
            </div>
            <button
              type="submit"
              disabled={loading}
              className="btn btn-primary w-full mt-2"
            >
              {loading ? (
                <>
                  <Loader2 size={17} className="animate-spin" />
                  <span>Creating account...</span>
                </>
              ) : (
                <>
                  <span>Create Account</span>
                  <ArrowRight size={17} />
                </>
              )}
            </button>
          </form>
          <p className="auth-register">
            Already have an account? <Link to="/login">Sign in</Link>
          </p>
        </section>
      </div>
    </div>
  );
}

export default Register;
