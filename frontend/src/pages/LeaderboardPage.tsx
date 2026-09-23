import { useState, useEffect, useCallback } from 'react';
import { analyticsService } from '../services/analyticsService';
import type { LeaderboardEntry } from '../types/analytics';

import LeaderboardPodium from '../components/analytics/LeaderboardPodium';
import LeaderboardTable from '../components/analytics/LeaderboardTable';

import '../components/analytics/analytics.css';

interface LeaderboardPageProps {
  assessmentId: number;
  onBack: () => void;
}

type LoadState = 'loading' | 'success' | 'error';

/**
 * Leaderboard Dashboard — Milestone 9.8
 *
 * Entry: /?analytics=leaderboard&assessmentId={n}
 *
 * Responsibilities:
 *   – fetches leaderboard data via getLeaderboard(id)
 *   – manages loading / error / success states
 *   – delegates rendering to child components
 */
function LeaderboardPage({ assessmentId, onBack }: LeaderboardPageProps) {
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [data, setData] = useState<LeaderboardEntry[]>([]);
  const [errorMessage, setErrorMessage] = useState('');
  const [retryKey, setRetryKey] = useState(0);

  useEffect(() => {
    let cancelled = false;

    async function fetchData() {
      try {
        const result = await analyticsService.getLeaderboard(assessmentId);
        if (!cancelled) {
          setData(result);
          setLoadState('success');
        }
      } catch (err) {
        if (!cancelled) {
          setErrorMessage(err instanceof Error ? err.message : 'Unable to load leaderboard');
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

  // ── Loading ────────────────────────────────────────────────────────────

  if (loadState === 'loading') {
    return (
      <main className="an-shell" aria-label="Leaderboard loading">
        <div className="an-loading" role="status" aria-live="polite">
          <span className="loader" aria-hidden="true" />
          <p className="an-loading__text">Loading leaderboard rankings…</p>
        </div>
      </main>
    );
  }

  // ── Error ──────────────────────────────────────────────────────────────

  if (loadState === 'error') {
    return (
      <main className="an-shell" aria-label="Leaderboard error">
        <header className="an-header">
          <div className="an-header__meta">
            <h1 className="an-header__title">Leaderboard unavailable</h1>
          </div>
          <button type="button" className="an-back-btn" onClick={onBack}>
            ← Back
          </button>
        </header>
        <div className="an-error" role="alert">
          <p className="an-error__kicker">Error</p>
          <h2>Could not load leaderboard</h2>
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

  const isEmpty = data.length === 0;
  const top3 = data.slice(0, 3);

  return (
    <main className="an-shell" aria-label="Assessment Leaderboard">
      <header className="an-header" aria-label="Leaderboard header">
        <div className="an-header__meta">
          <p className="an-header__kicker">
            Assessment #{assessmentId}
          </p>
          <h1 className="an-header__title">Leaderboard</h1>
          <p className="an-header__subtitle">
            Rankings based on score, accepted submissions, and execution time.
          </p>
        </div>
        <button type="button" className="an-back-btn" onClick={onBack}>
          ← Back
        </button>
      </header>

      {isEmpty ? (
        <div className="an-empty" role="status" aria-live="polite">
          <div className="an-empty__icon" aria-hidden="true">🏆</div>
          <h2>No rankings yet</h2>
          <p>
            Nobody has successfully submitted a solution for this assessment yet. The first one to score points will take the #1 spot!
          </p>
          <button
            type="button"
            className="an-retry-btn"
            onClick={handleRetry}
            style={{ marginTop: '20px' }}
          >
            Reload Leaderboard
          </button>
        </div>
      ) : (
        <>
          <LeaderboardPodium topEntries={top3} />
          <LeaderboardTable entries={data} />
        </>
      )}
    </main>
  );
}

export default LeaderboardPage;
