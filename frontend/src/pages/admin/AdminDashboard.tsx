import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
    BookOpen, 
    Plus,
    Settings
} from 'lucide-react';
import { assessmentService, type Assessment } from '../../services/assessmentService';

export default function AdminDashboard() {
    const navigate = useNavigate();
    const [assessments, setAssessments] = useState<Assessment[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const assessmentsData = await assessmentService.getAllAssessments();
                setAssessments(assessmentsData);
            } catch (error) {
                console.error('Failed to fetch admin dashboard data:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    if (loading) {
        return (
            <div className="p-8 flex justify-center items-center h-full">
                <div className="animate-spin text-primary">
                    <Settings size={32} />
                </div>
            </div>
        );
    }

    const publishedCount = assessments.filter(a => a.status === 'PUBLISHED').length;
    const draftCount = assessments.filter(a => a.status === 'DRAFT').length;

    return (
        <div className="p-6 md:p-8 max-w-7xl mx-auto space-y-8">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                    <h1 className="text-2xl md:text-3xl font-bold text-text-primary">
                        Admin Dashboard
                    </h1>
                    <p className="text-text-secondary mt-1">Manage coding assessments and review results</p>
                </div>
                <button 
                    onClick={() => navigate('/admin/assessments/new')}
                    className="btn btn-primary"
                >
                    <Plus size={18} />
                    Create Assessment
                </button>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
                <div className="stat-card">
                    <div className="flex items-center gap-3 mb-2">
                        <div className="p-2 bg-primary-light text-primary rounded-md">
                            <BookOpen size={20} />
                        </div>
                        <span className="stat-title">Total Assessments</span>
                    </div>
                    <div className="flex items-baseline gap-2">
                        <span className="stat-value">{assessments.length}</span>
                    </div>
                    <p className="text-sm text-text-secondary mt-2">
                        {publishedCount} published, {draftCount} drafts
                    </p>
                </div>
                
                <div className="stat-card">
                    <div className="flex items-center gap-3 mb-2">
                        <div className="p-2 bg-success-bg text-success-text rounded-md">
                            <Settings size={20} />
                        </div>
                        <span className="stat-title">Quick Actions</span>
                    </div>
                    <div className="flex flex-col gap-2 mt-2">
                        <button 
                            onClick={() => navigate('/admin/assessments/new')}
                            className="btn btn-secondary text-sm justify-start"
                        >
                            <Plus size={16} />
                            New Assessment
                        </button>
                        <button 
                            onClick={() => navigate('/admin/assessments')}
                            className="btn btn-secondary text-sm justify-start"
                        >
                            <BookOpen size={16} />
                            View All
                        </button>
                    </div>
                </div>

                <div className="stat-card">
                    <div className="flex items-center gap-3 mb-2">
                        <div className="p-2 bg-warning-bg text-warning-text rounded-md">
                            <Settings size={20} />
                        </div>
                        <span className="stat-title">System Status</span>
                    </div>
                    <div className="space-y-2 mt-2">
                        <div className="flex items-center justify-between text-sm">
                            <span className="text-text-secondary">Platform</span>
                            <span className="badge badge-success">Online</span>
                        </div>
                        <div className="flex items-center justify-between text-sm">
                            <span className="text-text-secondary">Judge0</span>
                            <span className="badge badge-success">Connected</span>
                        </div>
                        <div className="flex items-center justify-between text-sm">
                            <span className="text-text-secondary">AI Review</span>
                            <span className="badge badge-warning">On Demand</span>
                        </div>
                    </div>
                </div>
            </div>

            <div>
                <div className="flex items-center justify-between mb-4">
                    <h2 className="text-xl font-bold flex items-center gap-2">
                        <Settings size={20} className="text-text-primary" />
                        Recent Assessments
                    </h2>
                    <button 
                        onClick={() => navigate('/admin/assessments')}
                        className="text-sm font-medium text-primary hover:text-primary-hover"
                    >
                        View all
                    </button>
                </div>
                
                <div className="card table-container">
                    {assessments.length === 0 ? (
                        <div className="p-8 text-center text-text-secondary">
                            <p>No assessments found. Create your first one!</p>
                        </div>
                    ) : (
                        <table className="table w-full">
                            <thead>
                                <tr>
                                    <th>Title</th>
                                    <th>Status</th>
                                    <th>Duration</th>
                                    <th>Marks</th>
                                    <th className="text-right">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {assessments.slice(0, 5).map(assessment => (
                                    <tr key={assessment.id}>
                                        <td className="font-medium">{assessment.title}</td>
                                        <td>
                                            <span className={`badge ${
                                                assessment.status === 'PUBLISHED' ? 'badge-success' : 
                                                assessment.status === 'DRAFT' ? 'badge-neutral' : 'badge-warning'
                                            }`}>
                                                {assessment.status}
                                            </span>
                                        </td>
                                        <td>{assessment.durationMinutes} min</td>
                                        <td>{assessment.totalMarks}</td>
                                        <td className="text-right">
                                            <button 
                                                onClick={() => navigate(`/admin/assessments/${assessment.id}`)}
                                                className="btn btn-ghost btn-icon"
                                                title="Manage"
                                            >
                                                <Settings size={18} />
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </div>
            </div>
        </div>
    );
}
