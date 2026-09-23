import React from 'react';
import { CheckCircle2, XCircle, Terminal, AlertTriangle, X } from 'lucide-react';
import type { RunResponse } from '../../services/runService';

interface RunResultPanelProps {
    result: RunResponse | null;
    loading: boolean;
    onClose: () => void;
}

const RunResultPanel: React.FC<RunResultPanelProps> = ({ result, loading, onClose }) => {
    if (!loading && !result) return null;

    return (
        <div style={{
            position: 'absolute',
            bottom: 0, left: 0, right: 0,
            height: '40%',
            backgroundColor: '#1e1e1e',
            borderTop: '1px solid #444',
            color: '#eee',
            display: 'flex',
            flexDirection: 'column',
            zIndex: 10
        }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 15px', borderBottom: '1px solid #333', backgroundColor: '#252526' }}>
                <div style={{ fontWeight: 'bold', display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <Terminal size={18} /> Run Results
                    {result && (
                        <span style={{ 
                            marginLeft: '15px', 
                            color: result.status === 'ACCEPTED' ? '#28a745' : (result.status === 'COMPILATION_ERROR' ? '#ffc107' : '#dc3545'),
                            display: 'flex', alignItems: 'center', gap: '6px'
                        }}>
                            {result.status === 'ACCEPTED' ? <CheckCircle2 size={16} /> : (result.status === 'COMPILATION_ERROR' ? <AlertTriangle size={16} /> : <XCircle size={16} />)}
                            {result.status} ({result.passedTests}/{result.totalTests} passed)
                        </span>
                    )}
                </div>
                <button onClick={onClose} style={{ background: 'none', border: 'none', color: '#aaa', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}><X size={20} /></button>
            </div>
            
            <div style={{ padding: '15px', overflowY: 'auto', flex: 1 }}>
                {loading ? (
                    <div style={{ color: '#888' }}>Running against public test cases...</div>
                ) : result?.compilerOutput ? (
                    <div>
                        <h4 style={{ color: '#dc3545', margin: '0 0 10px 0' }}>Compilation Error</h4>
                        <pre style={{ backgroundColor: '#111', padding: '10px', borderRadius: '4px', overflowX: 'auto', color: '#ff8888' }}>
                            {result.compilerOutput}
                        </pre>
                    </div>
                ) : result?.testResults ? (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                        {result.testResults.map((tr, idx) => (
                            <div key={tr.testCaseId || idx} style={{ border: '1px solid #444', borderRadius: '4px' }}>
                                <div style={{ 
                                    padding: '8px 12px', 
                                    backgroundColor: tr.passed ? 'rgba(40, 167, 69, 0.1)' : 'rgba(220, 53, 69, 0.1)',
                                    borderBottom: '1px solid #444',
                                    fontWeight: 'bold',
                                    color: tr.passed ? '#28a745' : '#dc3545',
                                    display: 'flex', alignItems: 'center', gap: '6px'
                                }}>
                                    {tr.passed ? <CheckCircle2 size={16} /> : <XCircle size={16} />}
                                    Test Case {idx + 1}: {tr.passed ? 'Passed' : 'Failed'}
                                    {!tr.passed && tr.errorMessage && <span style={{ marginLeft: '10px', fontSize: '12px' }}>({tr.errorMessage})</span>}
                                </div>
                                <div style={{ padding: '12px', display: 'flex', gap: '20px' }}>
                                    <div style={{ flex: 1 }}>
                                        <div style={{ fontSize: '12px', color: '#888', marginBottom: '4px' }}>Input:</div>
                                        <pre style={{ margin: 0, padding: '8px', backgroundColor: '#111', borderRadius: '4px' }}>{tr.input}</pre>
                                    </div>
                                    <div style={{ flex: 1 }}>
                                        <div style={{ fontSize: '12px', color: '#888', marginBottom: '4px' }}>Expected Output:</div>
                                        <pre style={{ margin: 0, padding: '8px', backgroundColor: '#111', borderRadius: '4px' }}>{tr.expectedOutput}</pre>
                                    </div>
                                    <div style={{ flex: 1 }}>
                                        <div style={{ fontSize: '12px', color: '#888', marginBottom: '4px' }}>Actual Output:</div>
                                        <pre style={{ margin: 0, padding: '8px', backgroundColor: '#111', borderRadius: '4px', color: tr.passed ? '#eee' : '#ff8888' }}>
                                            {tr.actualOutput || (tr.errorMessage ? `<${tr.errorMessage}>` : '')}
                                        </pre>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                ) : (
                    <div style={{ color: '#888' }}>No results available.</div>
                )}
            </div>
        </div>
    );
};

export default RunResultPanel;
