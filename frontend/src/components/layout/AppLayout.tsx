import { useState } from 'react';
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import {
  BarChart3,
  BookOpenCheck,
  Code2,
  LayoutDashboard,
  LineChart,
  LogOut,
  Menu,
  Trophy,
  X,
} from 'lucide-react';
import { authService } from '../../services/authService';
import { NotificationsDropdown } from './NotificationsDropdown';
import { ThemeToggle } from './ThemeToggle';

const navItems = [
  { name: 'Overview', path: '/dashboard', icon: LayoutDashboard },
  { name: 'Assessments', path: '/assessments', icon: BookOpenCheck },
  { name: 'Analytics', path: '/analytics', icon: LineChart },
  { name: 'Leaderboard', path: '/leaderboard', icon: Trophy },
];

function Brand() {
  return (
    <Link to="/dashboard" className="brand-mark" aria-label="CodeAssess AI home">
      <span className="brand-icon"><Code2 size={19} strokeWidth={2.6} /></span>
      <span className="brand-copy">
        <strong>CodeAssess</strong>
        <small>AI / engineering proof</small>
      </span>
    </Link>
  );
}

function Sidebar({ open, onClose }: { open: boolean; onClose: () => void }) {
  const location = useLocation();
  const navigate = useNavigate();
  const user = authService.getCurrentUser();

  const logout = () => {
    authService.logout();
    navigate('/login');
  };

  return (
    <>
      {open && <button className="sidebar-scrim" type="button" onClick={onClose} aria-label="Close navigation" />}
      <aside className={`app-sidebar ${open ? 'is-open' : ''}`}>
        <div className="sidebar-top">
          <Brand />
          <button type="button" className="icon-button mobile-only" onClick={onClose} aria-label="Close navigation">
            <X size={17} />
          </button>
        </div>
        <div className="sidebar-label">Candidate workspace</div>
        <nav className="sidebar-nav" aria-label="Candidate navigation">
          {navItems.map(({ name, path, icon: Icon }) => {
            const active = path === '/dashboard'
              ? location.pathname === '/dashboard'
              : location.pathname.startsWith(path);
            return (
              <Link
                key={`${name}-${path}`}
                to={path}
                onClick={onClose}
                className={`sidebar-link ${active ? 'is-active' : ''}`}
              >
                <Icon size={16} />
                <span>{name}</span>
                {active && <span className="sidebar-active-dot" />}
              </Link>
            );
          })}
        </nav>
        <div className="sidebar-footer">
          <div className="engine-card">
            <div className="engine-heading"><span className="engine-dot" /> Evaluation engine</div>
            <p>Live execution, submissions, and AI review stay connected to your account.</p>
            <span className="engine-status"><span /> API channel</span>
          </div>
          <div className="account-row">
            <span className="avatar">{(user?.email?.[0] || 'C').toUpperCase()}</span>
            <div className="account-copy">
              <strong>{user?.email?.split('@')[0] || 'Candidate'}</strong>
              <small>candidate account</small>
            </div>
            <button type="button" className="icon-button" onClick={logout} aria-label="Log out">
              <LogOut size={15} />
            </button>
          </div>
        </div>
      </aside>
    </>
  );
}

export default function AppLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const location = useLocation();
  const user = authService.getCurrentUser();

  return (
    <div className="app-container noise">
      <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      <div className="main-content">
        <header className="topbar">
          <div className="topbar-left">
            <button type="button" className="icon-button mobile-only" onClick={() => setSidebarOpen(true)} aria-label="Open navigation">
              <Menu size={18} />
            </button>
            <div className="breadcrumb"><span>workspace</span><b>/</b><span>{location.pathname === '/dashboard' ? 'overview' : location.pathname.split('/')[1]}</span></div>
          </div>
          <div className="topbar-actions">
            <span className="connection-pill"><span /> API connected</span>
            <NotificationsDropdown />
            <ThemeToggle />
            <div className="topbar-user"><span className="avatar avatar-small">{(user?.email?.[0] || 'C').toUpperCase()}</span><span>{user?.email?.split('@')[0] || 'Candidate'}</span></div>
          </div>
        </header>
        <main className="page-content"><Outlet /></main>
      </div>
    </div>
  );
}