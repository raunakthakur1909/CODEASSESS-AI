import { useEffect, useState } from 'react';
import type { CodeReview, ReviewStatus } from '../types/aiReview';
import { aiReviewService } from '../services/aiReviewService';

const DEFAULT_INTERVAL_MS = 5000;
const DEFAULT_TIMEOUT_MS = 120000;

export interface AIReviewPollingOptions {
  enabled?: boolean;
  intervalMs?: number;
  timeoutMs?: number;
}

export interface AIReviewPollingResult {
  review: CodeReview | null;
  isPolling: boolean;
  error: string | null;
  retry: () => void;
}

export function isReviewTerminal(status: ReviewStatus): boolean {
  return status === 'COMPLETED' || status === 'FAILED' || status === 'SKIPPED';
}

export function useAIReviewPolling(
  submissionId: number | null | undefined,
  options: AIReviewPollingOptions = {},
): AIReviewPollingResult {
  const {
    enabled = true,
    intervalMs = DEFAULT_INTERVAL_MS,
    timeoutMs = DEFAULT_TIMEOUT_MS,
  } = options;
  const [review, setReview] = useState<CodeReview | null>(null);
  const [isPolling, setIsPolling] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [retryKey, setRetryKey] = useState(0);
  
  // Track previous dependencies to reset state during render (React recommended pattern)
  const [prevSubmissionId, setPrevSubmissionId] = useState(submissionId);
  const [prevRetryKey, setPrevRetryKey] = useState(retryKey);
  const [prevEnabled, setPrevEnabled] = useState(enabled);

  if (submissionId !== prevSubmissionId || retryKey !== prevRetryKey || enabled !== prevEnabled) {
    setPrevSubmissionId(submissionId);
    setPrevRetryKey(retryKey);
    setPrevEnabled(enabled);
    setReview(null);
    setIsPolling(!!(submissionId && enabled));
    setError(null);
  }

  useEffect(() => {
    if (!submissionId || !enabled) {
      return;
    }

    let cancelled = false;
    let timeoutHandle: ReturnType<typeof setTimeout> | undefined;
    const startedAt = Date.now();

    const stop = () => {
      if (!cancelled) {
        setIsPolling(false);
      }
    };

    const poll = async (): Promise<void> => {
      if (cancelled) {
        return;
      }

      if (Date.now() - startedAt >= timeoutMs) {
        setError('AI review polling timed out. Please try again.');
        stop();
        return;
      }

      try {
        const nextReview = await aiReviewService.getReviewBySubmission(submissionId);
        if (cancelled) {
          return;
        }

        setReview(nextReview);

        if (isReviewTerminal(nextReview.status)) {
          if (nextReview.status === 'FAILED') {
            setError('The AI review failed. Please try again.');
          }
          stop();
          return;
        }

        timeoutHandle = setTimeout(() => {
          void poll();
        }, intervalMs);
      } catch (pollError) {
        if (cancelled) {
          return;
        }

        setError(pollError instanceof Error ? pollError.message : 'Unable to load the AI review.');
        stop();
      }
    };

    void poll();

    return () => {
      cancelled = true;
      if (timeoutHandle !== undefined) {
        clearTimeout(timeoutHandle);
      }
    };
  }, [enabled, intervalMs, retryKey, submissionId, timeoutMs]);

  const retry = () => {
    if (review?.status === 'FAILED' && submissionId) {
      void aiReviewService.requestReview(submissionId).catch(() => {});
    }
    setRetryKey((currentKey) => currentKey + 1);
  };

  return { review, isPolling, error, retry };
}
