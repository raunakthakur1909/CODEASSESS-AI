import React from 'react';
import type { Problem } from '../../services/problemService';

interface ProblemPanelProps {
    problem: Problem | null;
}

const ProblemPanel: React.FC<ProblemPanelProps> = ({ problem }) => {
    if (!problem) return <div style={{ padding: '20px' }}>Select a problem</div>;

    const diffColor = problem.difficulty === 'EASY' ? '#28a745' : problem.difficulty === 'MEDIUM' ? '#ffc107' : '#dc3545';

    return (
        <div className="problem-panel" style={{ padding: '20px', height: '100%', overflowY: 'auto', backgroundColor: '#fdfdfd', borderRight: '1px solid #ddd' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '20px' }}>
                <h2 style={{ margin: 0 }}>{problem.title}</h2>
                <span style={{ padding: '4px 8px', borderRadius: '4px', backgroundColor: diffColor, color: 'white', fontSize: '12px', fontWeight: 'bold' }}>
                    {problem.difficulty}
                </span>
                <span style={{ fontSize: '14px', color: '#666' }}>{problem.points} pts</span>
            </div>

            <div style={{ marginBottom: '20px', lineHeight: '1.6', whiteSpace: 'pre-wrap' }}>
                {problem.description}
            </div>

            {problem.inputFormat && (
                <div style={{ marginBottom: '20px' }}>
                    <h4>Input Format</h4>
                    <pre style={{ backgroundColor: '#f4f4f4', padding: '10px', borderRadius: '4px' }}>{problem.inputFormat}</pre>
                </div>
            )}

            {problem.outputFormat && (
                <div style={{ marginBottom: '20px' }}>
                    <h4>Output Format</h4>
                    <pre style={{ backgroundColor: '#f4f4f4', padding: '10px', borderRadius: '4px' }}>{problem.outputFormat}</pre>
                </div>
            )}

            {problem.constraints && (
                <div style={{ marginBottom: '20px' }}>
                    <h4>Constraints</h4>
                    <ul style={{ paddingLeft: '20px' }}>
                        {problem.constraints.split('\n').map((c, i) => (
                            <li key={i}>{c}</li>
                        ))}
                    </ul>
                </div>
            )}
        </div>
    );
};

export default ProblemPanel;
