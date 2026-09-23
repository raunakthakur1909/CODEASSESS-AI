import type { ProblemSummary } from '../../types/analytics';

interface ProblemPerformanceTableProps {
  problems: ProblemSummary[];
}

/** Format a nullable number to n decimal places */
function fmt(n: number | null, decimals = 1): string {
  if (n === null || n === undefined) return '—';
  return n.toFixed(decimals);
}

/** Returns a CSS class that colour-codes acceptance rate */
function rateClass(rate: number | null): string {
  if (rate === null) return 'an-null';
  const pct = rate * 100;
  if (pct >= 60) return 'an-rate--high';
  if (pct >= 30) return 'an-rate--mid';
  return 'an-rate--low';
}

/**
 * Semantic <table> listing per-problem performance statistics.
 *
 * Columns: Problem | Attempts | Accepted | Acceptance Rate | Avg Score
 *
 * Accessibility:
 *   – <th scope="col"> on all headers
 *   – <caption> for screen readers
 *   – numeric columns are right-aligned (aria-label carries the value)
 */
function ProblemPerformanceTable({ problems }: ProblemPerformanceTableProps) {
  return (
    <section className="an-section" aria-labelledby="prob-table-heading">
      <div className="an-section-heading">
        <div className="an-section-marker" aria-hidden="true" />
        <h2 id="prob-table-heading">Problem performance</h2>
      </div>

      <div className="an-table-wrap">
        <table className="an-table" aria-label="Problem performance statistics">
          <caption className="sr-only">
            Per-problem acceptance rates and average scores
          </caption>
          <thead>
            <tr>
              <th scope="col">Problem</th>
              <th scope="col" className="an-th-right">Attempts</th>
              <th scope="col" className="an-th-right">Accepted</th>
              <th scope="col" className="an-th-right">Acceptance rate</th>
              <th scope="col" className="an-th-right">Avg score</th>
            </tr>
          </thead>
          <tbody>
            {problems.map((p) => {
              const rate = p.acceptanceRate;
              return (
                <tr key={p.problemId}>
                  <td>
                    <span className="an-problem-title">{p.title}</span>
                    <br />
                    <span className="an-null">ID #{p.problemId}</span>
                  </td>
                  <td className="an-td-right">{p.totalSubmissions}</td>
                  <td className="an-td-right">{p.acceptedSubmissions}</td>
                  <td className={`an-td-right ${rateClass(rate)}`}>
                    {rate !== null ? `${(rate * 100).toFixed(1)}%` : <span className="an-null">—</span>}
                  </td>
                  <td className="an-td-right">{fmt(p.averageScore)}</td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </section>
  );
}

export default ProblemPerformanceTable;
