import type { AssessmentAnalytics } from '../../types/analytics';

interface SubmissionMetricsProps {
  data: AssessmentAnalytics;
}

function fmt(n: number | null, decimals = 1): string {
  if (n === null || n === undefined) return '—';
  return n.toFixed(decimals);
}

/**
 * Supplementary submission breakdown panel:
 * – accepted vs wrong-answer split
 * – average score progress bar
 * – average execution time
 *
 * All calculations are local — no business logic leaks into the page.
 */
function SubmissionMetrics({ data }: SubmissionMetricsProps) {
  const wrongAnswer = data.totalSubmissions - data.acceptedSubmissions;
  const avgScoreBar = data.averageScore !== null ? data.averageScore / 100 : null;

  return (
    <section className="an-section" aria-labelledby="submission-metrics-heading">
      <div className="an-section-heading">
        <div className="an-section-marker" aria-hidden="true" />
        <h2 id="submission-metrics-heading">Submission breakdown</h2>
      </div>

      <div className="an-metrics-row">

        <div className="an-metric">
          <p className="an-metric__label">Accepted</p>
          <p className="an-metric__value">{data.acceptedSubmissions}</p>
        </div>

        <div className="an-metric">
          <p className="an-metric__label">Not accepted</p>
          <p className="an-metric__value">{wrongAnswer}</p>
        </div>

        <div className="an-metric">
          <p className="an-metric__label">Total</p>
          <p className="an-metric__value">{data.totalSubmissions}</p>
        </div>

        <div className="an-metric">
          <p className="an-metric__label">Average score</p>
          <p className="an-metric__value">{fmt(data.averageScore)}</p>
          {avgScoreBar !== null && (
            <div
              className="an-bar-track"
              role="progressbar"
              aria-label="Average score"
              aria-valuemin={0}
              aria-valuemax={100}
              aria-valuenow={Math.round(avgScoreBar * 100)}
            >
              <div
                className="an-bar-fill"
                style={{ width: `${(avgScoreBar * 100).toFixed(1)}%` }}
              />
            </div>
          )}
        </div>

        <div className="an-metric">
          <p className="an-metric__label">Avg exec time</p>
          <p className="an-metric__value">
            {data.averageExecutionTimeMs !== null
              ? `${fmt(data.averageExecutionTimeMs, 0)} ms`
              : <span className="an-null">—</span>}
          </p>
        </div>

        <div className="an-metric">
          <p className="an-metric__label">Unique candidates</p>
          <p className="an-metric__value">{data.totalCandidates}</p>
        </div>

      </div>
    </section>
  );
}

export default SubmissionMetrics;
