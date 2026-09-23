import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { 
  Code2, 
  LayoutDashboard, 
  BookOpen, 
  History, 
  BarChart2, 
  LogOut,
  Bell,
  User
} from 'lucide-react';
import { authService } from '../../services/authService';

export default function AppLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const user = authService.getCurrentUser();

  const handleLogout = () => {
    authService.logout();
    navigate('/login');
  };

  const navItems = [
    { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
    { name: 'Assessments', path: '/assessments', icon: BookOpen },
    { name: 'Submissions', path: '/submissions', icon: History },
    { name: 'Analytics', path: '/analytics', icon: BarChart2 },
  ];

  return (
    <div className="app-container">
      {/* Sidebar */}
      <aside className="w-64 bg-bg-sidebar border-r border-border-light flex flex-col hidden md:flex" style={{ width: '256px', flexShrink: 0, display: 'flex', flexDirection: 'column', borderRight: '1px solid var(--border-light)', backgroundColor: 'var(--bg-sidebar)' }}>
        <div className="p-6 flex items-center gap-3">
          <div className="text-white bg-primary p-1.5 rounded-md" style={{ backgroundColor: 'var(--primary)', color: 'white' }}>
            <Code2 size={24} />
          </div>
          <span className="font-bold text-lg tracking-tight" style={{ color: 'var(--text-primary)' }}>CodeAssess AI</span>
        </div>

        <nav className="flex-1 px-4 py-4 flex flex-col gap-1" style={{ flex: 1, padding: '16px', display: 'flex', flexDirection: 'column', gap: '4px' }}>
          {navItems.map((item) => {
            const isActive = location.pathname.startsWith(item.path);
            const Icon = item.icon;
            
            return (
              <Link
                key={item.path}
                to={item.path}
                className={`flex items-center gap-3 px-3 py-2.5 rounded-md text-sm font-medium transition-colors`}
                style={{ 
                  display: 'flex', alignItems: 'center', gap: '12px', padding: '10px 12px', borderRadius: '6px', fontSize: '14px', fontWeight: 500,
                  backgroundColor: isActive ? 'var(--primary-light)' : 'transparent',
                  color: isActive ? 'var(--primary-hover)' : 'var(--text-secondary)',
                  textDecoration: 'none'
                }}
              >
                <Icon size={18} />
                {item.name}
              </Link>
            );
          })}
        </nav>

        <div className="p-4 border-t border-border-light" style={{ padding: '16px', borderTop: '1px solid var(--border-light)' }}>
          <button 
            onClick={handleLogout}
            className="flex items-center gap-3 w-full px-3 py-2.5 rounded-md text-sm font-medium transition-colors"
            style={{ display: 'flex', alignItems: 'center', gap: '12px', width: '100%', padding: '10px 12px', borderRadius: '6px', fontSize: '14px', fontWeight: 500, backgroundColor: 'transparent', border: 'none', textAlign: 'left', cursor: 'pointer', color: 'var(--danger)' }}
          >
            <LogOut size={18} />
            Logout
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <div className="main-content">
        {/* Top Header */}
        <header className="h-16 bg-bg-card border-b border-border-light px-6 flex items-center justify-between" style={{ height: '64px', backgroundColor: 'var(--bg-card)', backdropFilter: 'blur(12px)', borderBottom: '1px solid var(--border-light)', padding: '0 24px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div className="md:hidden flex items-center gap-3">
            <div className="text-primary">
              <Code2 size={24} />
            </div>
            <span className="font-bold">CodeAssess AI</span>
          </div>
          
          <div className="hidden md:block"></div> {/* Spacer */}

          <div className="flex items-center gap-4" style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            <button className="text-text-secondary hover:text-text-primary transition-colors bg-transparent border-none cursor-pointer">
              <Bell size={20} />
            </button>
            
            <div className="flex items-center gap-2 pl-4 border-l border-border-light" style={{ display: 'flex', alignItems: 'center', gap: '8px', paddingLeft: '16px', borderLeft: '1px solid var(--border-light)' }}>
              <div className="w-8 h-8 rounded-full bg-primary text-white flex items-center justify-center font-bold text-sm" style={{ width: '32px', height: '32px', borderRadius: '50%', backgroundColor: 'var(--primary)', color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '14px', fontWeight: 'bold' }}>
                <User size={16} />
              </div>
              <span className="text-sm font-medium hidden sm:block" style={{ fontSize: '14px', fontWeight: 500, color: 'var(--text-primary)' }}>
                {user?.email?.split('@')[0] || 'Candidate'}
              </span>
            </div>
          </div>
        </header>

        {/* Page Content */}
        <main className="flex-1 overflow-y-auto bg-transparent p-6" style={{ flex: 1, overflowY: 'auto', backgroundColor: 'transparent', padding: '24px' }}>
          <Outlet />
        </main>
      </div>
    </div>
  );
}
