import { describe, it, expect } from 'vitest';

// ---------------------------------------------------------------------------
// App routing — Leaderboard query string parsing
// ---------------------------------------------------------------------------

describe('Leaderboard route query string parsing', () => {
  it('correctly routes valid numeric assessmentId for leaderboard', () => {
    const search = '?analytics=leaderboard&assessmentId=42';
    const params = new URLSearchParams(search);

    const type = params.get('analytics');
    const id = params.get('assessmentId');

    const shouldRoute =
      type === 'leaderboard' &&
      id &&
      Number.isInteger(Number(id)) &&
      Number(id) > 0;

    expect(shouldRoute).toBe(true);
  });

  it('rejects invalid assessmentId (e.g. text)', () => {
    const search = '?analytics=leaderboard&assessmentId=abc';
    const params = new URLSearchParams(search);

    const type = params.get('analytics');
    const id = params.get('assessmentId');

    const shouldRoute =
      type === 'leaderboard' &&
      id &&
      Number.isInteger(Number(id)) &&
      Number(id) > 0;

    expect(shouldRoute).toBe(false);
  });
});
