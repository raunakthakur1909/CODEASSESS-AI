/** @vitest-environment jsdom */
import { renderHook, act } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import type { Mock } from 'vitest';
import { useAssessmentEvents } from './useAssessmentEvents';
import { connectSse } from '../services/sseClient';
import { authService } from '../services/authService';

vi.mock('../services/sseClient');
vi.mock('../services/authService');

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

describe('useAssessmentEvents', () => {
  let mockOnEvent: (name: string, data: string) => void;
  let mockAbort: Mock;

  beforeEach(() => {
    (authService.getToken as Mock).mockReturnValue('fake-token');

    mockAbort = vi.fn();
    class MockAbortController {
      abort = mockAbort;
      signal = {};
    }
    vi.stubGlobal('AbortController', MockAbortController);

    mockFetch.mockResolvedValue({
      ok: true,
      json: async () => ({
        id: 10,
        status: 'PUBLISHED',
        startedAt: new Date('2026-01-01T00:00:00Z').toISOString(),
        durationMinutes: 60
      })
    });

    (connectSse as Mock).mockImplementation((_url, _token, options) => {
      mockOnEvent = options.onEvent;
      return Promise.resolve();
    });
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('connects and receives events, drops stale events, ignores after terminal', async () => {
    const { result } = renderHook(() => useAssessmentEvents(10));

    // Allow microtasks (REST recovery mock) to flush so CONNECTING is set
    await act(async () => {
      await Promise.resolve();
    });
    expect(result.current.connectionState).toBe('CONNECTING');

    // 1. Send EXPIRING event
    act(() => {
      mockOnEvent('assessment-status', JSON.stringify({
        assessmentId: 10,
        type: 'ASSESSMENT_EXPIRING',
        status: 'PUBLISHED',
        serverTime: new Date('2026-01-01T00:55:00Z').toISOString()
      }));
    });
    expect(result.current.event?.type).toBe('ASSESSMENT_EXPIRING');

    // 2. Send Stale event (should be ignored)
    act(() => {
      mockOnEvent('assessment-status', JSON.stringify({
        assessmentId: 10,
        type: 'ASSESSMENT_UPDATED',
        status: 'PUBLISHED',
        serverTime: new Date('2026-01-01T00:00:00Z').toISOString() // Older timestamp
      }));
    });
    // Still the newer type
    expect(result.current.event?.type).toBe('ASSESSMENT_EXPIRING');

    // 3. Send EXPIRED event (Terminal)
    act(() => {
      mockOnEvent('assessment-status', JSON.stringify({
        assessmentId: 10,
        type: 'ASSESSMENT_EXPIRED',
        status: 'CLOSED',
        serverTime: new Date('2026-01-01T01:00:00Z').toISOString()
      }));
    });
    expect(result.current.event?.status).toBe('CLOSED');
    expect(result.current.connectionState).toBe('DISCONNECTED');
    expect(mockAbort).toHaveBeenCalled();

    // 4. Send event after CLOSED (even with newer timestamp, should be ignored)
    act(() => {
      mockOnEvent('assessment-status', JSON.stringify({
        assessmentId: 10,
        type: 'ASSESSMENT_UPDATED', 
        status: 'PUBLISHED', // Should not revert
        serverTime: new Date('2026-01-01T01:05:00Z').toISOString()
      }));
    });
    expect(result.current.event?.status).toBe('CLOSED');
  });

  it('rest recovery skips SSE if DB state is already terminal', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        id: 10,
        status: 'CLOSED',
        startedAt: new Date('2026-01-01T00:00:00Z').toISOString(),
        durationMinutes: 60
      })
    });

    const { result } = renderHook(() => useAssessmentEvents(10));

    // Wait for microtasks
    await act(async () => {
      await Promise.resolve();
    });

    expect(result.current.event?.status).toBe('CLOSED');

    // SSE should never connect because it's already terminal
    expect(connectSse).not.toHaveBeenCalled();
    expect(result.current.connectionState).toBe('DISCONNECTED');
  });
});
