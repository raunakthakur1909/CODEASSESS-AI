import type { LeaderboardEntry } from '../../types/analytics';

interface LeaderboardTableProps {
  entries: LeaderboardEntry[];
}

function fmt(n: number | null, decimals = 1): string {
  if (n === null || n === undefined) return '—';
  return n.toFixed(decimals);
}

/**
 * Main leaderboard table.
 * 
 * Includes: Rank, Candidate Name, Total Score, Accepted count (out of Total Problems), and Average Time.
 */
function LeaderboardTable({ entries }: LeaderboardTableProps) {
  if (entries.length === 0) {
    return null;
  }

  return (
    <section className="an-section" aria-labelledby="leaderboard-table-heading" style={{ marginTop: '32px' }}>
      <div className="an-section-heading">
        <div className="an-section-marker" aria-hidden="true" />
        <h2 id="leaderboard-table-heading">Candidate Rankings</h2>
      </div>

      <div className="an-table-wrap">
        <table className="an-table" aria-label="Leaderboard rankings">
          <caption className="sr-only">
            Ranked list of candidates by score, accepted submissions, and execution time.
          </caption>
          <thead>
            <tr>
              <th scope="col" style={{ width: '60px' }}>Rank</th>
              <th scope="col">Candidate</th>
              <th scope="col" className="an-th-right">Score</th>
              <th scope="col" className="an-th-right">Accepted</th>
              <th scope="col" className="an-th-right">Avg time</th>
            </tr>
          </thead>
          <tbody>
            {entries.map((entry) => {
              // Highlight top 3 visually if we want
              const isTop3 = entry.rank !== undefined && entry.rank <= 3;
              
              return (
                <tr key={entry.candidateId}>
                  <td>
                    <span 
                      style={{ 
                        fontWeight: isTop3 ? 700 : 400,
                        color: isTop3 ? 'var(--ink, #19242a)' : 'inherit'
                      }}
                    >
                      #{entry.rank}
                    </span>
                  </td>
                  <td>
                    <span className="an-problem-title">{entry.candidateName}</span>
                  </td>
                  <td className="an-td-right" style={{ fontWeight: 600 }}>
                    {fmt(entry.totalScore)}
                  </td>
                  <td className="an-td-right">
                    {entry.acceptedCount} / {entry.totalProblems}
                  </td>
                  <td className="an-td-right">
                    {entry.averageExecutionTimeMs !== null 
                      ? `${fmt(entry.averageExecutionTimeMs, 0)} ms` 
                      : <span className="an-null">—</span>}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </section>
  );
}

export default LeaderboardTable;
