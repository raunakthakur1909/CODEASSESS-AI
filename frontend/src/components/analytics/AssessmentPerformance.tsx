import type { CandidateAnalytics } from '../../types/analytics';

interface AssessmentPerformanceProps {
  data: CandidateAnalytics;
}

function fmt(n: number | null, decimals = 1): string {
  if (n === null || n === undefined) return '—';
  return n.toFixed(decimals);
}

/**
 * Submission outcome breakdown — shows accepted vs wrong-answer distribution
 * as a stacked visual bar and a metrics grid.
 *
 * Uses only the aggregate fields available in CandidateAnalytics;
 * no per-assessment breakdown is available at this endpoint level.
 */
function AssessmentPerformance({ data }: AssessmentPerformanceProps) {
  const total = data.totalSubmissions;
  const acceptedPct = total > 0 ? (data.acceptedCount / total) * 100 : 0;
  const wrongPct = total > 0 ? (data.wrongAnswerCount / total) * 100 : 0;
  const otherCount = total - data.acceptedCount - data.wrongAnswerCount;
  const otherPct = total > 0 ? (otherCount / total) * 100 : 0;

  return (
    <section className="an-section" aria-labelledby="assessment-perf-heading">
      <div className="an-section-heading">
        <div className="an-section-marker" aria-hidden="true" />
        <h2 id="assessment-perf-heading">Assessment performance</h2>
      </div>

      {/* Stacked outcome bar */}
      {total > 0 && (
        <div style={{ marginBottom: '24px' }}>
          <p className="an-kpi-card__label" style={{ marginBottom: '10px' }}>
            Submission outcomes
          </p>
          <div
            style={{
              display: 'flex',
              height: '14px',
              borderRadius: '2px',
              overflow: 'hidden',
              background: 'var(--line, #d9ded7)',
            }}
            role="img"
            aria-label={`Accepted ${acceptedPct.toFixed(0)}%, wrong answer ${wrongPct.toFixed(0)}%, other ${otherPct.toFixed(0)}%`}
          >
            <div
              style={{ width: `${acceptedPct}%`, background: '#b8d96b', transition: 'width 0.4s' }}
              title={`Accepted: ${data.acceptedCount}`}
            />
            <div
              style={{ width: `${wrongPct}%`, background: '#f19a79', transition: 'width 0.4s' }}
              title={`Wrong answer: ${data.wrongAnswerCount}`}
            />
            {otherCount > 0 && (
              <div
                style={{ width: `${otherPct}%`, background: '#d9ded7' }}
                title={`Other: ${otherCount}`}
              />
            )}
          </div>
          <div style={{ display: 'flex', gap: '20px', marginTop: '8px' }}>
            <span style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '12px', color: 'var(--muted, #68757a)', fontFamily: 'var(--mono)' }}>
              <span style={{ width: '10px', height: '10px', background: '#b8d96b', display: 'inline-block' }} />
              Accepted
            </span>
            <span style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '12px', color: 'var(--muted, #68757a)', fontFamily: 'var(--mono)' }}>
              <span style={{ width: '10px', height: '10px', background: '#f19a79', display: 'inline-block' }} />
              Wrong answer
            </span>
            {otherCount > 0 && (
              <span style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '12px', color: 'var(--muted, #68757a)', fontFamily: 'var(--mono)' }}>
                <span style={{ width: '10px', height: '10px', background: '#d9ded7', display: 'inline-block' }} />
                Other
              </span>
            )}
          </div>
        </div>
      )}

      {/* Metrics grid */}
      <div className="an-metrics-row">
        <div className="an-metric">
          <p className="an-metric__label">Accepted</p>
          <p className="an-metric__value">{data.acceptedCount}</p>
        </div>
        <div className="an-metric">
          <p className="an-metric__label">Wrong answer</p>
          <p className="an-metric__value">{data.wrongAnswerCount}</p>
        </div>
        <div className="an-metric">
          <p className="an-metric__label">Total</p>
          <p className="an-metric__value">{data.totalSubmissions}</p>
        </div>
        <div className="an-metric">
          <p className="an-metric__label">Assessments attempted</p>
          <p className="an-metric__value">{data.assessmentsAttempted}</p>
        </div>
        <div className="an-metric">
          <p className="an-metric__label">Avg score</p>
          <p className="an-metric__value">{fmt(data.averageScore)}</p>
        </div>
        <div className="an-metric">
          <p className="an-metric__label">Best score</p>
          <p className="an-metric__value">{fmt(data.bestScore)}</p>
        </div>
      </div>
    </section>
  );
}

export default AssessmentPerformance;
