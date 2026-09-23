import { useState, useEffect, useRef, useCallback } from 'react';
import type { AssessmentEvent } from '../types/assessmentEvents';
import { connectSse } from '../services/sseClient';
import { authService } from '../services/authService';

export type SSEConnectionState =
  | 'CONNECTING'
  | 'CONNECTED'
  | 'DISCONNECTED'
  | 'ERROR';

export function useAssessmentEvents(assessmentId: number | null) {
  const [event, setEvent] = useState<AssessmentEvent | null>(null);
  const [connectionState, setConnectionState] = useState<SSEConnectionState>('DISCONNECTED');
  const [error, setError] = useState<Error | null>(null);

  const abortControllerRef = useRef<AbortController | null>(null);
  const reconnectTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const backoffRef = useRef(1000);
  const isTerminalRef = useRef(false);

  const connect = useCallback(async () => {
    if (!assessmentId || isTerminalRef.current) return;

    setEvent(null);
    setConnectionState('CONNECTING');
    setError(null);

    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
    
    // REST Recovery before connecting SSE
    try {
      const res = await fetch(`/api/assessments/${assessmentId}`, {
        headers: {
          'Authorization': `Bearer ${authService.getToken()}`
        }
      });
      if (!res.ok) throw new Error('Failed to fetch assessment');
      const restData: any = await res.json();
      
      const mappedEvent: AssessmentEvent = {
        assessmentId: restData.id || assessmentId,
        type: 'ASSESSMENT_UPDATED',
        status: restData.status,
        serverTime: new Date().toISOString(),
        expiresAt: restData.startedAt 
          ? new Date(new Date(restData.startedAt).getTime() + restData.durationMinutes * 60000).toISOString()
          : null
      };
      
      if (mappedEvent.status === 'CLOSED') {
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

    await connectSse(`/api/events/assessments/${assessmentId}`, token, {
      signal: controller.signal,
      onEvent: (name, data) => {
        if (name === 'assessment-status') {
          try {
            const parsed = JSON.parse(data) as AssessmentEvent;
            setEvent(prev => {
              if (prev && (prev.status === 'CLOSED' || prev.type === 'ASSESSMENT_EXPIRED')) {
                return prev; // Ignore any event if already terminal
              }
              if (prev && new Date(parsed.serverTime).getTime() <= new Date(prev.serverTime).getTime()) {
                return prev; // Ignore stale or duplicate events
              }
              return parsed;
            });

            setConnectionState('CONNECTED');
            backoffRef.current = 1000; // Reset backoff on successful event

            if (parsed.type === 'ASSESSMENT_EXPIRED' || parsed.status === 'CLOSED') {
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

  }, [assessmentId]);

  const connectRef = useRef(connect);
  useEffect(() => {
    connectRef.current = connect;
  }, [connect]);

  useEffect(() => {
    isTerminalRef.current = false;
    backoffRef.current = 1000;

    if (assessmentId) {
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
    };
  }, [assessmentId, connect]);

  const reconnect = useCallback(() => {
    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current);
      reconnectTimeoutRef.current = null;
    }
    backoffRef.current = 1000; 
    isTerminalRef.current = false; 
    connect();
  }, [connect]);

  return { event, connectionState, error, reconnect };
}
