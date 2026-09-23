import React, { useState, useEffect } from 'react';
import { assessmentService } from '../services/assessmentService';
import { problemService } from '../services/problemService';
import type { Problem, TestCase } from '../services/problemService';
import { 
  Settings, 
  FileCode2, 
  Plus, 
  Trash2, 
  Save, 
  Eye, 
  EyeOff, 
  Play
} from 'lucide-react';

interface AssessmentBuilderPageProps {
    assessmentId?: number;
    onNavigate: (path: string) => void;
}

const AssessmentBuilderPage: React.FC<AssessmentBuilderPageProps> = ({ assessmentId, onNavigate }) => {
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    
    // Assessment State
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [instructions, setInstructions] = useState('');
    const [durationMinutes, setDurationMinutes] = useState(60);
    const [status, setStatus] = useState('DRAFT');

    // Problems State
    const [problems, setProblems] = useState<Problem[]>([]);
    
    // UI State
    const [activeTab, setActiveTab] = useState<'settings' | number>('settings');
    const [testCasesMap, setTestCasesMap] = useState<Record<number, TestCase[]>>({});

    useEffect(() => {
        if (assessmentId) {
            loadAssessmentData();
        }
    }, [assessmentId]);

    const loadAssessmentData = async () => {
        try {
            setLoading(true);
            const data = await assessmentService.getAssessment(assessmentId!);
            setTitle(data.title);
            setDescription(data.description);
            setInstructions(data.instructions || '');
            setDurationMinutes(data.durationMinutes);
            setStatus(data.status);
            
            const problemsData = await problemService.getProblems(assessmentId!);
            setProblems(problemsData);
            
            // Load test cases for all problems
            const tcMap: Record<number, TestCase[]> = {};
            for (const p of problemsData) {
                tcMap[p.id] = await problemService.getTestCases(p.id);
            }
            setTestCasesMap(tcMap);
        } catch (err: any) {
            alert('Failed to load assessment: ' + (err.response?.data?.message || err.message));
        } finally {
            setLoading(false);
        }
    };

    const handleSaveAssessment = async () => {
        try {
            setSaving(true);
            const reqData = { title, description, instructions, durationMinutes };
            if (assessmentId) {
                await assessmentService.updateAssessment(assessmentId, reqData);
            } else {
                const newAss = await assessmentService.createAssessment(reqData);
                onNavigate(`/admin/assessments/${newAss.id}`);
            }
        } catch (err: any) {
            alert('Failed to save assessment: ' + (err.response?.data?.message || err.message));
        } finally {
            setSaving(false);
        }
    };

    const handlePublish = async () => {
        if (!assessmentId) return;
        try {
            if (!confirm('Are you sure you want to publish this assessment? Candidates will be able to see it.')) return;
            setSaving(true);
            await assessmentService.publishAssessment(assessmentId);
            setStatus('PUBLISHED');
            alert('Assessment published successfully!');
        } catch (err: any) {
            alert('Failed to publish: ' + (err.response?.data?.message || err.message));
        } finally {
            setSaving(false);
        }
    };

    const handleAddProblem = async () => {
        if (!assessmentId) {
            alert('Please save the assessment settings first.');
            return;
        }
        try {
            const newProb = await problemService.createProblem(assessmentId, {
                title: 'New Problem',
                description: 'Problem description here...',
                difficulty: 'EASY',
                points: 10
            });
            setProblems([...problems, newProb]);
            setTestCasesMap({ ...testCasesMap, [newProb.id]: [] });
            setActiveTab(newProb.id);
        } catch (err: any) {
            alert('Failed to create problem: ' + err.message);
        }
    };

    const handleUpdateProblem = async (problemId: number, data: Partial<Problem>) => {
        try {
            const p = problems.find(x => x.id === problemId)!;
            const updated = await problemService.updateProblem(problemId, {
                title: data.title ?? p.title,
                description: data.description ?? p.description,
                difficulty: data.difficulty ?? p.difficulty,
                points: data.points ?? p.points,
                inputFormat: data.inputFormat ?? p.inputFormat,
                outputFormat: data.outputFormat ?? p.outputFormat,
                constraints: data.constraints ?? p.constraints,
            });
            setProblems(problems.map(x => x.id === problemId ? updated : x));
        } catch (err: any) {
            alert('Failed to update problem: ' + err.message);
        }
    };

    const handleDeleteProblem = async (problemId: number) => {
        if (!confirm('Delete this problem?')) return;
        try {
            await problemService.deleteProblem(problemId);
            setProblems(problems.filter(p => p.id !== problemId));
            if (activeTab === problemId) setActiveTab('settings');
        } catch (err: any) {
            alert('Failed to delete problem: ' + err.message);
        }
    };

    const handleAddTestCase = async (problemId: number) => {
        try {
            const tc = await problemService.createTestCase(problemId, {
                input: 'Sample Input',
                expectedOutput: 'Expected Output',
                hidden: false,
                timeLimitMs: 2000,
                memoryLimitMb: 256
            });
            setTestCasesMap(prev => ({
                ...prev,
                [problemId]: [...(prev[problemId] || []), tc]
            }));
        } catch (err: any) {
            alert('Failed to add test case: ' + err.message);
        }
    };

    const handleDeleteTestCase = async (problemId: number, tcId: number) => {
        if (!confirm('Delete this test case?')) return;
        try {
            await problemService.deleteTestCase(tcId);
            setTestCasesMap(prev => ({
                ...prev,
                [problemId]: prev[problemId].filter(t => t.id !== tcId)
            }));
        } catch (err: any) {
            alert('Failed to delete test case: ' + err.message);
        }
    };

    if (loading) return <div className="p-8 text-center text-text-secondary">Loading...</div>;

    const activeProblem = typeof activeTab === 'number' ? problems.find(p => p.id === activeTab) : null;
    const isPublished = status === 'PUBLISHED';

    return (
        <div className="flex h-full gap-6">
            {/* Left Sidebar Navigation */}
            <div className="w-64 flex flex-col gap-4">
                <div className="card p-4">
                    <button 
                        onClick={() => setActiveTab('settings')}
                        className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-md text-sm font-medium transition-colors ${
                            activeTab === 'settings' 
                                ? 'bg-primary-light text-primary' 
                                : 'text-text-secondary hover:bg-white/5 hover:text-white'
                        }`}
                        style={{
                            backgroundColor: activeTab === 'settings' ? 'var(--primary-light)' : 'transparent',
                            color: activeTab === 'settings' ? 'var(--primary)' : 'var(--text-secondary)'
                        }}
                    >
                        <Settings size={18} />
                        Assessment Settings
                    </button>
                    
                    <div className="mt-6 mb-2 px-3 text-xs font-bold text-text-muted uppercase tracking-wider">
                        Problems
                    </div>
                    
                    <div className="flex flex-col gap-1">
                        {problems.map((p, idx) => (
                            <button 
                                key={p.id}
                                onClick={() => setActiveTab(p.id)}
                                className="w-full flex items-center gap-3 px-3 py-2.5 rounded-md text-sm font-medium transition-colors"
                                style={{
                                    backgroundColor: activeTab === p.id ? 'var(--primary-light)' : 'transparent',
                                    color: activeTab === p.id ? 'var(--primary)' : 'var(--text-secondary)'
                                }}
                            >
                                <FileCode2 size={18} />
                                <span className="truncate flex-1 text-left">{idx + 1}. {p.title}</span>
                            </button>
                        ))}
                    </div>

                    {!isPublished && (
                        <button 
                            onClick={handleAddProblem}
                            className="mt-4 w-full flex items-center justify-center gap-2 px-3 py-2 rounded-md text-sm font-medium text-primary hover:bg-primary-light transition-colors border border-dashed border-primary"
                        >
                            <Plus size={16} />
                            Add Problem
                        </button>
                    )}
                </div>
            </div>

            {/* Main Content Area */}
            <div className="flex-1 flex flex-col min-h-0">
                {activeTab === 'settings' && (
                    <div className="card flex-1 overflow-y-auto">
                        <div className="card-header border-b border-border-light pb-4 mb-4">
                            <div>
                                <h2 className="text-xl font-bold text-white">Assessment Settings</h2>
                                <p className="text-text-secondary text-sm mt-1">Configure the general details of the assessment.</p>
                            </div>
                            <div className="flex items-center gap-3">
                                {status === 'DRAFT' && (
                                    <span className="badge badge-warning">Draft</span>
                                )}
                                {status === 'PUBLISHED' && (
                                    <span className="badge badge-success">Published</span>
                                )}
                                {!isPublished && (
                                    <>
                                        <button onClick={handleSaveAssessment} disabled={saving} className="btn btn-secondary">
                                            <Save size={16} /> {saving ? 'Saving...' : 'Save Draft'}
                                        </button>
                                        <button onClick={handlePublish} disabled={saving} className="btn btn-primary">
                                            <Play size={16} /> Publish
                                        </button>
                                    </>
                                )}
                            </div>
                        </div>
                        
                        <div className="card-content max-w-3xl flex flex-col gap-6">
                            <div className="form-group">
                                <label className="form-label">Assessment Title</label>
                                <input 
                                    className="form-input" 
                                    value={title} 
                                    onChange={e => setTitle(e.target.value)} 
                                    placeholder="e.g. Senior Java Backend Developer"
                                    disabled={isPublished}
                                />
                            </div>
                            <div className="form-group">
                                <label className="form-label">Internal Description</label>
                                <textarea 
                                    className="form-input" 
                                    value={description} 
                                    onChange={e => setDescription(e.target.value)} 
                                    rows={3}
                                    placeholder="Private notes for admins..."
                                    disabled={isPublished}
                                />
                            </div>
                            <div className="form-group">
                                <label className="form-label">Candidate Instructions</label>
                                <textarea 
                                    className="form-input" 
                                    value={instructions} 
                                    onChange={e => setInstructions(e.target.value)} 
                                    rows={5}
                                    placeholder="Instructions shown to candidates before they start..."
                                    disabled={isPublished}
                                />
                            </div>
                            <div className="form-group">
                                <label className="form-label">Time Limit (Minutes)</label>
                                <input 
                                    type="number"
                                    className="form-input w-32" 
                                    value={durationMinutes} 
                                    onChange={e => setDurationMinutes(Number(e.target.value))} 
                                    min={5}
                                    disabled={isPublished}
                                />
                            </div>
                        </div>
                    </div>
                )}

                {activeProblem && (
                    <div className="card flex-1 overflow-y-auto">
                        <div className="card-header border-b border-border-light pb-4 mb-4">
                            <div>
                                <h2 className="text-xl font-bold text-white">Edit Problem: {activeProblem.title}</h2>
                            </div>
                            {!isPublished && (
                                <button onClick={() => handleDeleteProblem(activeProblem.id)} className="btn btn-ghost text-danger hover:text-danger hover:bg-danger-bg border border-danger/20">
                                    <Trash2 size={16} /> Delete
                                </button>
                            )}
                        </div>
                        
                        <div className="card-content grid grid-cols-2 gap-8">
                            <div className="flex flex-col gap-4">
                                <h3 className="font-semibold text-white border-b border-border-light pb-2">Problem Details</h3>
                                <div className="form-group mb-0">
                                    <label className="form-label">Title</label>
                                    <input className="form-input" value={activeProblem.title} onChange={e => handleUpdateProblem(activeProblem.id, { title: e.target.value })} disabled={isPublished} />
                                </div>
                                <div className="grid grid-cols-2 gap-4">
                                    <div className="form-group mb-0">
                                        <label className="form-label">Difficulty</label>
                                        <select className="form-input" value={activeProblem.difficulty} onChange={e => handleUpdateProblem(activeProblem.id, { difficulty: e.target.value as any })} disabled={isPublished}>
                                            <option value="EASY">Easy</option>
                                            <option value="MEDIUM">Medium</option>
                                            <option value="HARD">Hard</option>
                                        </select>
                                    </div>
                                    <div className="form-group mb-0">
                                        <label className="form-label">Points</label>
                                        <input type="number" className="form-input" value={activeProblem.points} onChange={e => handleUpdateProblem(activeProblem.id, { points: Number(e.target.value) })} disabled={isPublished} />
                                    </div>
                                </div>
                                <div className="form-group mb-0">
                                    <label className="form-label">Description</label>
                                    <textarea className="form-input" rows={4} value={activeProblem.description} onChange={e => handleUpdateProblem(activeProblem.id, { description: e.target.value })} disabled={isPublished} />
                                </div>
                                <div className="form-group mb-0">
                                    <label className="form-label">Input Format</label>
                                    <input className="form-input" value={activeProblem.inputFormat || ''} onChange={e => handleUpdateProblem(activeProblem.id, { inputFormat: e.target.value })} disabled={isPublished} />
                                </div>
                                <div className="form-group mb-0">
                                    <label className="form-label">Output Format</label>
                                    <input className="form-input" value={activeProblem.outputFormat || ''} onChange={e => handleUpdateProblem(activeProblem.id, { outputFormat: e.target.value })} disabled={isPublished} />
                                </div>
                                <div className="form-group mb-0">
                                    <label className="form-label">Constraints</label>
                                    <input className="form-input" value={activeProblem.constraints || ''} onChange={e => handleUpdateProblem(activeProblem.id, { constraints: e.target.value })} disabled={isPublished} />
                                </div>
                            </div>
                            
                            <div className="flex flex-col gap-4">
                                <div className="flex items-center justify-between border-b border-border-light pb-2">
                                    <h3 className="font-semibold text-white">Test Cases</h3>
                                    {!isPublished && (
                                        <button onClick={() => handleAddTestCase(activeProblem.id)} className="text-primary hover:text-primary-hover text-sm font-medium flex items-center gap-1 bg-transparent border-none cursor-pointer">
                                            <Plus size={14} /> Add Test Case
                                        </button>
                                    )}
                                </div>
                                
                                <div className="flex flex-col gap-4">
                                    {(testCasesMap[activeProblem.id] || []).map((tc, idx) => (
                                        <div key={tc.id} className="p-4 rounded-md border border-border-light bg-black/20">
                                            <div className="flex items-center justify-between mb-3">
                                                <span className="text-sm font-bold text-text-secondary">Test Case {idx + 1}</span>
                                                <div className="flex items-center gap-2">
                                                    {tc.hidden ? (
                                                        <span className="badge badge-warning flex items-center gap-1"><EyeOff size={12}/> Hidden</span>
                                                    ) : (
                                                        <span className="badge badge-success flex items-center gap-1"><Eye size={12}/> Public</span>
                                                    )}
                                                    {!isPublished && (
                                                        <button onClick={() => handleDeleteTestCase(activeProblem.id, tc.id)} className="text-danger hover:text-red-400 bg-transparent border-none cursor-pointer p-1">
                                                            <Trash2 size={14} />
                                                        </button>
                                                    )}
                                                </div>
                                            </div>
                                            <div className="grid grid-cols-2 gap-4">
                                                <div>
                                                    <label className="text-xs text-text-muted mb-1 block">Input</label>
                                                    <textarea className="form-input font-mono text-sm" rows={2} readOnly={isPublished} defaultValue={tc.input} />
                                                </div>
                                                <div>
                                                    <label className="text-xs text-text-muted mb-1 block">Expected Output</label>
                                                    <textarea className="form-input font-mono text-sm" rows={2} readOnly={isPublished} defaultValue={tc.expectedOutput} />
                                                </div>
                                            </div>
                                            <div className="text-xs text-text-muted mt-3 text-right italic">
                                                Note: Editing test cases inline coming in next version. Delete and recreate for now.
                                            </div>
                                        </div>
                                    ))}
                                    {(testCasesMap[activeProblem.id] || []).length === 0 && (
                                        <div className="text-center p-6 text-text-muted border border-dashed border-border-light rounded-md">
                                            No test cases added yet.
                                        </div>
                                    )}
                                </div>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default AssessmentBuilderPage;
