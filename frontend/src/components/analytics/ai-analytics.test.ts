import { describe, it, expect } from 'vitest';

// ---------------------------------------------------------------------------
// App routing — AI Analytics query string parsing
// ---------------------------------------------------------------------------

describe('AI Analytics route query string parsing', () => {
  it('correctly routes valid numeric assessmentId for AI analytics', () => {
    const search = '?analytics=ai&assessmentId=42';
    const params = new URLSearchParams(search);

    const type = params.get('analytics');
    const id = params.get('assessmentId');

    const shouldRoute =
      type === 'ai' &&
      id &&
      Number.isInteger(Number(id)) &&
      Number(id) > 0;

    expect(shouldRoute).toBe(true);
  });

  it('rejects invalid assessmentId (e.g. text) for AI analytics', () => {
    const search = '?analytics=ai&assessmentId=abc';
    const params = new URLSearchParams(search);

    const type = params.get('analytics');
    const id = params.get('assessmentId');

    const shouldRoute =
      type === 'ai' &&
      id &&
      Number.isInteger(Number(id)) &&
      Number(id) > 0;

    expect(shouldRoute).toBe(false);
  });
});
