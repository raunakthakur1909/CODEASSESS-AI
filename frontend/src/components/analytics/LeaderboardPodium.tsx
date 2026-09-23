import type { LeaderboardEntry } from '../../types/analytics';

interface LeaderboardPodiumProps {
  topEntries: LeaderboardEntry[];
}

function fmt(n: number | null, decimals = 1): string {
  if (n === null || n === undefined) return '—';
  return n.toFixed(decimals);
}

/**
 * Visual podium for the top 3 candidates.
 * Position 2 is on the left, 1 in the centre, 3 on the right.
 */
function LeaderboardPodium({ topEntries }: LeaderboardPodiumProps) {
  if (topEntries.length === 0) return null;

  // Ensure we have up to 3 entries, padded with null if missing
  const first = topEntries[0] || null;
  const second = topEntries[1] || null;
  const third = topEntries[2] || null;

  return (
    <section className="an-section" aria-label="Top 3 podium" style={{ textAlign: 'center', padding: '40px 20px', overflow: 'hidden' }}>
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'flex-end', gap: '16px', minHeight: '220px' }}>
        
        {/* SECOND PLACE */}
        <div style={{ flex: '1', maxWidth: '160px', opacity: second ? 1 : 0.4 }}>
          {second && (
            <div style={{ marginBottom: '16px' }}>
              <div style={{ fontSize: '28px', marginBottom: '8px', filter: 'drop-shadow(0 2px 4px rgba(0,0,0,0.1))' }}>🥈</div>
              <div style={{ fontWeight: 600, fontSize: '15px', color: 'var(--ink, #19242a)', textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' }}>
                {second.candidateName}
              </div>
              <div style={{ fontSize: '12px', color: 'var(--muted, #68757a)', fontFamily: 'var(--mono)', marginTop: '4px' }}>
                {fmt(second.totalScore)} pts
              </div>
            </div>
          )}
          <div className="an-podium-bar" style={{ height: '100px', background: 'linear-gradient(180deg, #e2e6df 0%, #cbd1c5 100%)', borderRadius: '12px 12px 0 0', border: '1px solid rgba(255,255,255,0.6)', borderBottom: 0 }} />
        </div>

        {/* FIRST PLACE */}
        <div style={{ flex: '1', maxWidth: '180px', opacity: first ? 1 : 0.4, zIndex: 2 }}>
          {first && (
            <div style={{ marginBottom: '20px' }}>
              <div style={{ fontSize: '36px', marginBottom: '8px', filter: 'drop-shadow(0 4px 8px rgba(0,0,0,0.15))' }}>🏆</div>
              <div style={{ fontWeight: 700, fontSize: '18px', color: 'var(--ink, #19242a)', textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' }}>
                {first.candidateName}
              </div>
              <div style={{ fontSize: '14px', fontWeight: 700, color: '#7ea429', fontFamily: 'var(--mono)', marginTop: '4px' }}>
                {fmt(first.totalScore)} pts
              </div>
            </div>
          )}
          <div className="an-podium-bar" style={{ height: '140px', background: 'linear-gradient(180deg, #f3cb67 0%, #d4a218 100%)', borderRadius: '12px 12px 0 0', border: '1px solid rgba(255,255,255,0.6)', borderBottom: 0 }} />
        </div>

        {/* THIRD PLACE */}
        <div style={{ flex: '1', maxWidth: '160px', opacity: third ? 1 : 0.4 }}>
          {third && (
            <div style={{ marginBottom: '16px' }}>
              <div style={{ fontSize: '28px', marginBottom: '8px', filter: 'drop-shadow(0 2px 4px rgba(0,0,0,0.1))' }}>🥉</div>
              <div style={{ fontWeight: 600, fontSize: '15px', color: 'var(--ink, #19242a)', textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' }}>
                {third.candidateName}
              </div>
              <div style={{ fontSize: '12px', color: 'var(--muted, #68757a)', fontFamily: 'var(--mono)', marginTop: '4px' }}>
                {fmt(third.totalScore)} pts
              </div>
            </div>
          )}
          <div className="an-podium-bar" style={{ height: '70px', background: 'linear-gradient(180deg, #eef0eb 0%, #dcded8 100%)', borderRadius: '12px 12px 0 0', border: '1px solid rgba(255,255,255,0.6)', borderBottom: 0 }} />
        </div>

      </div>
    </section>
  );
}

export default LeaderboardPodium;
