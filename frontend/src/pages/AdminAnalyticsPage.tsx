import { useState, useEffect, useCallback } from 'react';
import { analyticsService } from '../services/analyticsService';
import type { AssessmentAnalytics } from '../types/analytics';

import AnalyticsHeader from '../components/analytics/AnalyticsHeader';
import AnalyticsKpiGrid from '../components/analytics/AnalyticsKpiGrid';
import SubmissionMetrics from '../components/analytics/SubmissionMetrics';
import ProblemPerformanceTable from '../components/analytics/ProblemPerformanceTable';
import AnalyticsEmptyState from '../components/analytics/AnalyticsEmptyState';

import '../components/analytics/analytics.css';

interface AdminAnalyticsPageProps {
  assessmentId: number;
  onBack: () => void;
}

type LoadState = 'loading' | 'success' | 'error';

/**
 * Admin Analytics Dashboard — Milestone 9.6
 *
 * Entry: /?analytics=assessment&assessmentId={n}
 *
 * Responsibilities (this page):
 *   – fetch AssessmentAnalytics once on mount / on retry
 *   – manage loading / error / success states
 *   – pass data down to pure child components
 *   – own the "back" navigation
 *
 * No formatting or business calculations live here.
 */
function AdminAnalyticsPage({ assessmentId, onBack }: AdminAnalyticsPageProps) {
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [data, setData] = useState<AssessmentAnalytics | null>(null);
  const [errorMessage, setErrorMessage] = useState('');
  const [retryKey, setRetryKey] = useState(0);

  useEffect(() => {
    let cancelled = false;

    async function fetchData() {
      try {
        const result = await analyticsService.getAssessmentAnalytics(assessmentId);
        if (!cancelled) {
          setData(result);
          setLoadState('success');
        }
      } catch (err) {
        if (!cancelled) {
          setErrorMessage(err instanceof Error ? err.message : 'Unable to load analytics');
          setLoadState('error');
        }
      }
    }

    void fetchData();

    return () => {
      cancelled = true;
    };
  }, [assessmentId, retryKey]);

  const handleRetry = useCallback(() => {
    setLoadState('loading');
    setErrorMessage('');
    setRetryKey(k => k + 1);
  }, []);

  // ── Loading state ───────────────────────────────────────────────────────

  if (loadState === 'loading') {
    return (
      <main className="an-shell" aria-label="Analytics loading">
        <div className="an-loading" role="status" aria-live="polite">
          <span className="loader" aria-hidden="true" />
          <p className="an-loading__text">Loading assessment analytics…</p>
        </div>
      </main>
    );
  }

  // ── Error state ─────────────────────────────────────────────────────────

  if (loadState === 'error') {
    return (
      <main className="an-shell" aria-label="Analytics error">
        <header className="an-header">
          <div className="an-header__meta">
            <h1 className="an-header__title">Analytics unavailable</h1>
          </div>
          <button type="button" className="an-back-btn" onClick={onBack}>
            ← Back
          </button>
        </header>
        <div className="an-error" role="alert">
          <p className="an-error__kicker">Error</p>
          <h2>Could not load analytics</h2>
          <p>{errorMessage}</p>
          <button
            type="button"
            className="an-retry-btn"
            onClick={handleRetry}
          >
            Try again
          </button>
        </div>
      </main>
    );
  }

  // ── Success state ───────────────────────────────────────────────────────

  if (!data) return null;

  return (
    <main className="an-shell" aria-label="Analytics dashboard">
      <AnalyticsHeader
        assessmentId={data.assessmentId}
        title={data.assessmentTitle}
        onBack={onBack}
      />

      {data.totalSubmissions === 0 ? (
        <AnalyticsEmptyState assessmentId={data.assessmentId} onRetry={handleRetry} />
      ) : (
        <>
          <AnalyticsKpiGrid data={data} />
          <SubmissionMetrics data={data} />
          <ProblemPerformanceTable problems={data.problems} />
        </>
      )}
    </main>
  );
}

export default AdminAnalyticsPage;
