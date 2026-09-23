import { useState } from 'react';
import type { FormEvent } from 'react';
import { useNavigate, Link, useLocation } from 'react-router-dom';
import { Code2, ArrowRight, Loader2 } from 'lucide-react';
import { authService } from '../services/authService';

function Login() {
  const navigate = useNavigate();
  const location = useLocation();
  const [formData, setFormData] = useState({
    email: '',
    password: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

    const queryParams = new URLSearchParams(location.search);
    const isExpired = queryParams.get('expired') === 'true';
    const successMessage = location.state?.message;

    // Use useEffect to clear the error after a while, or just display it
    useState(() => {
        if (isExpired) {
            setError('Your session has expired. Please sign in again.');
        }
    });

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
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
                <div className="auth-header">
                    <div className="auth-logo">
                        <Code2 size={28} />
                    </div>
                    <h1 className="auth-title">Welcome back</h1>
                    <p className="auth-subtitle">Sign in to continue your coding journey</p>
                </div>

                <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                    {successMessage && (
                        <div className="bg-success-bg text-success-text px-4 py-3 rounded-md text-sm border border-success-text/20">
                            {successMessage}
                        </div>
                    )}
                    {error && (
                        <div className="bg-danger-bg text-danger-text px-4 py-3 rounded-md text-sm border border-danger-text/20">
                            {error}
                        </div>
                    )}

          <div className="form-group">
            <label htmlFor="email" className="form-label">
              Email address
            </label>
            <input
              id="email"
              type="email"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              required
              className="form-input"
              placeholder="name@example.com"
            />
          </div>

          <div className="form-group mb-6">
            <label htmlFor="password" className="form-label">
              Password
            </label>
            <input
              id="password"
              type="password"
              value={formData.password}
              onChange={(e) => setFormData({ ...formData, password: e.target.value })}
              required
              className="form-input"
              placeholder="••••••••"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="btn btn-primary w-full justify-center py-2.5 text-base"
          >
            {loading ? (
              <>
                <Loader2 size={18} className="animate-spin" />
                Signing in...
              </>
            ) : (
              <>
                Sign In
                <ArrowRight size={18} />
              </>
            )}
          </button>
        </form>

        <p className="mt-8 text-center text-sm text-secondary">
          Don't have an account?{' '}
          <Link to="/register" className="font-semibold text-primary hover:text-primary-hover">
            Create account
          </Link>
        </p>
      </div>
    </div>
  );
}

export default Login;
