import type { AIAnalytics } from '../../types/analytics';

interface IssueSeverityChartProps {
  data: AIAnalytics;
}

/**
 * Visual breakdown of issues by severity (CRITICAL, HIGH, MEDIUM, LOW)
 * using a premium Donut/Ring visualization.
 */
function IssueSeverityChart({ data }: IssueSeverityChartProps) {
  const total = data.totalIssuesFound;
  const hasIssues = total > 0;

  const criticalPct = hasIssues ? (data.criticalIssues / total) * 100 : 0;
  const highPct = hasIssues ? (data.highIssues / total) * 100 : 0;
  const mediumPct = hasIssues ? (data.mediumIssues / total) * 100 : 0;
  const lowPct = hasIssues ? (data.lowIssues / total) * 100 : 0;

  // Construct the conic-gradient dynamically
  let currentAngle = 0;
  const gradients: string[] = [];
  
  if (criticalPct > 0) {
    gradients.push(`#e86c6c ${currentAngle}% ${currentAngle + criticalPct}%`);
    currentAngle += criticalPct;
  }
  if (highPct > 0) {
    gradients.push(`#f3cb67 ${currentAngle}% ${currentAngle + highPct}%`);
    currentAngle += highPct;
  }
  if (mediumPct > 0) {
    gradients.push(`#b8d96b ${currentAngle}% ${currentAngle + mediumPct}%`);
    currentAngle += mediumPct;
  }
  if (lowPct > 0) {
    gradients.push(`#7a8e85 ${currentAngle}% ${currentAngle + lowPct}%`);
  }

  const gradientString = gradients.length > 0 
    ? `conic-gradient(${gradients.join(', ')})`
    : 'none';

  return (
    <section className="an-section" aria-labelledby="issue-severity-heading">
      <div className="an-section-heading">
        <div className="an-section-marker" aria-hidden="true" />
        <h2 id="issue-severity-heading">Severity Distribution</h2>
      </div>

      <div style={{ display: 'flex', gap: '32px', alignItems: 'center', flexWrap: 'wrap' }}>
        
        {/* Donut Chart */}
        <div style={{ flex: '0 0 auto' }}>
          {hasIssues ? (
            <div 
              className="an-donut-chart" 
              style={{ width: '160px', height: '160px', background: gradientString }}
              role="img"
              aria-label={`Chart showing ${total} total issues`}
            >
              <div className="an-donut-inner">
                <span style={{ fontSize: '32px', fontFamily: 'var(--heading)', fontWeight: 700, color: 'var(--ink)', lineHeight: 1 }}>
                  {total}
                </span>
                <span style={{ fontSize: '11px', textTransform: 'uppercase', letterSpacing: '1px', color: 'var(--muted)', marginTop: '4px' }}>
                  Total
                </span>
              </div>
            </div>
          ) : (
            <div 
              className="an-donut-chart" 
              style={{ width: '160px', height: '160px', background: 'rgba(25, 36, 42, 0.05)' }}
            >
              <div className="an-donut-inner">
                <span style={{ fontSize: '32px', fontFamily: 'var(--heading)', color: 'var(--muted)' }}>0</span>
              </div>
            </div>
          )}
        </div>

        {/* Legend */}
        <div style={{ flex: '1 1 auto', display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(120px, 1fr))', gap: '16px' }}>
          <div className="an-kpi-card" style={{ padding: '16px 20px' }}>
            <p className="an-kpi-card__label" style={{ color: '#e86c6c' }}>Critical</p>
            <p className="an-kpi-card__value" style={{ fontSize: '28px' }}>{data.criticalIssues}</p>
          </div>
          <div className="an-kpi-card" style={{ padding: '16px 20px' }}>
            <p className="an-kpi-card__label" style={{ color: '#d4a218' }}>High</p>
            <p className="an-kpi-card__value" style={{ fontSize: '28px' }}>{data.highIssues}</p>
          </div>
          <div className="an-kpi-card" style={{ padding: '16px 20px' }}>
            <p className="an-kpi-card__label" style={{ color: '#7ea429' }}>Medium</p>
            <p className="an-kpi-card__value" style={{ fontSize: '28px' }}>{data.mediumIssues}</p>
          </div>
          <div className="an-kpi-card" style={{ padding: '16px 20px' }}>
            <p className="an-kpi-card__label" style={{ color: '#7a8e85' }}>Low</p>
            <p className="an-kpi-card__value" style={{ fontSize: '28px' }}>{data.lowIssues}</p>
          </div>
        </div>

      </div>
    </section>
  );
}

export default IssueSeverityChart;
