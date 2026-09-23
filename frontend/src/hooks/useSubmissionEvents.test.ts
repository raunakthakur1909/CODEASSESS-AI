/** @vitest-environment jsdom */
import { renderHook, act } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import type { Mock } from 'vitest';
import { useSubmissionEvents } from './useSubmissionEvents';
import { connectSse } from '../services/sseClient';
import { authService } from '../services/authService';
import { submissionService } from '../services/submissionService';

vi.mock('../services/sseClient');
vi.mock('../services/authService');
vi.mock('../services/submissionService');

describe('useSubmissionEvents', () => {
  let mockOnEvent: (name: string, data: string) => void;
  let mockOnError: (err: Error) => void;
  let mockAbort: Mock;

  beforeEach(() => {
    (authService.getToken as Mock).mockReturnValue('fake-token');

    mockAbort = vi.fn();
    class MockAbortController {
      abort = mockAbort;
      signal = {};
    }
    vi.stubGlobal('AbortController', MockAbortController);

    (submissionService.getSubmission as Mock).mockResolvedValue({
      id: 42,
      status: 'QUEUED',
      result: 'PENDING',
      timestamp: new Date('2026-01-01T00:00:00Z').toISOString()
    });

    (connectSse as Mock).mockImplementation((_url, _token, options) => {
      mockOnEvent = options.onEvent;
      mockOnError = options.onError;
      return Promise.resolve();
    });
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('connects and receives events, drops stale events, ignores after terminal', async () => {
    const { result } = renderHook(() => useSubmissionEvents(42));

    await act(async () => {
      await Promise.resolve();
    });
    expect(result.current.connectionState).toBe('CONNECTING');

    // 1. Send RUNNING event
    act(() => {
      mockOnEvent('submission-status', JSON.stringify({
        submissionId: 42,
        status: 'RUNNING',
        result: 'PENDING',
        timestamp: new Date('2026-01-01T00:00:01Z').toISOString()
      }));
    });
    expect(result.current.event?.status).toBe('RUNNING');

    // 2. Send Stale RUNNING event (should be ignored)
    act(() => {
      mockOnEvent('submission-status', JSON.stringify({
        submissionId: 42,
        status: 'RUNNING',
        result: 'PENDING',
        timestamp: new Date('2026-01-01T00:00:00Z').toISOString() // Older timestamp
      }));
    });
    // Still the newer timestamp
    expect(result.current.event?.timestamp).toBe(new Date('2026-01-01T00:00:01Z').toISOString());

    // 3. Send EVALUATING event
    act(() => {
      mockOnEvent('submission-status', JSON.stringify({
        submissionId: 42,
        status: 'EVALUATING',
        result: 'PENDING',
        timestamp: new Date('2026-01-01T00:00:02Z').toISOString()
      }));
    });
    expect(result.current.event?.status).toBe('EVALUATING');

    // 4. Send COMPLETED event (Terminal)
    act(() => {
      mockOnEvent('submission-status', JSON.stringify({
        submissionId: 42,
        status: 'COMPLETED',
        result: 'ACCEPTED',
        timestamp: new Date('2026-01-01T00:00:03Z').toISOString()
      }));
    });
    expect(result.current.event?.status).toBe('COMPLETED');
    expect(result.current.connectionState).toBe('DISCONNECTED');
    expect(mockAbort).toHaveBeenCalled();

    // 5. Send event after COMPLETED (even with newer timestamp, should be ignored)
    act(() => {
      mockOnEvent('submission-status', JSON.stringify({
        submissionId: 42,
        status: 'EVALUATING', // Should not revert
        result: 'PENDING',
        timestamp: new Date('2026-01-01T00:00:04Z').toISOString()
      }));
    });
    expect(result.current.event?.status).toBe('COMPLETED');
  });

  it('reconnects after disconnect with backoff', async () => {
    vi.useFakeTimers();
    const { result } = renderHook(() => useSubmissionEvents(42));

    await act(async () => {
      await Promise.resolve();
    });
    expect(result.current.connectionState).toBe('CONNECTING');

    // Simulate network error
    act(() => {
      mockOnError(new Error('Network loss'));
    });

    expect(result.current.connectionState).toBe('ERROR');
    
    const connectSpy = vi.mocked(connectSse);
    connectSpy.mockClear();

    // Advance by the next backoff (2000ms)
    await act(async () => {
      vi.advanceTimersByTime(2500);
      await Promise.resolve();
    });

    expect(connectSpy).toHaveBeenCalledTimes(1);
    vi.useRealTimers();
  });

  it('rest recovery skips SSE if DB state is already terminal', async () => {
    (submissionService.getSubmission as Mock).mockResolvedValue({
      id: 42,
      status: 'COMPLETED',
      result: 'WRONG_ANSWER',
      timestamp: new Date('2026-01-01T00:00:00Z').toISOString()
    });

    const { result } = renderHook(() => useSubmissionEvents(42));

    // Wait for microtasks
    await act(async () => {
      await Promise.resolve();
    });

    expect(result.current.event?.status).toBe('COMPLETED');

    // SSE should never connect because it's already terminal
    expect(connectSse).not.toHaveBeenCalled();
    expect(result.current.connectionState).toBe('DISCONNECTED');
  });
});
