import React from 'react';
import { CheckCircle2, XCircle, Clock } from 'lucide-react';

interface Submission {
    id: number;
    language: string;
    status: string;
    result: string;
    score: number;
    passedTests: number;
    totalTests: number;
    submittedAt: string;
    sourceCode?: string;
}

interface SubmissionHistoryProps {
    submissions: Submission[];
    onSelect: (submission: Submission) => void;
}

const SubmissionHistory: React.FC<SubmissionHistoryProps> = ({ submissions, onSelect }) => {
    if (submissions.length === 0) {
        return <div style={{ padding: '10px', color: '#888' }}>No past submissions found.</div>;
    }

    return (
        <div style={{ padding: '10px', overflowY: 'auto', maxHeight: '100%' }}>
            <h4 style={{ margin: '0 0 10px 0', color: '#111' }}>Past Submissions</h4>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                {submissions.map(sub => (
                    <div 
                        key={sub.id}
                        onClick={() => onSelect(sub)}
                        style={{ 
                            padding: '10px', 
                            backgroundColor: '#fff', 
                            borderRadius: '4px',
                            cursor: 'pointer',
                            border: '1px solid #ddd',
                            borderLeft: `4px solid ${sub.result === 'ACCEPTED' ? '#28a745' : '#dc3545'}`,
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center'
                        }}
                    >
                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                            {sub.result === 'ACCEPTED' ? (
                                <CheckCircle2 size={20} color="#28a745" />
                            ) : (
                                <XCircle size={20} color="#dc3545" />
                            )}
                            <div>
                                <div style={{ fontWeight: 'bold', color: '#333', fontSize: '14px' }}>{sub.result}</div>
                                <div style={{ fontSize: '12px', color: '#666', marginTop: '4px', display: 'flex', alignItems: 'center', gap: '4px' }}>
                                    <Clock size={12} />
                                    {new Date(sub.submittedAt).toLocaleTimeString()} • {sub.language}
                                </div>
                            </div>
                        </div>
                        <div style={{ fontSize: '14px', fontWeight: 'bold', color: '#333' }}>
                            {sub.score}%
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default SubmissionHistory;
