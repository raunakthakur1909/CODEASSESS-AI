import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { ArrowRight, Code2, Loader2 } from 'lucide-react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';

function Login() {
  const navigate = useNavigate();
  const location = useLocation();
  const [formData, setFormData] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const queryParams = new URLSearchParams(location.search);
  const isExpired = queryParams.get('expired') === 'true';
  const successMessage = location.state?.message;

  useEffect(() => {
    if (isExpired) setError('Your session has expired. Please sign in again.');
  }, [isExpired]);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError('');
    setLoading(true);
    try {
      await authService.login(formData);
      const user = authService.getCurrentUser();
      const from = location.state?.from?.pathname || (user?.role === 'ADMIN' ? '/admin' : '/dashboard');
      navigate(from, { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Invalid credentials. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <section className="auth-header">
          <div className="auth-logo"><Code2 size={25} /></div>
          <h1 className="auth-title">Prove the code.<br />Keep the signal.</h1>
          <p className="auth-subtitle">A calm, evidence-first workspace for engineering assessment, execution, and feedback.</p>
          <div className="auth-protocol"><span /> prompt / implementation / evidence</div>
        </section>
        <section className="auth-form-panel">
          <div className="auth-kicker">Welcome back</div>
          <h2 className="auth-form-title">Enter the workspace.</h2>
          <p className="auth-form-copy">Use your engineering account to continue.</p>
          <form onSubmit={handleSubmit} className="mt-8 flex flex-col gap-4">
            {successMessage && <div className="auth-message auth-message-success">{successMessage}</div>}
            {error && <div className="auth-message auth-message-error" role="alert">{error}</div>}
            <div className="form-group">
              <label htmlFor="email" className="form-label">Work email</label>
              <input id="email" type="email" autoComplete="email" value={formData.email} onChange={(event) => setFormData({ ...formData, email: event.target.value })} required className="form-input" placeholder="you@company.com" />
            </div>
            <div className="form-group">
              <label htmlFor="password" className="form-label">Password</label>
              <input id="password" type="password" autoComplete="current-password" value={formData.password} onChange={(event) => setFormData({ ...formData, password: event.target.value })} required className="form-input" placeholder="Enter your password" />
            </div>
            <div className="auth-form-meta"><label><input type="checkbox" /> Remember me</label><button type="button">Forgot password?</button></div>
            <button type="submit" disabled={loading} className="btn btn-primary w-full">
              {loading ? <><Loader2 size={17} className="animate-spin" /> Signing in...</> : <><span>Sign in</span><ArrowRight size={17} /> </>}
            </button>
          </form>
          <p className="auth-register">Don't have an account? <Link to="/register">Create account</Link></p>
        </section>
      </div>
    </div>
  );
}

export default Login;