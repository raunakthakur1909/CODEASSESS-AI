import { useState, useEffect, useCallback } from 'react';
import { analyticsService } from '../services/analyticsService';
import type { CandidateAnalytics } from '../types/analytics';

import CandidatePerformance from '../components/analytics/CandidatePerformance';
import AssessmentPerformance from '../components/analytics/AssessmentPerformance';
import RecentActivity from '../components/analytics/RecentActivity';

import '../components/analytics/analytics.css';

interface CandidateAnalyticsPageProps {
  /** 'me' for the current authenticated user, or a numeric string ID */
  candidateId: string;
  onBack: () => void;
}

type LoadState = 'loading' | 'success' | 'error';

/**
 * Candidate Analytics Dashboard — Milestone 9.7
 *
 * Entry: /?analytics=candidate&candidateId=me
 *    or: /?analytics=candidate&candidateId={n}
 *
 * Responsibilities:
 *   – fetches own data via getMyCandidateAnalytics() if candidateId === 'me'
 *   – fetches specific user data via getCandidateAnalytics(id) otherwise
 *   – manages loading / error / success states
 *   – delegates rendering to pure child components
 */
function CandidateAnalyticsPage({ candidateId, onBack }: CandidateAnalyticsPageProps) {
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [data, setData] = useState<CandidateAnalytics | null>(null);
  const [errorMessage, setErrorMessage] = useState('');
  const [retryKey, setRetryKey] = useState(0);

  useEffect(() => {
    let cancelled = false;

    async function fetchData() {
      try {
        let result: CandidateAnalytics;
        if (candidateId === 'me') {
          result = await analyticsService.getMyCandidateAnalytics();
        } else {
          result = await analyticsService.getCandidateAnalytics(Number(candidateId));
        }

        if (!cancelled) {
          setData(result);
          setLoadState('success');
        }
      } catch (err) {
        if (!cancelled) {
          setErrorMessage(err instanceof Error ? err.message : 'Unable to load candidate analytics');
          setLoadState('error');
        }
      }
    }

    void fetchData();

    return () => {
      cancelled = true;
    };
  }, [candidateId, retryKey]);

  const handleRetry = useCallback(() => {
    setLoadState('loading');
    setErrorMessage('');
    setRetryKey(k => k + 1);
  }, []);

  // ── Loading ────────────────────────────────────────────────────────────

  if (loadState === 'loading') {
    return (
      <main className="an-shell" aria-label="Candidate analytics loading">
        <div className="an-loading" role="status" aria-live="polite">
          <span className="loader" aria-hidden="true" />
          <p className="an-loading__text">Loading candidate profile…</p>
        </div>
      </main>
    );
  }

  // ── Error ──────────────────────────────────────────────────────────────

  if (loadState === 'error') {
    return (
      <main className="an-shell" aria-label="Candidate analytics error">
        <header className="an-header">
          <div className="an-header__meta">
            <h1 className="an-header__title">Profile unavailable</h1>
          </div>
          <button type="button" className="an-back-btn" onClick={onBack}>
            ← Back
          </button>
        </header>
        <div className="an-error" role="alert">
          <p className="an-error__kicker">Error</p>
          <h2>Could not load profile</h2>
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

  // ── Success ────────────────────────────────────────────────────────────

  if (!data) return null;

  return (
    <main className="an-shell" aria-label="Candidate analytics dashboard">
      
      <header className="an-header" aria-label="Candidate header">
        <div className="an-header__meta">
          <p className="an-header__kicker">
            Candidate Profile
          </p>
          <h1 className="an-header__title">{data.candidateName}</h1>
          <p className="an-header__subtitle">
            {data.candidateEmail} &middot; ID: {data.candidateId}
          </p>
        </div>
        <button type="button" className="an-back-btn" onClick={onBack}>
          ← Back
        </button>
      </header>

      {data.totalSubmissions === 0 ? (
        <div className="an-empty" role="status" aria-live="polite">
          <div className="an-empty__icon" aria-hidden="true">💻</div>
          <h2>No submissions yet</h2>
          <p>
            This candidate hasn't submitted any code yet. Once they complete an assessment, their performance profile will appear here.
          </p>
          <button
            type="button"
            className="an-retry-btn"
            onClick={handleRetry}
            style={{ marginTop: '20px' }}
          >
            Refresh
          </button>
        </div>
      ) : (
        <>
          <CandidatePerformance data={data} />
          <AssessmentPerformance data={data} />
          <RecentActivity data={data} />
        </>
      )}
    </main>
  );
}

export default CandidateAnalyticsPage;
