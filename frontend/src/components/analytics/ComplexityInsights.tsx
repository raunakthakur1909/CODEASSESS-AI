import type { AIAnalytics } from '../../types/analytics';

interface ComplexityInsightsProps {
  data: AIAnalytics;
}

function fmt(n: number | null, decimals = 1): string {
  if (n === null || n === undefined) return '—';
  return n.toFixed(decimals);
}

/**
 * Displays Big-O time and space complexity scores.
 */
function ComplexityInsights({ data }: ComplexityInsightsProps) {
  return (
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px' }}>
      <div className="an-kpi-card">
        <p className="an-kpi-card__label">Time Complexity Score</p>
        <p className="an-kpi-card__value">
          {data.averageTimeComplexityScore !== null 
            ? fmt(data.averageTimeComplexityScore) 
            : <span className="an-null">—</span>}
        </p>
        {data.averageTimeComplexityScore !== null && (
          <div className="an-bar-track" aria-hidden="true" style={{ marginTop: '12px' }}>
            <div 
              className="an-bar-fill" 
              style={{ width: `${Math.max(0, Math.min(100, data.averageTimeComplexityScore))}%` }} 
            />
          </div>
        )}
      </div>

      <div className="an-kpi-card">
        <p className="an-kpi-card__label">Space Complexity Score</p>
        <p className="an-kpi-card__value">
          {data.averageSpaceComplexityScore !== null 
            ? fmt(data.averageSpaceComplexityScore) 
            : <span className="an-null">—</span>}
        </p>
        {data.averageSpaceComplexityScore !== null && (
          <div className="an-bar-track" aria-hidden="true" style={{ marginTop: '12px' }}>
            <div 
              className="an-bar-fill" 
              style={{ width: `${Math.max(0, Math.min(100, data.averageSpaceComplexityScore))}%` }} 
            />
          </div>
        )}
      </div>
    </div>
  );
}

export default ComplexityInsights;
