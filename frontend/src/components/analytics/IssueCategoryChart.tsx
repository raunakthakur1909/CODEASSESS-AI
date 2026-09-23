import type { AIAnalytics } from '../../types/analytics';

interface IssueCategoryChartProps {
  data: AIAnalytics;
}

/**
 * Breakdown of issues by category.
 */
function IssueCategoryChart({ data }: IssueCategoryChartProps) {
  const categories = [
    { label: 'Security', count: data.securityIssues },
    { label: 'Performance', count: data.performanceIssues },
    { label: 'Maintainability', count: data.maintainabilityIssues },
    { label: 'Complexity', count: data.complexityIssues },
    { label: 'Best Practices', count: data.bestPracticesIssues },
    { label: 'Bug Risk', count: data.bugRiskIssues },
  ].sort((a, b) => b.count - a.count); // sort descending

  const maxCount = Math.max(...categories.map(c => c.count));

  return (
    <section className="an-section" aria-labelledby="issue-category-heading">
      <div className="an-section-heading">
        <div className="an-section-marker" aria-hidden="true" />
        <h2 id="issue-category-heading">Issue Categories</h2>
      </div>

      <div style={{ display: 'grid', gap: '12px' }}>
        {categories.map((cat) => {
          const widthPct = maxCount > 0 ? (cat.count / maxCount) * 100 : 0;
          return (
            <div key={cat.label} style={{ display: 'grid', gridTemplateColumns: '120px 1fr 40px', alignItems: 'center', gap: '16px' }}>
              <span style={{ fontSize: '14px', color: 'var(--ink, #19242a)' }}>{cat.label}</span>
              <div className="an-bar-track" aria-hidden="true" style={{ height: '8px' }}>
                <div 
                  className="an-bar-fill" 
                  style={{ width: `${widthPct}%`, background: 'var(--ink, #19242a)' }} 
                />
              </div>
              <span style={{ fontSize: '14px', fontFamily: 'var(--mono)', textAlign: 'right' }}>
                {cat.count}
              </span>
            </div>
          );
        })}
      </div>
    </section>
  );
}

export default IssueCategoryChart;
