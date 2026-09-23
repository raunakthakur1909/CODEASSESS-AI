import { describe, expect, it } from 'vitest';
import { isReviewTerminal } from './useAIReviewPolling';

describe('isReviewTerminal', () => {
  it('stops polling for completed reviews', () => {
    expect(isReviewTerminal('COMPLETED')).toBe(true);
  });

  it('stops polling for failed and skipped reviews', () => {
    expect(isReviewTerminal('FAILED')).toBe(true);
    expect(isReviewTerminal('SKIPPED')).toBe(true);
  });

  it('keeps polling while a review is queued or processing', () => {
    expect(isReviewTerminal('PENDING')).toBe(false);
    expect(isReviewTerminal('PROCESSING')).toBe(false);
  });
});