import { useState, useEffect, useCallback } from 'react';
import { analyticsService } from '../services/analyticsService';
import type { AIAnalytics } from '../types/analytics';

import AIAnalyticsHeader from '../components/analytics/AIAnalyticsHeader';
import AIReviewScoreCard from '../components/analytics/AIReviewScoreCard';
import ComplexityInsights from '../components/analytics/ComplexityInsights';
import IssueSeverityChart from '../components/analytics/IssueSeverityChart';
import IssueCategoryChart from '../components/analytics/IssueCategoryChart';
import CommonWeaknesses from '../components/analytics/CommonWeaknesses';
import ImprovementAreas from '../components/analytics/ImprovementAreas';

import '../components/analytics/analytics.css';

interface AIAnalyticsPageProps {
  assessmentId: number;
  onBack: () => void;
}

type LoadState = 'loading' | 'success' | 'error';

/**
 * AI Analytics Dashboard — Milestone 9.9
 *
 * Entry: /?analytics=ai&assessmentId={n}
 *
 * Responsibilities:
 *   – fetches AI analytics data via getAIAnalytics(id)
 *   – manages loading / error / success states
 *   – delegates rendering to child components
 */
function AIAnalyticsPage({ assessmentId, onBack }: AIAnalyticsPageProps) {
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [data, setData] = useState<AIAnalytics | null>(null);
  const [errorMessage, setErrorMessage] = useState('');
  const [retryKey, setRetryKey] = useState(0);

  useEffect(() => {
    let cancelled = false;

    async function fetchData() {
      try {
        const result = await analyticsService.getAIAnalytics(assessmentId);
        if (!cancelled) {
          setData(result);
          setLoadState('success');
        }
      } catch (err) {
        if (!cancelled) {
          setErrorMessage(err instanceof Error ? err.message : 'Unable to load AI analytics');
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
      <main className="an-shell" aria-label="AI analytics loading">
        <div className="an-loading" role="status" aria-live="polite">
          <span className="loader" aria-hidden="true" />
          <p className="an-loading__text">Loading AI insights…</p>
        </div>
      </main>
    );
  }

  // ── Error ──────────────────────────────────────────────────────────────

  if (loadState === 'error') {
    return (
      <main className="an-shell" aria-label="AI analytics error">
        <header className="an-header">
          <div className="an-header__meta">
            <h1 className="an-header__title">AI Insights unavailable</h1>
          </div>
          <button type="button" className="an-back-btn" onClick={onBack}>
            ← Back
          </button>
        </header>
        <div className="an-error" role="alert">
          <p className="an-error__kicker">Error</p>
          <h2>Could not load AI analytics</h2>
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
    <main className="an-shell" aria-label="AI analytics dashboard">
      <AIAnalyticsHeader assessmentId={assessmentId} onBack={onBack} />

      {data.totalReviews === 0 ? (
        <div className="an-empty" role="status" aria-live="polite">
          <div className="an-empty__icon" aria-hidden="true">🤖</div>
          <h2>No AI reviews yet</h2>
          <p>
            There are no completed AI reviews for this assessment. Once candidates submit code and reviews are processed, insights will appear here.
          </p>
          <button
            type="button"
            className="an-retry-btn"
            onClick={handleRetry}
            style={{ marginTop: '20px' }}
          >
            Reload
          </button>
        </div>
      ) : (
        <div style={{ display: 'grid', gap: '24px' }}>
          {/* Top row: Overall Score & Complexity */}
          <section className="an-section" style={{ padding: 0, border: 'none', background: 'none' }}>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '24px' }}>
              <AIReviewScoreCard data={data} />
            </div>
          </section>

          <section className="an-section" aria-labelledby="complexity-heading">
             <div className="an-section-heading">
              <div className="an-section-marker" aria-hidden="true" />
              <h2 id="complexity-heading">Algorithm Efficiency</h2>
            </div>
            <ComplexityInsights data={data} />
          </section>

          {/* Issue distributions */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))', gap: '24px' }}>
            <IssueSeverityChart data={data} />
            <IssueCategoryChart data={data} />
          </div>

          {/* Text-based insights */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))', gap: '24px' }}>
            <CommonWeaknesses data={data} />
            <ImprovementAreas data={data} />
          </div>
        </div>
      )}
    </main>
  );
}

export default AIAnalyticsPage;
