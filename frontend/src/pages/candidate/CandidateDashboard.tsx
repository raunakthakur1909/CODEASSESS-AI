import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Play, 
  CheckCircle2, 
  Clock, 
  Target, 
  Trophy,
  Activity,
  Code2
} from 'lucide-react';
import { assessmentService, type Assessment } from '../../services/assessmentService';
import { analyticsService } from '../../services/analyticsService';
import type { CandidateAnalytics } from '../../types/analytics';
import { authService } from '../../services/authService';

export default function CandidateDashboard() {
  const navigate = useNavigate();
  const user = authService.getCurrentUser();
  const [assessments, setAssessments] = useState<Assessment[]>([]);
  const [analytics, setAnalytics] = useState<CandidateAnalytics | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [assessmentsData, analyticsData] = await Promise.all([
          assessmentService.getAvailableAssessments(),
          analyticsService.getMyCandidateAnalytics().catch(() => null)
        ]);
        
        setAssessments(assessmentsData);
        if (analyticsData) setAnalytics(analyticsData);
      } catch (error) {
        console.error('Failed to fetch dashboard data:', error);
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
          <Activity size={32} />
        </div>
      </div>
    );
  }

  // Fallback metrics if analytics fails or is empty
  const activeCount = analytics?.assessmentsAttempted || 0;
  const completedCount = analytics?.acceptedCount || 0;
  const avgScore = analytics?.averageScore ? Math.round(analytics.averageScore) : 0;

  return (
    <div className="p-6 md:p-8 max-w-7xl mx-auto space-y-8">
      {/* Welcome Section */}
      <div>
        <h1 className="text-2xl md:text-3xl font-bold text-text-primary">
          Welcome back, {user?.email?.split('@')[0] || 'Candidate'} 👋
        </h1>
        <p className="text-text-secondary mt-1">Continue your coding journey</p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="stat-card">
          <div className="flex items-center gap-3 mb-2">
            <div className="p-2 bg-warning-bg text-warning-text rounded-md">
              <Clock size={20} />
            </div>
            <span className="stat-title">Active Assessments</span>
          </div>
          <span className="stat-value">{activeCount}</span>
        </div>
        
        <div className="stat-card">
          <div className="flex items-center gap-3 mb-2">
            <div className="p-2 bg-success-bg text-success-text rounded-md">
              <CheckCircle2 size={20} />
            </div>
            <span className="stat-title">Completed</span>
          </div>
          <span className="stat-value">{completedCount}</span>
        </div>

        <div className="stat-card">
          <div className="flex items-center gap-3 mb-2">
            <div className="p-2 bg-primary-light text-primary rounded-md">
              <Target size={20} />
            </div>
            <span className="stat-title">Average Score</span>
          </div>
          <div className="flex items-baseline gap-1">
            <span className="stat-value">{avgScore}</span>
            <span className="text-text-secondary font-medium">%</span>
          </div>
        </div>
      </div>

      {/* Available Assessments */}
      <div>
        <h2 className="text-xl font-bold mb-4 flex items-center gap-2">
          <Code2 size={20} className="text-primary" />
          Available Assessments
        </h2>
        
        {assessments.length === 0 ? (
          <div className="card p-8 text-center text-text-secondary">
            <p>No assessments available at the moment.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {assessments.map(assessment => (
              <div key={assessment.id} className="card flex flex-col hover:border-primary/50 transition-colors">
                <div className="card-header">
                  <div>
                    <h3 className="card-title">{assessment.title}</h3>
                    <p className="card-description line-clamp-2 mt-2">{assessment.description}</p>
                  </div>
                </div>
                <div className="card-content flex-1">
                  <div className="flex flex-wrap gap-2 mt-4">
                    <span className="badge badge-neutral">
                      {assessment.durationMinutes} min
                    </span>
                    <span className="badge badge-neutral">
                      {assessment.totalMarks} marks
                    </span>
                  </div>
                </div>
                <div className="card-footer">
                  <button 
                    onClick={() => navigate(`/assessments/${assessment.id}`)}
                    className="btn btn-primary w-full justify-center"
                  >
                    Start Test
                    <Play size={16} />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Recent Activity (Placeholder for now, can be populated from submissions api) */}
      <div>
        <h2 className="text-xl font-bold mb-4 flex items-center gap-2">
          <Trophy size={20} className="text-primary" />
          Recent Submissions
        </h2>
        <div className="card">
          <div className="card-content p-0">
            <div className="p-8 text-center text-text-secondary">
              Real submission data will appear here.
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
