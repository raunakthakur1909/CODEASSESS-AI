// @vitest-environment jsdom
import { render, screen, fireEvent, cleanup } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import SubmissionResultPage from './SubmissionResultPage';
import { useSubmissionEvents } from '../hooks/useSubmissionEvents';

vi.mock('../hooks/useSubmissionEvents');
vi.mock('../services/aiReviewService');

describe('SubmissionResultPage Live UI', () => {
  let mockEvent: any = null;
  let mockConnectionState = 'DISCONNECTED';
  let mockError: Error | null = null;
  let mockReconnect: any;
  let mockOnBack: any;

  afterEach(() => {
    cleanup();
  });

  beforeEach(() => {
    mockEvent = null;
    mockConnectionState = 'DISCONNECTED';
    mockError = null;
    mockReconnect = vi.fn();
    mockOnBack = vi.fn();

    vi.mocked(useSubmissionEvents).mockImplementation(() => ({
      event: mockEvent,
      connectionState: mockConnectionState as any,
      error: mockError,
      reconnect: mockReconnect
    }));
  });

  const renderComponent = () => render(<SubmissionResultPage submissionId={42} onBack={mockOnBack} />);

  it('renders initial QUEUED loading state', () => {
    mockConnectionState = 'CONNECTING';
    mockEvent = { status: 'QUEUED' };
    renderComponent();
    
    expect(screen.getByText('Submission queued for execution...')).toBeTruthy();
    expect(screen.getByText(/Live status: QUEUED/)).toBeTruthy();
  });

  it('renders RUNNING → EVALUATING → COMPLETED transitions', () => {
    const { rerender } = render(<SubmissionResultPage submissionId={42} onBack={mockOnBack} />);

    // RUNNING
    mockConnectionState = 'CONNECTED';
    mockEvent = { status: 'RUNNING' };
    rerender(<SubmissionResultPage submissionId={42} onBack={mockOnBack} />);
    expect(screen.getByText('Code is currently executing...')).toBeTruthy();

    // EVALUATING
    mockEvent = { status: 'EVALUATING' };
    rerender(<SubmissionResultPage submissionId={42} onBack={mockOnBack} />);
    expect(screen.getByText('Test cases are being evaluated...')).toBeTruthy();

    // COMPLETED
    mockEvent = {
      status: 'COMPLETED',
      score: 100,
      passedTests: 10,
      totalTests: 10,
      result: 'ACCEPTED',
      timestamp: '2023-01-01T00:00:00Z'
    };
    rerender(<SubmissionResultPage submissionId={42} onBack={mockOnBack} />);
    expect(screen.getByText('Your submission is ready for review.')).toBeTruthy();
    expect(screen.getByText('100')).toBeTruthy(); // Score
    expect(screen.getByText('ACCEPTED')).toBeTruthy(); // Result
    expect(screen.getByText('10 / 10 passed')).toBeTruthy(); // Tests
  });

  it('renders FAILED state correctly', () => {
    mockEvent = {
      status: 'FAILED',
      result: 'Compilation Error'
    };
    renderComponent();

    expect(screen.getByText('Execution Failed')).toBeTruthy();
    expect(screen.getByText('Compilation Error')).toBeTruthy();
  });

  it('shows error state and reconnect action on SSE disconnect', () => {
    mockConnectionState = 'ERROR';
    mockError = new Error('Connection lost') as any;
    mockEvent = { status: 'RUNNING' };
    
    renderComponent();
    
    expect(screen.getByText('Connection lost. Reconnecting...')).toBeTruthy();
    
    const reconnectBtn = screen.getByRole('button', { name: 'Reconnect now' });
    fireEvent.click(reconnectBtn);
    expect(mockReconnect).toHaveBeenCalled();
  });

  it('renders already-completed submission correctly', () => {
    mockConnectionState = 'DISCONNECTED';
    mockEvent = {
      status: 'COMPLETED',
      score: 80,
      passedTests: 8,
      totalTests: 10,
      result: 'WRONG_ANSWER',
      timestamp: '2023-01-01T00:00:00Z'
    };
    renderComponent();

    expect(screen.getByText('Your submission is ready for review.')).toBeTruthy();
    expect(screen.getByText('80')).toBeTruthy();
    expect(screen.getByText('WRONG_ANSWER')).toBeTruthy();
    expect(screen.getByText('8 / 10 passed')).toBeTruthy();
  });

  it('responsive rendering - hero section exists', () => {
    mockConnectionState = 'CONNECTING';
    renderComponent();
    expect(screen.getByText('Connecting...')).toBeTruthy();
    expect(document.querySelector('.submission-result-hero')).toBeTruthy();
  });
});
