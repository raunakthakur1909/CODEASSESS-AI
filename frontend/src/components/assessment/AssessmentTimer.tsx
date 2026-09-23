import React from 'react';
import { useAssessmentTimer } from '../../hooks/useAssessmentTimer';

interface AssessmentTimerProps {
  attempt: { expiresAt: string, status: string } | null;
  onExpire: () => void;
}

const AssessmentTimer: React.FC<AssessmentTimerProps> = ({ attempt, onExpire }) => {
  const { formattedTime, isWarning, isExpired } = useAssessmentTimer(attempt, onExpire);

  if (!attempt || !attempt.expiresAt) {
    return (
      <div className="assessment-timer">
        Waiting to start...
      </div>
    );
  }

  const timerStyle = {
    padding: '8px 16px',
    borderRadius: '6px',
    backgroundColor: isExpired ? '#ffebee' : isWarning ? '#fff3cd' : '#e7f3ff',
    color: isExpired ? '#c62828' : isWarning ? '#856404' : '#004085',
    fontWeight: 'bold' as const,
    fontSize: '18px',
    display: 'inline-flex',
    alignItems: 'center',
    gap: '8px',
    transition: 'all 0.3s ease',
    border: `1px solid ${isExpired ? '#ef9a9a' : isWarning ? '#ffeeba' : '#b8daff'}`
  };

  return (
    <div
      className={`assessment-timer ${isWarning ? 'warning' : ''} ${isExpired ? 'expired' : ''}`}
      style={timerStyle}
      aria-live="polite"
      role="timer"
    >
      <span className="timer-icon">⏱</span>
      <span className="timer-countdown">{isExpired ? '00:00' : formattedTime}</span>
      {isWarning && !isExpired && (
        <span className="sr-only">Warning: Less than 5 minutes remaining</span>
      )}
      {isExpired && (
        <span className="sr-only">Time has expired</span>
      )}
    </div>
  );
};

export default AssessmentTimer;
