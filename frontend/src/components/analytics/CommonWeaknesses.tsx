import type { AIAnalytics } from '../../types/analytics';

interface CommonWeaknessesProps {
  data: AIAnalytics;
}

/**
 * Displays the most common weaknesses identified by AI.
 */
function CommonWeaknesses({ data }: CommonWeaknessesProps) {
  if (!data.mostCommonWeaknesses || Object.keys(data.mostCommonWeaknesses).length === 0) {
    return null;
  }

  // Sort by count descending
  const sortedWeaknesses = Object.entries(data.mostCommonWeaknesses)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 5); // top 5

  return (
    <section className="an-section" aria-labelledby="common-weaknesses-heading">
      <div className="an-section-heading">
        <div className="an-section-marker" aria-hidden="true" />
        <h2 id="common-weaknesses-heading">Common Weaknesses</h2>
      </div>

      <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'grid', gap: '8px' }}>
        {sortedWeaknesses.map(([weakness, count]) => (
          <li key={weakness} style={{ display: 'flex', justifyContent: 'space-between', padding: '12px', background: '#f8f9fa', borderRadius: '4px' }}>
            <span style={{ fontSize: '14px', color: 'var(--ink, #19242a)' }}>{weakness}</span>
            <span style={{ fontSize: '12px', color: 'var(--muted, #68757a)', fontFamily: 'var(--mono)', background: '#e9ecef', padding: '2px 8px', borderRadius: '12px' }}>
              {count} {count === 1 ? 'issue' : 'issues'}
            </span>
          </li>
        ))}
      </ul>
    </section>
  );
}

export default CommonWeaknesses;
