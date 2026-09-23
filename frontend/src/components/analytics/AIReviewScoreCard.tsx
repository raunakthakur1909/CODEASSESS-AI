import type { AIAnalytics } from '../../types/analytics';

interface AIReviewScoreCardProps {
  data: AIAnalytics;
}

function fmt(n: number | null, decimals = 1): string {
  if (n === null || n === undefined) return '—';
  return n.toFixed(decimals);
}

/**
 * Top-level KPI card for the overall AI review score.
 */
function AIReviewScoreCard({ data }: AIReviewScoreCardProps) {
  const hasScore = data.averageOverallScore !== null;
  const score = hasScore ? Math.round(data.averageOverallScore!) : 0;
  
  // Choose a color based on score
  let color = 'var(--muted, #68757a)';
  if (hasScore) {
    if (score >= 90) color = '#b8d96b'; // green-ish
    else if (score >= 70) color = '#f3cb67'; // yellow-ish
    else color = '#e86c6c'; // red-ish
  }

  return (
    <div className="an-kpi-card" style={{ gridColumn: '1 / -1', display: 'flex', flexDirection: 'column', alignItems: 'center', padding: '32px' }}>
      <p className="an-kpi-card__label" style={{ marginBottom: '16px', fontSize: '14px', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
        Overall AI Code Quality
      </p>
      <div style={{ display: 'flex', alignItems: 'baseline', gap: '8px' }}>
        <span className="an-kpi-card__value" style={{ fontSize: '48px', color }}>
          {hasScore ? fmt(data.averageOverallScore) : '—'}
        </span>
        {hasScore && <span style={{ fontSize: '18px', color: 'var(--muted, #68757a)', fontWeight: 600 }}>/ 100</span>}
      </div>
      <p style={{ marginTop: '8px', fontSize: '13px', color: 'var(--muted, #68757a)' }}>
        Based on {data.totalReviews} AI review{data.totalReviews === 1 ? '' : 's'}
      </p>
    </div>
  );
}

export default AIReviewScoreCard;
