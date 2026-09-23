import type { CandidateAnalytics } from '../../types/analytics';

interface RecentActivityProps {
  data: CandidateAnalytics;
}

function fmt(n: number | null, decimals = 1): string {
  if (n === null || n === undefined) return '—';
  return n.toFixed(decimals);
}

/**
 * Performance efficiency panel — shows execution time, AI review score,
 * and a combined "efficiency profile" derived from the aggregate submission stats.
 *
 * Note: The CandidateAnalytics endpoint returns aggregate data, not per-submission
 * records. This component surfaces the available time/score metrics and a
 * submissions-per-assessment ratio as a proxy for activity density.
 */
function RecentActivity({ data }: RecentActivityProps) {
  // Submissions per assessment — proxy for engagement density
  const submissionsPerAssessment =
    data.assessmentsAttempted > 0
      ? data.totalSubmissions / data.assessmentsAttempted
      : null;

  const hasAiReview = data.averageAiReviewScore !== null;

  return (
    <section className="an-section" aria-labelledby="recent-activity-heading">
      <div className="an-section-heading">
        <div className="an-section-marker" aria-hidden="true" />
        <h2 id="recent-activity-heading">Activity & efficiency</h2>
      </div>

      <div className="an-metrics-row">

        <div className="an-metric">
          <p className="an-metric__label">Avg execution time</p>
          <p className="an-metric__value">
            {data.averageExecutionTimeMs !== null
              ? `${fmt(data.averageExecutionTimeMs, 0)} ms`
              : <span className="an-null">—</span>}
          </p>
        </div>

        <div className="an-metric">
          <p className="an-metric__label">Submissions per exam</p>
          <p className="an-metric__value">
            {submissionsPerAssessment !== null
              ? fmt(submissionsPerAssessment)
              : <span className="an-null">—</span>}
          </p>
        </div>

        <div className="an-metric">
          <p className="an-metric__label">AI review score</p>
          <p className="an-metric__value">
            {hasAiReview
              ? fmt(data.averageAiReviewScore)
              : <span className="an-null">No reviews yet</span>}
          </p>
        </div>

      </div>

      {/* Score vs acceptance bar comparison */}
      {data.totalSubmissions > 0 && (
        <div style={{ marginTop: '24px', display: 'grid', gap: '16px' }}>

          {data.averageScore !== null && (
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '6px' }}>
                <span className="an-kpi-card__label">Average score</span>
                <span style={{ fontFamily: 'var(--mono)', fontSize: '12px', color: 'var(--ink, #19242a)' }}>
                  {fmt(data.averageScore)} / 100
                </span>
              </div>
              <div
                className="an-bar-track"
                role="progressbar"
                aria-label="Average score"
                aria-valuemin={0}
                aria-valuemax={100}
                aria-valuenow={Math.round(data.averageScore)}
              >
                <div className="an-bar-fill" style={{ width: `${data.averageScore.toFixed(1)}%` }} />
              </div>
            </div>
          )}

          {hasAiReview && data.averageAiReviewScore !== null && (
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '6px' }}>
                <span className="an-kpi-card__label">AI review score</span>
                <span style={{ fontFamily: 'var(--mono)', fontSize: '12px', color: 'var(--ink, #19242a)' }}>
                  {fmt(data.averageAiReviewScore)} / 100
                </span>
              </div>
              <div
                className="an-bar-track"
                role="progressbar"
                aria-label="AI review score"
                aria-valuemin={0}
                aria-valuemax={100}
                aria-valuenow={Math.round(data.averageAiReviewScore)}
              >
                <div
                  className="an-bar-fill"
                  style={{
                    width: `${data.averageAiReviewScore.toFixed(1)}%`,
                    background: '#f3cb67',
                  }}
                />
              </div>
            </div>
          )}

        </div>
      )}
    </section>
  );
}

export default RecentActivity;
