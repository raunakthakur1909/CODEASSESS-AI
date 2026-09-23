import type { AIAnalytics } from '../../types/analytics';

interface ImprovementAreasProps {
  data: AIAnalytics;
}

/**
 * Displays the top improvement areas identified by AI.
 */
function ImprovementAreas({ data }: ImprovementAreasProps) {
  if (!data.topImprovementAreas || data.topImprovementAreas.length === 0) {
    return null;
  }

  return (
    <section className="an-section" aria-labelledby="improvement-areas-heading">
      <div className="an-section-heading">
        <div className="an-section-marker" aria-hidden="true" />
        <h2 id="improvement-areas-heading">Top Improvement Areas</h2>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
        {data.topImprovementAreas.map((area, index) => (
          <div key={index} style={{ display: 'flex', gap: '12px', padding: '16px', background: '#eef2f5', borderRadius: '4px', borderLeft: '4px solid #b8d96b' }}>
            <div style={{ fontWeight: 600, color: 'var(--ink, #19242a)' }}>{index + 1}.</div>
            <div style={{ fontSize: '14px', color: 'var(--ink, #19242a)', lineHeight: 1.5 }}>
              {area}
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}

export default ImprovementAreas;
