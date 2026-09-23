import React from 'react';
import { useNavigate } from 'react-router-dom';
import { LogOut } from 'lucide-react';
import AssessmentTimer from './AssessmentTimer';
import { authService } from '../../services/authService';

interface AssessmentHeaderProps {
  title: string;
  assessment: { durationMinutes: number } | null;
  attempt: { expiresAt: string, status: string } | null;
  onExpire: () => void;
  onExit: () => void;
}

const AssessmentHeader: React.FC<AssessmentHeaderProps> = ({ title, attempt, onExpire, onExit }) => {
  const navigate = useNavigate();

  const handleLogout = () => {
    authService.logout();
    navigate('/login');
  };

  return (
    <div className="assessment-header" style={{
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      padding: '16px 24px',
      backgroundColor: '#ffffff',
      borderBottom: '1px solid #eaeaea',
      boxShadow: '0 2px 4px rgba(0,0,0,0.02)'
    }}>
      <div className="assessment-header-left">
        <h2 style={{ margin: 0, fontSize: '20px', color: '#333' }}>{title}</h2>
      </div>
      <div className="assessment-header-center">
        <AssessmentTimer attempt={attempt} onExpire={onExpire} />
      </div>
      <div className="assessment-header-right" style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
        <button 
          onClick={onExit}
          style={{
            padding: '8px 16px',
            backgroundColor: '#f8f9fa',
            border: '1px solid #ddd',
            borderRadius: '4px',
            cursor: 'pointer',
            fontSize: '14px'
          }}
        >
          Exit
        </button>
        <button 
          onClick={handleLogout}
          style={{
            padding: '8px 16px',
            backgroundColor: '#dc3545',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer',
            color: '#ffffff',
            fontSize: '14px',
            display: 'flex',
            alignItems: 'center',
            gap: '6px'
          }}
        >
          <LogOut size={16} />
          Logout
        </button>
      </div>
    </div>
  );
};

export default AssessmentHeader;
