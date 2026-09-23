import React, { useState, useEffect, useCallback } from 'react';
import { assessmentService } from '../services/assessmentService';
import type { Assessment } from '../services/assessmentService';
import { authService } from '../services/authService';
import { useNavigate } from 'react-router-dom';
import { BookOpen, Clock, Target, Plus, Edit3, Trash2, BarChart3, Play, Copy } from 'lucide-react';

interface AssessmentsListPageProps {
    onNavigate?: (search: string) => void;
}

const AssessmentsListPage: React.FC<AssessmentsListPageProps> = () => {
    const navigate = useNavigate();
    const [assessments, setAssessments] = useState<Assessment[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const user = authService.getCurrentUser();
    const isAdmin = user?.role === 'ADMIN';

    const loadAssessments = useCallback(async () => {
        try {
            setLoading(true);
            const data = isAdmin ? await assessmentService.getAllAssessments() : await assessmentService.getAvailableAssessments();
            setAssessments(data);
            setError(null);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to load assessments');
        } finally {
            setLoading(false);
        }
    }, [isAdmin]);

    useEffect(() => {
        loadAssessments();
    }, [loadAssessments]);

    const handleDuplicate = async (id: number) => {
        try {
            await assessmentService.duplicateAssessment(id);
            loadAssessments();
        } catch (err: any) {
            alert(err.response?.data?.message || 'Failed to duplicate assessment');
        }
    };

    const handleDelete = async (id: number) => {
        if (window.confirm('Are you sure you want to delete this assessment?')) {
            try {
                await assessmentService.deleteAssessment(id);
                loadAssessments();
            } catch (err: any) {
                alert(err.response?.data?.message || 'Failed to delete assessment');
            }
        }
    };

    const handlePublish = async (id: number) => {
        try {
            await assessmentService.publishAssessment(id);
            loadAssessments();
        } catch (err: any) {
            alert(err.response?.data?.message || 'Failed to publish assessment');
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center h-64">
                <div className="animate-spin text-primary">
                    <Clock size={32} />
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="card p-6 border border-danger/30 bg-danger-bg">
                <p className="text-danger-text">{error}</p>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                    <h1 className="text-2xl md:text-3xl font-bold text-text-primary">
                        {isAdmin ? 'Manage Assessments' : 'Available Assessments'}
                    </h1>
                    <p className="text-text-secondary mt-1">
                        {isAdmin ? 'Create, edit, and publish coding assessments' : 'Choose an assessment to begin'}
                    </p>
                </div>
                <div className="flex gap-3">
                    {isAdmin && (
                        <button 
                            onClick={() => navigate('/admin/assessments/new')}
                            className="btn btn-primary"
                        >
                            <Plus size={18} />
                            Create Assessment
                        </button>
                    )}
                </div>
            </div>

            {assessments.length === 0 ? (
                <div className="card p-12 text-center">
                    <BookOpen size={48} className="mx-auto text-text-muted mb-4" />
                    <p className="text-text-secondary text-lg">No assessments found.</p>
                    {isAdmin && (
                        <button 
                            onClick={() => navigate('/admin/assessments/new')}
                            className="btn btn-primary mt-4"
                        >
                            <Plus size={18} />
                            Create your first assessment
                        </button>
                    )}
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
                    {assessments.map((assessment) => (
                        <div key={assessment.id} className="card flex flex-col hover:border-primary/40 transition-all duration-200">
                            <div className="card-header">
                                <div className="flex items-start justify-between gap-3">
                                    <div className="flex-1 min-w-0">
                                        <h3 className="card-title truncate">{assessment.title}</h3>
                                        <p className="card-description line-clamp-2 mt-2">{assessment.description}</p>
                                    </div>
                                    <span className={`badge ${
                                        assessment.status === 'PUBLISHED' ? 'badge-success' : 
                                        assessment.status === 'DRAFT' ? 'badge-warning' : 'badge-neutral'
                                    }`}>
                                        {assessment.status}
                                    </span>
                                </div>
                            </div>
                            
                            <div className="card-content flex-1">
                                <div className="flex flex-wrap gap-2">
                                    <span className="badge badge-neutral flex items-center gap-1.5">
                                        <Clock size={12} />
                                        {assessment.durationMinutes} min
                                    </span>
                                    <span className="badge badge-neutral flex items-center gap-1.5">
                                        <Target size={12} />
                                        {assessment.totalMarks} marks
                                    </span>
                                </div>
                            </div>

                            <div className="card-footer">
                                <div className="flex gap-2 w-full">
                                    {isAdmin ? (
                                        <>
                                            <button 
                                                onClick={() => navigate(`/admin/assessments/${assessment.id}`)}
                                                className="btn btn-ghost btn-icon flex-1 justify-center"
                                                title="Edit"
                                            >
                                                <Edit3 size={16} />
                                                Edit
                                            </button>
                                            <button 
                                                onClick={() => navigate(`/admin/analytics/${assessment.id}`)}
                                                className="btn btn-ghost btn-icon flex-1 justify-center"
                                                title="Analytics"
                                            >
                                                <BarChart3 size={16} />
                                                Analytics
                                            </button>
                                            {assessment.status === 'DRAFT' && (
                                                <>
                                                    <button 
                                                        onClick={() => handlePublish(assessment.id)}
                                                        className="btn btn-primary flex-1 justify-center"
                                                        title="Publish"
                                                    >
                                                        <Play size={16} />
                                                        Publish
                                                    </button>
                                                    <button 
                                                        onClick={() => handleDelete(assessment.id)}
                                                        className="btn btn-ghost btn-icon text-danger hover:text-danger hover:bg-danger-bg"
                                                        title="Delete"
                                                    >
                                                        <Trash2 size={16} />
                                                    </button>
                                                </>
                                            )}
                                            {assessment.status === 'PUBLISHED' && (
                                                <button 
                                                    onClick={() => handleDuplicate(assessment.id)}
                                                    className="btn btn-ghost btn-icon flex-1 justify-center"
                                                    title="Duplicate"
                                                >
                                                    <Copy size={16} />
                                                    Duplicate
                                                </button>
                                            )}
                                        </>
                                    ) : (
                                        <button 
                                            onClick={() => navigate(`/assessments/${assessment.id}`)}
                                            className="btn btn-primary w-full justify-center"
                                        >
                                            <Play size={16} />
                                            Start Assessment
                                        </button>
                                    )}
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default AssessmentsListPage;
