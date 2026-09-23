import type { AssessmentAnalytics } from '../../types/analytics';

interface AnalyticsKpiGridProps {
  data: AssessmentAnalytics;
}

/** Format a nullable number to 1 dp or return null sentinel */
function fmt(n: number | null, decimals = 1): string {
  if (n === null || n === undefined) return '—';
  return n.toFixed(decimals);
}

/** Format a rate (0–1 float) as a percentage string */
function pct(n: number | null): string {
  if (n === null || n === undefined) return '—';
  return `${(n * 100).toFixed(1)}%`;
}

interface KpiCardProps {
  label: string;
  value: string;
  unit?: string;
  accent?: boolean;
  /** 0–1 to show a progress bar; undefined = no bar */
  barValue?: number | null;
}

function KpiCard({ label, value, unit, accent = false, barValue }: KpiCardProps) {
  const cls = ['an-kpi-card', accent ? 'an-kpi-card--accent' : ''].filter(Boolean).join(' ');
  return (
    <article className={cls} aria-label={label}>
      <p className="an-kpi-card__label">{label}</p>
      <div className="an-kpi-card__value" aria-describedby={undefined}>{value}</div>
      {unit && <p className="an-kpi-card__unit">{unit}</p>}
      {barValue !== undefined && barValue !== null && (
        <div
          className="an-bar-track"
          role="progressbar"
          aria-label={label}
          aria-valuemin={0}
          aria-valuemax={100}
          aria-valuenow={Math.round(barValue * 100)}
        >
          <div
            className="an-bar-fill"
            style={{ width: `${Math.min(100, barValue * 100).toFixed(1)}%` }}
          />
        </div>
      )}
    </article>
  );
}

/**
 * Six KPI cards derived from AssessmentAnalytics.
 * All formatting / calculations live here — components stay pure.
 */
function AnalyticsKpiGrid({ data }: AnalyticsKpiGridProps) {
  const acceptanceRate =
    data.totalSubmissions > 0
      ? data.acceptedSubmissions / data.totalSubmissions
      : null;

  return (
    <section
      className="an-kpi-grid"
      aria-label="Key performance indicators"
    >
      <KpiCard
        label="Total candidates"
        value={String(data.totalCandidates)}
        unit="unique participants"
        accent
      />

      <KpiCard
        label="Total submissions"
        value={String(data.totalSubmissions)}
        unit={`${data.acceptedSubmissions} accepted`}
      />

      <KpiCard
        label="Acceptance rate"
        value={pct(acceptanceRate)}
        unit="accepted ÷ submitted"
        barValue={acceptanceRate}
      />

      <KpiCard
        label="Average score"
        value={fmt(data.averageScore)}
        unit="out of 100"
      />

      <KpiCard
        label="Completion rate"
        value={pct(data.completionRate)}
        unit="candidates who attempted all problems"
        barValue={data.completionRate}
      />

      <KpiCard
        label="Avg execution time"
        value={
          data.averageExecutionTimeMs !== null
            ? `${fmt(data.averageExecutionTimeMs, 0)} ms`
            : '—'
        }
        unit="across all submissions"
      />
    </section>
  );
}

export default AnalyticsKpiGrid;
