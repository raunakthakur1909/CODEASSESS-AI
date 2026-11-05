import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import AssessmentHeader from '../components/assessment/AssessmentHeader';
import ProblemNavigator from '../components/assessment/ProblemNavigator';
import ProblemPanel from '../components/assessment/ProblemPanel';
import { authService } from '../services/authService';
import CodeEditor from '../components/assessment/CodeEditor';
import LanguageSelector from '../components/assessment/LanguageSelector';
import RunResultPanel from '../components/assessment/RunResultPanel';
import { useAssessmentEvents } from '../hooks/useAssessmentEvents';
import { problemService } from '../services/problemService';
import type { Problem } from '../services/problemService';
import { runService } from '../services/runService';
import type { RunResponse } from '../services/runService';
import { draftService } from '../services/draftService';
import { submissionService } from '../services/submissionService';
import type { Submission } from '../services/submissionService';
import SubmissionHistory from '../components/assessment/SubmissionHistory';
import { Loader2 } from 'lucide-react';
import { getStarterTemplate } from '../utils/starterTemplates';
import './AssessmentWorkspacePage.css';

interface AssessmentData {
    id: number;
    title: string;
    durationMinutes: number;
    startedAt?: string | null;
    status: string;
    instructions?: string;
}

interface AssessmentAttempt {
    id: number;
    candidateId: number;
    assessmentId: number;
    startedAt: string;
    expiresAt: string;
    status: string;
}

interface AssessmentWorkspacePageProps {
    assessmentId: number;
    onExit: () => void;
}

/**
 * Fallback only. Used when a problem has no configured starterLanguage.
 * Per-problem language always wins — see the editor content effect below.
 */
const DEFAULT_LANGUAGE = 'JAVA';

