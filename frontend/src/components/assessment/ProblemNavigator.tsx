import React from 'react';

interface Problem {
  id: number;
  title: string;
  points: number;
}

interface ProblemNavigatorProps {
  problems: Problem[];
  activeProblemId: number | null;
  onSelectProblem: (id: number) => void;
  disabled?: boolean;
}

const ProblemNavigator: React.FC<ProblemNavigatorProps> = ({ 
  problems, 
  activeProblemId, 
  onSelectProblem,
  disabled = false
}) => {
  return (
    <div className="problem-navigator" style={{
      width: '250px',
      borderRight: '1px solid #eaeaea',
      backgroundColor: '#fafafa',
      display: 'flex',
      flexDirection: 'column',
      height: '100%'
    }}>
      <div style={{ padding: '16px', borderBottom: '1px solid #eaeaea', fontWeight: 'bold' }}>
        Questions
      </div>
      <div style={{ overflowY: 'auto', flex: 1 }}>
        {problems.length === 0 ? (
          <div style={{ padding: '16px', color: '#666' }}>No problems available.</div>
        ) : (
          <ul style={{ listStyle: 'none', margin: 0, padding: 0 }}>
            {problems.map((p, index) => (
              <li key={p.id}>
                <button
                  onClick={() => onSelectProblem(p.id)}
                  disabled={disabled}
                  style={{
                    width: '100%',
                    textAlign: 'left',
                    padding: '12px 16px',
                    border: 'none',
                    borderBottom: '1px solid #eee',
                    backgroundColor: activeProblemId === p.id ? '#e7f3ff' : 'transparent',
                    color: activeProblemId === p.id ? '#0056b3' : '#333',
                    cursor: disabled ? 'not-allowed' : 'pointer',
                    opacity: disabled ? 0.6 : 1,
                    display: 'flex',
                    justifyContent: 'space-between'
                  }}
                >
                  <span>{index + 1}. {p.title}</span>
                  <span style={{ fontSize: '12px', color: '#666' }}>{p.points} pts</span>
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
};

export default ProblemNavigator;
