import type { CandidateAnalytics } from '../../types/analytics';

interface CandidatePerformanceProps {
  data: CandidateAnalytics;
}

/** Format a nullable number to n decimal places, or em-dash */
function fmt(n: number | null, decimals = 1): string {
  if (n === null || n === undefined) return '—';
  return n.toFixed(decimals);
}

/**
 * Top-row KPI cards for a candidate:
 *   Assessments · Submissions · Avg Score · Best Score
 *
 * Plus a second row with acceptance rate (prominent bar) and execution time.
 *
 * ⚠️  acceptanceRate from the backend is already a percentage (0–100).
 *     Do NOT multiply by 100 again.
 */
function CandidatePerformance({ data }: CandidatePerformanceProps) {
  // acceptanceRate is 0–100 from backend; convert to 0–1 only for the progress bar width
  const acceptanceBarWidth =
    data.acceptanceRate !== null
      ? Math.min(100, Math.max(0, data.acceptanceRate))
      : 0;

  return (
    <section className="an-section" aria-labelledby="candidate-perf-heading">
      <div className="an-section-heading">
        <div className="an-section-marker" aria-hidden="true" />
        <h2 id="candidate-perf-heading">My performance</h2>
      </div>

      {/* Top 4 KPI cards */}
      <div className="an-kpi-grid" style={{ marginBottom: '24px' }}>

        <article className="an-kpi-card an-kpi-card--accent" aria-label="Assessments attempted">
          <p className="an-kpi-card__label">Assessments</p>
          <div className="an-kpi-card__value">{data.assessmentsAttempted}</div>
          <p className="an-kpi-card__unit">attempted</p>
        </article>

        <article className="an-kpi-card" aria-label="Total submissions">
          <p className="an-kpi-card__label">Submissions</p>
          <div className="an-kpi-card__value">{data.totalSubmissions}</div>
          <p className="an-kpi-card__unit">{data.acceptedCount} accepted</p>
        </article>

        <article className="an-kpi-card" aria-label="Average score">
          <p className="an-kpi-card__label">Avg score</p>
          <div className="an-kpi-card__value">{fmt(data.averageScore)}</div>
          <p className="an-kpi-card__unit">out of 100</p>
        </article>

        <article className="an-kpi-card" aria-label="Best score">
          <p className="an-kpi-card__label">Best score</p>
          <div className="an-kpi-card__value">{fmt(data.bestScore)}</div>
          <p className="an-kpi-card__unit">personal best</p>
        </article>

      </div>

      {/* Acceptance rate — prominent single-row bar */}
      <div className="an-metric" style={{ borderTop: '1px solid var(--line, #d9ded7)', paddingTop: '16px' }}>
        <p className="an-metric__label">Acceptance rate</p>
        <p className="an-metric__value">
          {data.acceptanceRate !== null ? `${fmt(data.acceptanceRate)}%` : '—'}
        </p>
        <div
          className="an-bar-track"
          role="progressbar"
          aria-label="Acceptance rate"
          aria-valuemin={0}
          aria-valuemax={100}
          aria-valuenow={Math.round(acceptanceBarWidth)}
        >
          <div
            className="an-bar-fill"
            style={{ width: `${acceptanceBarWidth.toFixed(1)}%` }}
          />
        </div>
        <p className="an-kpi-card__unit" style={{ marginTop: '8px' }}>
          {data.acceptedCount} accepted out of {data.totalSubmissions} total submissions
        </p>
      </div>

    </section>
  );
}

export default CandidatePerformance;