const AssessmentWorkspacePage: React.FC<AssessmentWorkspacePageProps> = ({ assessmentId, onExit }) => {
    const navigate = useNavigate();
    const [assessment, setAssessment] = useState<AssessmentData | null>(null);
    const [attempt, setAttempt] = useState<AssessmentAttempt | null>(null);
    const [problems, setProblems] = useState<Problem[]>([]);
    const [activeProblemId, setActiveProblemId] = useState<number | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [isExpired, setIsExpired] = useState(false);
    const [warningMessage, setWarningMessage] = useState<string | null>(null);

    const [language, setLanguage] = useState(DEFAULT_LANGUAGE);
    const [code, setCode] = useState('');
    
    const [isRunning, setIsRunning] = useState(false);
    const [runResult, setRunResult] = useState<RunResponse | null>(null);
    const [showRunPanel, setShowRunPanel] = useState(false);

    const [isSubmitting, setIsSubmitting] = useState(false);
    const [actionError, setActionError] = useState<string | null>(null);

    const [activeTab, setActiveTab] = useState<'problem' | 'instructions' | 'submissions'>('problem');
    const [submissions, setSubmissions] = useState<Submission[]>([]);

    const { event: sseEvent } = useAssessmentEvents(assessmentId);

    const draftTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
    const isFirstLoadRef = useRef(true);
    const loadedForProblemRef = useRef<number | null>(null);
    const editorRef = useRef<any>(null);

    useEffect(() => {
        if (activeTab === 'submissions' && activeProblemId) {
            submissionService.getMyProblemSubmissions(activeProblemId)
                .then(data => setSubmissions(data))
                .catch(err => console.error("Failed to load submissions", err));
        }
    }, [activeTab, activeProblemId]);

    useEffect(() => {
        if (sseEvent) {
            if (sseEvent.type === 'ASSESSMENT_EXPIRING') {
                setWarningMessage('Warning: Assessment will expire in 5 minutes!');
            } else if (sseEvent.type === 'ASSESSMENT_EXPIRED' || sseEvent.status === 'CLOSED') {
                setIsExpired(true);
                setWarningMessage(null);
            }
        }
    }, [sseEvent]);

    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                const token = authService.getToken();
                const headers = { 'Authorization': `Bearer ${token}` };

                const res = await fetch(`/api/assessments/${assessmentId}`, { headers });
                if (!res.ok) throw new Error('Failed to load assessment');
                const data = await res.json();
                setAssessment(data);

                const attemptRes = await fetch(`/api/assessments/${assessmentId}/attempt`, {
                    method: 'POST',
                    headers
                });
                if (!attemptRes.ok) {
                    const errorData = await attemptRes.json().catch(() => ({ message: 'Failed to start/load assessment attempt' }));
                    throw new Error(errorData.message || 'Failed to start/load assessment attempt');
                }
                const attemptData = await attemptRes.json();
                setAttempt(attemptData);
                if (attemptData.status === 'EXPIRED') {
                    setIsExpired(true);
                }

                const probData = await problemService.getProblems(assessmentId);
                setProblems(probData);
                if (probData.length > 0) {
                    setActiveProblemId(probData[0].id);
                }
            } catch (err: any) {
                setError(err.message || 'An error occurred');
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [assessmentId]);

    /*
     * Editor content resolution, in priority order:
     *   1. newest draft (server vs localStorage)
     *   2. the problem's starter code
     *   3. empty editor
     *
     * Guarded by loadedForProblemRef rather than the dependency array so
     * that `problems` arriving in the same render as the initial
     * activeProblemId does not re-trigger a load and clobber typing,
     * while switching back to an earlier problem still reloads it.
     */
    useEffect(() => {
        if (!activeProblemId) return;

        const problem = problems.find(p => p.id === activeProblemId);
        if (!problem) return;

        if (loadedForProblemRef.current === activeProblemId) return;
        loadedForProblemRef.current = activeProblemId;

        isFirstLoadRef.current = true;

        const initEmptyWorkspace = () => {
            setLanguage(problem.starterLanguage ?? DEFAULT_LANGUAGE);
            setCode('');
        };

        const loadEditorContent = async () => {
            try {
                const serverDraft = await draftService.getDraft(assessmentId, activeProblemId);

                if (serverDraft && serverDraft.sourceCode !== undefined && serverDraft.sourceCode !== null) {
                    setLanguage(serverDraft.language);
                    setCode(serverDraft.sourceCode);
                    draftService.saveLocalDraft(
                        assessmentId,
                        activeProblemId,
                        serverDraft.language,
                        serverDraft.sourceCode
                    );
                } else {
                    // When starting solving the problem, no draft exists -> editor starts empty
                    draftService.clearLocalDraft(assessmentId, activeProblemId);
                    initEmptyWorkspace();
                }
            } catch (err) {
                console.error('Failed to load draft, initializing empty workspace', err);
                initEmptyWorkspace();
            } finally {
                isFirstLoadRef.current = false;
            }
        };

        loadEditorContent();
    }, [activeProblemId, assessmentId, problems]);

    useEffect(() => {
        if (isFirstLoadRef.current || !activeProblemId) return;

        if (draftTimerRef.current) {
            clearTimeout(draftTimerRef.current);
        }

        draftService.saveLocalDraft(assessmentId, activeProblemId, language, code);

        draftTimerRef.current = setTimeout(() => {
            draftService.saveDraft(assessmentId, activeProblemId, { language, sourceCode: code })
                .catch(err => console.error("Failed to save draft to server", err));
        }, 3000);

        return () => {
            if (draftTimerRef.current) clearTimeout(draftTimerRef.current);
        };
    }, [code, language, activeProblemId, assessmentId]);

    const handleExpire = () => {
        setIsExpired(true);
    };

    const handleRun = async () => {
        if (!activeProblemId || !code.trim()) return;
        
        try {
            setIsRunning(true);
            setShowRunPanel(true);
            setActionError(null);
            setRunResult(null);
            const result = await runService.executeRun(assessmentId, activeProblemId, {
                language,
                sourceCode: code
            });
            setRunResult(result);
        } catch (err: any) {
            setActionError('Run failed: ' + (err.response?.data?.message || 'please try again.'));
            setShowRunPanel(false);
        } finally {
            setIsRunning(false);
        }
    };

    /**
     * Submits the current editor contents and hands the candidate straight to
     * the live result page, which subscribes to the existing submission SSE
     * stream to show QUEUED -> RUNNING -> EVALUATING -> COMPLETED.
     */
    const handleSubmit = async () => {
        if (!activeProblemId || !code.trim()) return;

        try {
            setIsSubmitting(true);
            setActionError(null);

            const submission = await submissionService.submitCode(
                activeProblemId,
                language,
                code
            );

            navigate(`/submissions/${submission.id}/result`);
        } catch (err: any) {
            setActionError(
                err.response?.data?.message
                || 'Submission failed. Please check your connection and try again.'
            );
        } finally {
            setIsSubmitting(false);
        }
    };

    if (loading) return <div className="p-8 text-center text-text-secondary">Loading assessment workspace...</div>;
    if (error) return <div className="p-8 text-center text-danger">{error}</div>;
    if (!assessment) return <div className="p-8 text-center text-text-secondary">Assessment not found.</div>;

    const activeProblem = problems.find(p => p.id === activeProblemId) || null;

    const handleLanguageChange = (newLang: string) => {
        setLanguage(newLang);
    };

    const handleInsertTemplate = () => {
        const template = getStarterTemplate(activeProblem, language);
        setCode(template);
        if (editorRef.current) {
            editorRef.current.setValue(template);
        }
        if (activeProblemId) {
            draftService.saveLocalDraft(assessmentId, activeProblemId, language, template);
            draftService.saveDraft(assessmentId, activeProblemId, { language, sourceCode: template })
                .catch(() => {});
        }
    };

    const handleClear = () => {
        setCode('');
        if (editorRef.current) {
            editorRef.current.setValue('');
        }
        if (activeProblemId) {
            draftService.clearLocalDraft(assessmentId, activeProblemId);
            draftService.saveDraft(assessmentId, activeProblemId, { language, sourceCode: '' })
                .catch(() => {});
        }
    };

    return (
        <div className="assessment-workspace" style={{ display: 'flex', flexDirection: 'column', height: '100vh', overflow: 'hidden' }}>
            <AssessmentHeader 
                title={assessment.title} 
                assessment={assessment}
                attempt={attempt}
                onExpire={handleExpire} 
                onExit={onExit} 
            />
            
            <div className="assessment-workspace-container">
                <ProblemNavigator 
                    problems={problems as any} 
                    activeProblemId={activeProblemId} 
                    onSelectProblem={setActiveProblemId} 
                    disabled={isExpired}
                />
                
                <div className="assessment-left-panel">
                    <div className="workspace-tabs" role="tablist">
                        <button 
                            role="tab"
                            aria-selected={activeTab === 'problem'}
                            className={`workspace-tab ${activeTab === 'problem' ? 'active' : ''}`}
                            onClick={() => setActiveTab('problem')}
                        >
                            Problem
                        </button>
                        <button 
                            role="tab"
                            aria-selected={activeTab === 'instructions'}
                            className={`workspace-tab ${activeTab === 'instructions' ? 'active' : ''}`}
                            onClick={() => setActiveTab('instructions')}
                        >
                            Instructions
                        </button>
                        <button 
                            role="tab"
                            aria-selected={activeTab === 'submissions'}
                            className={`workspace-tab ${activeTab === 'submissions' ? 'active' : ''}`}
                            onClick={() => setActiveTab('submissions')}
                        >
                            Submissions
                        </button>
                    </div>
                    
                    <div style={{ flex: 1, overflowY: 'auto' }}>
                        {activeTab === 'problem' && <ProblemPanel problem={activeProblem} />}
                        {activeTab === 'instructions' && (
                            <div className="assessment-instructions">
                                <h3>Assessment Instructions</h3>
                                <div style={{ whiteSpace: 'pre-wrap' }}>{assessment.instructions || 'No specific instructions provided for this assessment.'}</div>
                            </div>
                        )}
                        {activeTab === 'submissions' && (
                            <SubmissionHistory submissions={submissions} onSelect={(sub) => {
                                if(sub.sourceCode && !isExpired && !isRunning && !isSubmitting) {
                                    if(window.confirm('Load this submission into the editor? Your current draft will be overwritten.')) {
                                        setLanguage(sub.language);
                                        setCode(sub.sourceCode);
                                        if (editorRef.current) {
                                            editorRef.current.setValue(sub.sourceCode);
                                        }
                                    }
                                }
                            }} />
                        )}
                    </div>
                </div>

                <div className="assessment-right-panel">
                    {isExpired && (
                        <div className="assessment-expired-overlay">
                            <h2 style={{ color: 'var(--danger)' }}>Time's Up!</h2>
                            <p>This assessment has expired. You can no longer make submissions.</p>
                            <button 
                                onClick={onExit}
                                className="btn btn-primary"
                                style={{ marginTop: '16px' }}
                            >
                                Return to Dashboard
                            </button>
                        </div>
                    )}

                    <div className="assessment-toolbar">
                        <div className="assessment-toolbar-left" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                            <LanguageSelector value={language} onChange={handleLanguageChange} disabled={isExpired || isRunning || isSubmitting} />
                            <button
                                id="starter-template-btn"
                                type="button"
                                onClick={handleInsertTemplate}
                                disabled={isExpired || isRunning || isSubmitting}
                                className="btn btn-secondary"
                                style={{ fontSize: '12px', padding: '6px 12px' }}
                                title={`Load starter template for ${language}`}
                            >
                                Starter Template
                            </button>
                            <button
                                id="clear-code-btn"
                                type="button"
                                onClick={handleClear}
                                disabled={isExpired || isRunning || isSubmitting}
                                className="btn btn-secondary"
                                style={{ fontSize: '12px', padding: '6px 12px' }}
                                title="Clear editor"
                            >
                                Clear
                            </button>
                        </div>
                        <div className="assessment-toolbar-right">
                            {warningMessage && (
                                <span style={{ color: 'var(--warning)', fontSize: '14px', fontWeight: 'bold' }}>{warningMessage}</span>
                            )}
                            <button 
                                onClick={handleRun} 
                                disabled={isExpired || isRunning || isSubmitting}
                                className="btn btn-secondary"
                                style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                            >
                                {isRunning && <Loader2 size={16} className="animate-spin" />}
                                {isRunning ? 'Running...' : 'Run Code'}
                            </button>
                            <button 
                                onClick={handleSubmit} 
                                disabled={isExpired || isRunning || isSubmitting}
                                className="btn btn-primary"
                                style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                            >
                                {isSubmitting && <Loader2 size={16} className="animate-spin" />}
                                {isSubmitting ? 'Submitting...' : 'Submit'}
                            </button>
                        </div>
                    </div>

                    {actionError && (
                        <div
                            role="alert"
                            style={{
                                padding: '10px 16px',
                                background: 'rgba(239, 68, 68, 0.12)',
                                borderBottom: '1px solid var(--danger)',
                                color: 'var(--danger)',
                                fontSize: '14px',
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center',
                                gap: '12px'
                            }}
                        >
                            <span>{actionError}</span>
                            <button
                                onClick={() => setActionError(null)}
                                aria-label="Dismiss error"
                                style={{
                                    background: 'none',
                                    border: 'none',
                                    color: 'inherit',
                                    cursor: 'pointer',
                                    fontSize: '16px'
                                }}
                            >
                                ×
                            </button>
                        </div>
                    )}

                    <div style={{ flex: 1, position: 'relative' }}>
                        <CodeEditor
                            language={language}
                            value={code}
                            onChange={(val) => setCode(val || '')}
                            onMount={(editor) => { editorRef.current = editor; }}
                            readOnly={isExpired || isRunning || isSubmitting}
                        />

                        {showRunPanel && (
                            <RunResultPanel 
                                result={runResult} 
                                loading={isRunning} 
                                onClose={() => setShowRunPanel(false)} 
                            />
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AssessmentWorkspacePage;
