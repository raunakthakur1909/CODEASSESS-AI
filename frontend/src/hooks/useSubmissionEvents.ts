import { useState, useEffect, useRef, useCallback } from 'react';
import type { SubmissionEvent } from '../types/submissionEvents';
import { connectSse } from '../services/sseClient';
import { authService } from '../services/authService';
import { submissionService } from '../services/submissionService';

export type SSEConnectionState =
  | 'CONNECTING'
  | 'CONNECTED'
  | 'DISCONNECTED'
  | 'ERROR';

/*
 * Lifecycle ordering used to decide whether an incoming event actually moves
 * the submission forward. Timestamps alone are not sufficient: they are
 * serialised to millisecond precision on the client, so two transitions
 * emitted within the same millisecond compare as equal and the later one
 * would be discarded as a duplicate.
 */
const STATUS_RANK: Record<string, number> = {
  QUEUED: 0,
  RUNNING: 1,
  EVALUATING: 2,
  COMPLETED: 3,
  FAILED: 3
};

function isProgress(next: SubmissionEvent, prev: SubmissionEvent): boolean {
  const nextRank = STATUS_RANK[next.status] ?? -1;
  const prevRank = STATUS_RANK[prev.status] ?? -1;

  if (nextRank !== prevRank) {
    return nextRank > prevRank;
  }

  // Same status: EVALUATING streams per-test-case progress.
  if ((next.currentTestCase ?? -1) !== (prev.currentTestCase ?? -1)) {
    return (next.currentTestCase ?? -1) > (prev.currentTestCase ?? -1);
  }

  if ((next.passedTests ?? -1) !== (prev.passedTests ?? -1)) {
    return (next.passedTests ?? -1) > (prev.passedTests ?? -1);
  }

  return new Date(next.timestamp).getTime() > new Date(prev.timestamp).getTime();
}

export function useSubmissionEvents(submissionId: number | null) {
  const [event, setEvent] = useState<SubmissionEvent | null>(null);
  const [connectionState, setConnectionState] = useState<SSEConnectionState>('DISCONNECTED');
  const [error, setError] = useState<Error | null>(null);

  const abortControllerRef = useRef<AbortController | null>(null);
  const reconnectTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const backoffRef = useRef(1000);
  const isTerminalRef = useRef(false);

  const connect = useCallback(async () => {
    if (!submissionId || isTerminalRef.current) return;

    setEvent(null);
    setConnectionState('CONNECTING');
    setError(null);

    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
    
    // REST Recovery before connecting SSE
    try {
      const restData: any = await submissionService.getSubmission(submissionId);
      // Backend /api/submissions/{id} typically returns standard submission response
      // We map it to the SubmissionEvent format for consistency
      const mappedEvent: SubmissionEvent = {
        submissionId: restData.submissionId || restData.id || submissionId,
        status: restData.status,
        result: restData.result,
        score: restData.score,
        passedTests: restData.passedTests,
        totalTests: restData.totalTests,
        currentTestCase: restData.currentTestCase,
        timestamp: restData.timestamp || new Date().toISOString()
      };
      
      if (mappedEvent.status === 'COMPLETED' || mappedEvent.status === 'FAILED') {
        setEvent(mappedEvent);
        isTerminalRef.current = true;
        setConnectionState('DISCONNECTED');
        return;
      }
    } catch (e) {
      console.warn("REST recovery failed", e);
    }

    const token = authService.getToken();
    if (!token) {
      const err = new Error("No token available");
      setError(err);
      setConnectionState('ERROR');
      return;
    }

    const controller = new AbortController();
    abortControllerRef.current = controller;

    await connectSse(`/api/events/submissions/${submissionId}`, token, {
      signal: controller.signal,
      onEvent: (name, data) => {
        if (name === 'submission-status') {
          try {
            const parsed = JSON.parse(data) as SubmissionEvent;
            setEvent(prev => {
              if (prev && (prev.status === 'COMPLETED' || prev.status === 'FAILED')) {
                return prev; // Ignore any event if already terminal
              }
              if (prev && !isProgress(parsed, prev)) {
                return prev; // Ignore stale or duplicate events
              }
              return parsed;
            });

            setConnectionState('CONNECTED');
            backoffRef.current = 1000; // Reset backoff on successful event

            if (parsed.status === 'COMPLETED' || parsed.status === 'FAILED') {
              isTerminalRef.current = true;
              controller.abort();
              setConnectionState('DISCONNECTED');
            }
          } catch (_e) {
            console.error("Malformed JSON in SSE", data);
          }
        }
      },
      onError: (err) => {
        setConnectionState('ERROR');
        setError(err);
        
        // Exponential backoff
        const nextDelay = Math.min(backoffRef.current * 2, 30000);
        backoffRef.current = nextDelay;

        reconnectTimeoutRef.current = setTimeout(() => {
          connectRef.current?.();
        }, nextDelay);
      }
    });

  }, [submissionId]);

  const connectRef = useRef(connect);
  useEffect(() => {
    connectRef.current = connect;
  }, [connect]);

  useEffect(() => {
    isTerminalRef.current = false;
    backoffRef.current = 1000;

    if (submissionId) {
      connect();
    }

    return () => {
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
        abortControllerRef.current = null;
      }
      if (reconnectTimeoutRef.current) {
        clearTimeout(reconnectTimeoutRef.current);
        reconnectTimeoutRef.current = null;
      }
      // Connection is effectively disconnected when aborted, UI state will just linger or be overwritten on next connect
    };
  }, [submissionId, connect]);

  const reconnect = useCallback(() => {
    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current);
      reconnectTimeoutRef.current = null;
    }
    backoffRef.current = 1000; // Reset for manual reconnect
    isTerminalRef.current = false; // Allow reconnection attempt even if terminal just in case
    connect();
  }, [connect]);

  return { event, connectionState, error, reconnect };
}
