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
  ShieldCheck,
  X,
} from 'lucide-react';
import { authService } from '../../services/authService';
import { NotificationsDropdown } from './NotificationsDropdown';
import { ThemeToggle } from './ThemeToggle';

const navItems = [
  { name: 'Admin home', path: '/admin', icon: LayoutDashboard },
  { name: 'Manage assessments', path: '/admin/assessments', icon: BookOpenCheck },
  { name: 'Analytics', path: '/admin/analytics', icon: LineChart },
  { name: 'AI analytics', path: '/admin/analytics/ai', icon: BarChart3 },
];

function Brand() {
  return (
    <Link to="/admin" className="brand-mark" aria-label="CodeAssess AI admin home">
      <span className="brand-icon"><Code2 size={19} strokeWidth={2.6} /></span>
      <span className="brand-copy">
        <strong>CodeAssess</strong>
        <small>AI / admin console</small>
      </span>
    </Link>
  );
}

export default function AdminLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();
  const user = authService.getCurrentUser();

  const logout = () => {
    authService.logout();
    navigate('/login');
  };

  return (
    <div className="app-container noise">
      {sidebarOpen && <button className="sidebar-scrim" type="button" onClick={() => setSidebarOpen(false)} aria-label="Close navigation" />}
      <aside className={`app-sidebar ${sidebarOpen ? 'is-open' : ''}`}>
        <div className="sidebar-top">
          <Brand />
          <button type="button" className="icon-button mobile-only" onClick={() => setSidebarOpen(false)} aria-label="Close navigation"><X size={17} /></button>
        </div>
        <div className="sidebar-label">Admin console</div>
        <nav className="sidebar-nav" aria-label="Admin navigation">
          {navItems.map(({ name, path, icon: Icon }) => {
            const active = path === '/admin'
              ? location.pathname === '/admin'
              : location.pathname.startsWith(path);
            return (
              <Link key={`${name}-${path}`} to={path} onClick={() => setSidebarOpen(false)} className={`sidebar-link ${active ? 'is-active' : ''}`}>
                <Icon size={16} />
                <span>{name}</span>
                {active && <span className="sidebar-active-dot" />}
              </Link>
            );
          })}
        </nav>
        <div className="sidebar-footer">
          <div className="engine-card">
            <div className="engine-heading"><ShieldCheck size={14} /> Admin access</div>
            <p>Assessment operations, candidate access, and review signal in one place.</p>
            <span className="engine-status"><span /> RBAC protected</span>
          </div>
          <div className="account-row">
            <span className="avatar">{(user?.email?.[0] || 'A').toUpperCase()}</span>
            <div className="account-copy">
              <strong>{user?.email?.split('@')[0] || 'Administrator'}</strong>
              <small>admin account</small>
            </div>
            <button type="button" className="icon-button" onClick={logout} aria-label="Log out"><LogOut size={15} /></button>
          </div>
        </div>
      </aside>
      <div className="main-content">
        <header className="topbar">
          <div className="topbar-left">
            <button type="button" className="icon-button mobile-only" onClick={() => setSidebarOpen(true)} aria-label="Open navigation"><Menu size={18} /></button>
            <div className="breadcrumb"><span>admin</span><b>/</b><span>{location.pathname === '/admin' ? 'overview' : location.pathname.slice('/admin/'.length).split('/')[0]}</span></div>
          </div>
          <div className="topbar-actions">
            <span className="connection-pill"><span /> API connected</span>
            <NotificationsDropdown />
            <ThemeToggle />
            <div className="topbar-user"><span className="avatar avatar-small">{(user?.email?.[0] || 'A').toUpperCase()}</span><span>{user?.email?.split('@')[0] || 'Administrator'}</span></div>
          </div>
        </header>
        <main className="page-content"><Outlet /></main>
      </div>
    </div>
  );
}