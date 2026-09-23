import { BrowserRouter, Routes, Route, Navigate, useParams, useNavigate } from 'react-router-dom';
import type { ReactElement } from 'react';
import { authService } from './services/authService';

// Layouts
import AppLayout from './components/layout/AppLayout';
import AdminLayout from './components/layout/AdminLayout';

// Auth Pages
import Login from './pages/Login';
import Register from './pages/Register';

// Admin Pages
import AdminDashboard from './pages/admin/AdminDashboard';
import AssessmentBuilderPage from './pages/AssessmentBuilderPage';
import AssessmentsListPage from './pages/AssessmentsListPage';
import AdminAnalyticsPage from './pages/AdminAnalyticsPage';
import AIAnalyticsPage from './pages/AIAnalyticsPage';

// Candidate Pages
import CandidateDashboard from './pages/candidate/CandidateDashboard';
import AssessmentWorkspacePage from './pages/AssessmentWorkspacePage';
import SubmissionResultPage from './pages/SubmissionResultPage';
import CandidateAnalyticsPage from './pages/CandidateAnalyticsPage';
import LeaderboardPage from './pages/LeaderboardPage';
import AIReviewPage from './pages/AIReviewPage';

// ────────────────────────────────────────────────────────────
// Protected Route wrapper
// ────────────────────────────────────────────────────────────
function ProtectedRoute({ children, requiredRole }: { children: ReactElement; requiredRole?: string }) {
  const user = authService.getCurrentUser();
  const isAuthenticated = authService.isAuthenticated();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRole && user?.role !== requiredRole) {
    return <Navigate to={user?.role === 'ADMIN' ? '/admin' : '/dashboard'} replace />;
  }

  return children;
}

// ────────────────────────────────────────────────────────────
// Router-aware wrappers (bridge URL params → callback props)
// ────────────────────────────────────────────────────────────

/** /admin/assessments/new  or  /admin/assessments/:id/edit */
function AssessmentBuilderWrapper() {
  const { id } = useParams<{ id?: string }>();
  const navigate = useNavigate();
  return (
    <AssessmentBuilderPage
      assessmentId={id ? Number(id) : undefined}
      onNavigate={(path) => navigate(path)}
    />
  );
}

/** /admin/analytics/:id  or  /admin/analytics?assessmentId=N */
function AdminAnalyticsWrapper() {
  const { id } = useParams<{ id?: string }>();
  const navigate = useNavigate();
  const params = new URLSearchParams(window.location.search);
  const assessmentId = id ? Number(id) : Number(params.get('assessmentId') ?? '0');
  return (
    <AdminAnalyticsPage
      assessmentId={assessmentId}
      onBack={() => navigate(-1)}
    />
  );
}

/** /admin/analytics/ai/:id  or  /admin/analytics/ai?assessmentId=N */
function AIAnalyticsWrapper() {
  const { id } = useParams<{ id?: string }>();
  const navigate = useNavigate();
  const params = new URLSearchParams(window.location.search);
  const assessmentId = id ? Number(id) : Number(params.get('assessmentId') ?? '0');
  return (
    <AIAnalyticsPage
      assessmentId={assessmentId}
      onBack={() => navigate(-1)}
    />
  );
}

/** /analytics  (own analytics, candidateId = 'me') */
function CandidateAnalyticsWrapper() {
  const navigate = useNavigate();
  return (
    <CandidateAnalyticsPage
      candidateId="me"
      onBack={() => navigate(-1)}
    />
  );
}

/** /leaderboard?assessmentId=N */
function LeaderboardWrapper() {
  const navigate = useNavigate();
  const params = new URLSearchParams(window.location.search);
  const assessmentId = Number(params.get('assessmentId') ?? '0');
  return (
    <LeaderboardPage
      assessmentId={assessmentId}
      onBack={() => navigate(-1)}
    />
  );
}

/** /assessments/:id/attempt */
function AssessmentWorkspaceWrapper() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  return (
    <AssessmentWorkspacePage
      assessmentId={Number(id)}
      onExit={() => navigate('/dashboard')}
    />
  );
}

/** /submissions/:id */
function SubmissionResultWrapper() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  return (
    <SubmissionResultPage
      submissionId={Number(id)}
      onBack={() => navigate('/dashboard')}
    />
  );
}

/** /submissions/:id/ai-review */
function AIReviewWrapper() {
  const { id } = useParams<{ id: string }>();
  return <AIReviewPage submissionId={Number(id)} />;
}

// ────────────────────────────────────────────────────────────
// App / Router
// ────────────────────────────────────────────────────────────
function App() {
  const user = authService.getCurrentUser();
  const isAuthenticated = authService.isAuthenticated();
  const defaultDash = user?.role === 'ADMIN' ? '/admin' : '/dashboard';

  return (
    <BrowserRouter>
      <Routes>
        {/* Public */}
        <Route path="/login" element={isAuthenticated ? <Navigate to={defaultDash} replace /> : <Login />} />
        <Route path="/register" element={isAuthenticated ? <Navigate to={defaultDash} replace /> : <Register />} />

        {/* Admin */}
        <Route path="/admin" element={<ProtectedRoute requiredRole="ADMIN"><AdminLayout /></ProtectedRoute>}>
          <Route index element={<AdminDashboard />} />
          <Route path="assessments" element={<AssessmentsListPage />} />
          <Route path="assessments/new" element={<AssessmentBuilderWrapper />} />
          <Route path="assessments/:id/edit" element={<AssessmentBuilderWrapper />} />
          <Route path="analytics" element={<AdminAnalyticsWrapper />} />
          <Route path="analytics/:id" element={<AdminAnalyticsWrapper />} />
          <Route path="analytics/ai" element={<AIAnalyticsWrapper />} />
          <Route path="analytics/ai/:id" element={<AIAnalyticsWrapper />} />
        </Route>

        {/* Candidate – with sidebar layout */}
        <Route path="/dashboard" element={<ProtectedRoute><AppLayout /></ProtectedRoute>}>
          <Route index element={<CandidateDashboard />} />
        </Route>

        <Route path="/assessments" element={<ProtectedRoute><AppLayout /></ProtectedRoute>}>
          <Route index element={<AssessmentsListPage />} />
        </Route>

        <Route path="/analytics" element={<ProtectedRoute><AppLayout /></ProtectedRoute>}>
          <Route index element={<CandidateAnalyticsWrapper />} />
        </Route>

        <Route path="/leaderboard" element={<ProtectedRoute><AppLayout /></ProtectedRoute>}>
          <Route index element={<LeaderboardWrapper />} />
        </Route>

        {/* Full-screen pages (no layout wrapper) */}
        <Route
          path="/assessments/:id/attempt"
          element={<ProtectedRoute><AssessmentWorkspaceWrapper /></ProtectedRoute>}
        />
        <Route
          path="/submissions/:id"
          element={<ProtectedRoute><SubmissionResultWrapper /></ProtectedRoute>}
        />
        <Route
          path="/submissions/:id/ai-review"
          element={<ProtectedRoute><AIReviewWrapper /></ProtectedRoute>}
        />

        {/* Root & catch-all */}
        <Route path="/" element={<Navigate to={isAuthenticated ? defaultDash : '/login'} replace />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
