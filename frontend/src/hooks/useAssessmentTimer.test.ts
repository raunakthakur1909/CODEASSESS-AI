// @vitest-environment jsdom
import { renderHook, act } from '@testing-library/react';
import { useAssessmentTimer } from './useAssessmentTimer';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';

describe('useAssessmentTimer', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2023-01-01T12:00:00Z'));
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('handles null assessment gracefully', () => {
    const { result } = renderHook(() => useAssessmentTimer(null, vi.fn()));
    expect(result.current.remainingSeconds).toBe(0);
    expect(result.current.isExpired).toBe(true);
  });

  it('handles assessment with missing startedAt gracefully', () => {
    const { result } = renderHook(() => useAssessmentTimer({ expiresAt: '2023-01-01T13:00:00Z', status: 'IN_PROGRESS' }, vi.fn()));
    expect(result.current.remainingSeconds).toBe(3600);
    expect(result.current.isExpired).toBe(false);
  });

  it('calculates remaining time correctly', () => {
    const assessment = {
      expiresAt: '2023-01-01T12:30:00Z',
      status: 'IN_PROGRESS'
    };
    const onExpire = vi.fn();
    
    // 12:00 - 11:30 = 30 mins passed, 30 mins remaining = 1800s
    const { result } = renderHook(() => useAssessmentTimer(assessment, onExpire));
    
    expect(result.current.remainingSeconds).toBe(1800);
    expect(result.current.formattedTime).toBe('30:00');
    expect(result.current.isWarning).toBe(false);
    expect(result.current.isExpired).toBe(false);
  });

  it('ticks down correctly over time', () => {
    const assessment = {
      expiresAt: '2023-01-01T12:30:00Z',
      status: 'IN_PROGRESS'
    };
    const onExpire = vi.fn();
    
    const { result } = renderHook(() => useAssessmentTimer(assessment, onExpire));
    
    expect(result.current.remainingSeconds).toBe(1800);
    
    act(() => {
      vi.advanceTimersByTime(1000);
    });
    
    expect(result.current.remainingSeconds).toBe(1799);
  });

  it('sets warning state when under 5 minutes', () => {
    const assessment = {
      expiresAt: '2023-01-01T12:05:00Z',
      status: 'IN_PROGRESS'
    };
    const onExpire = vi.fn();
    
    const { result } = renderHook(() => useAssessmentTimer(assessment, onExpire));
    
    expect(result.current.remainingSeconds).toBe(300);
    expect(result.current.isWarning).toBe(true);
    expect(result.current.isExpired).toBe(false);
  });

  it('triggers onExpire and sets expired state when time reaches 0', () => {
    const assessment = {
      expiresAt: '2023-01-01T12:00:01Z',
      status: 'IN_PROGRESS'
    };
    const onExpire = vi.fn();
    
    const { result } = renderHook(() => useAssessmentTimer(assessment, onExpire));
    
    expect(result.current.remainingSeconds).toBe(1); // 1 sec left
    expect(result.current.isExpired).toBe(false);
    
    act(() => {
      vi.advanceTimersByTime(1000);
    });
    
    expect(result.current.remainingSeconds).toBe(0);
    expect(result.current.isExpired).toBe(true);
    expect(onExpire).toHaveBeenCalledTimes(1);
    
    // Additional ticks should not trigger it again
    act(() => {
      vi.advanceTimersByTime(5000);
    });
    
    expect(result.current.remainingSeconds).toBe(0);
    expect(onExpire).toHaveBeenCalledTimes(1);
  });

  it('handles already expired assessment on mount', () => {
    const assessment = {
      expiresAt: '2023-01-01T11:00:00Z',
      status: 'IN_PROGRESS'
    };
    const onExpire = vi.fn();
    
    const { result } = renderHook(() => useAssessmentTimer(assessment, onExpire));
    
    expect(result.current.remainingSeconds).toBe(0);
    expect(result.current.isExpired).toBe(true);
    expect(onExpire).toHaveBeenCalledTimes(1);
  });
});
