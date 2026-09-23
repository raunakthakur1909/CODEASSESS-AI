import { useState, useEffect, useRef } from 'react';

export interface UseAssessmentTimerResult {
  remainingSeconds: number;
  formattedTime: string;
  isWarning: boolean;
  isExpired: boolean;
}

export function useAssessmentTimer(
  attempt: { expiresAt: string, status: string } | null,
  onExpire: () => void
): UseAssessmentTimerResult {
  const [remainingSeconds, setRemainingSeconds] = useState(() => {
    if (!attempt || !attempt.expiresAt) {
      return 0;
    }
    if (attempt.status === 'EXPIRED') return 0;
    
    let expiresStr = attempt.expiresAt;
    if (!expiresStr.endsWith('Z')) {
      expiresStr += 'Z';
    }
    const endAt = new Date(expiresStr).getTime();
    const now = Date.now();
    const diff = endAt - now;
    return Math.max(0, Math.floor(diff / 1000));
  });

  const onExpireRef = useRef(onExpire);

  // Keep ref fresh so interval always calls the latest callback
  useEffect(() => {
    onExpireRef.current = onExpire;
  }, [onExpire]);

  useEffect(() => {
    if (!attempt || !attempt.expiresAt) {
      setRemainingSeconds(0);
      return;
    }

    if (attempt.status === 'EXPIRED') {
      setRemainingSeconds(0);
      return;
    }
    
    let expiresStr = attempt.expiresAt;
    if (!expiresStr.endsWith('Z')) {
      expiresStr += 'Z';
    }
    const endAt = new Date(expiresStr).getTime();

    const calculateRemaining = () => {
      const now = Date.now();
      const diff = endAt - now;
      return Math.max(0, Math.floor(diff / 1000));
    };

    const initial = calculateRemaining();
    setRemainingSeconds(initial);

    if (initial <= 0) {
      onExpireRef.current();
      return;
    }

    const intervalId = setInterval(() => {
      const current = calculateRemaining();
      setRemainingSeconds(current);

      if (current <= 0) {
        clearInterval(intervalId);
        onExpireRef.current();
      }
    }, 1000);

    return () => {
      clearInterval(intervalId);
    };
  }, [attempt?.expiresAt, attempt?.status]);

  const hrs = Math.floor(remainingSeconds / 3600);
  const mins = Math.floor((remainingSeconds % 3600) / 60);
  const secs = remainingSeconds % 60;

  const formattedTime = [
    hrs > 0 ? hrs.toString().padStart(2, '0') : null,
    mins.toString().padStart(2, '0'),
    secs.toString().padStart(2, '0')
  ].filter(Boolean).join(':');

  return {
    remainingSeconds,
    formattedTime,
    isWarning: remainingSeconds > 0 && remainingSeconds <= 300, // 5 minutes warning
    isExpired: remainingSeconds <= 0
  };
}
