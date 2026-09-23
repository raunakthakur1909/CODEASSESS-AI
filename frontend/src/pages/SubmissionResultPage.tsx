import { useState, useEffect } from 'react';
import AIReviewPage from './AIReviewPage';
import { aiReviewService } from '../services/aiReviewService';
import { useSubmissionEvents } from '../hooks/useSubmissionEvents';
import type { SubmissionEventStatus } from '../types/submissionEvents';
import LiveExecutionProgress from '../components/assessment/LiveExecutionProgress';
import './review.css';

interface SubmissionResultPageProps {
  submissionId: number;
  onBack: () => void;
}

function getStatusMessage(status?: SubmissionEventStatus) {
  switch (status) {
    case 'QUEUED':
      return 'Submission queued for execution...';
    case 'RUNNING':
      return 'Code is currently executing...';
    case 'EVALUATING':
      return 'Test cases are being evaluated...';
    case 'COMPLETED':
      return 'Evaluation completed.';
    case 'FAILED':
      return 'Execution failed.';
    default:
      return 'Connecting...';
  }
}

function SubmissionResultPage({ submissionId, onBack }: SubmissionResultPageProps) {
  const [viewingReview, setViewingReview] = useState(false);
  const [existingReview, setExistingReview] = useState<boolean | null>(null); // null = loading, true = exists, false = doesn't exist
  const [isRequesting, setIsRequesting] = useState(false);
  const [reviewError, setReviewError] = useState<string | null>(null);

  const { event, connectionState, error: sseError, reconnect } = useSubmissionEvents(submissionId);

  // Check if review already exists on mount
  useEffect(() => {
    const checkExistingReview = async () => {
      try {
        await aiReviewService.getReviewBySubmission(submissionId);
        setExistingReview(true);
      } catch (error) {
        // 404 means no review exists yet
        setExistingReview(false);
      }
    };

    void checkExistingReview();
  }, [submissionId]);

  const requestReview = async () => {
    setIsRequesting(true);
    setReviewError(null);

    try {
      await aiReviewService.requestReview(submissionId);
      setExistingReview(true);
      setViewingReview(true);
    } catch (requestError) {
      setReviewError(requestError instanceof Error ? requestError.message : 'Unable to request an AI review.');
    } finally {
      setIsRequesting(false);
    }
  };

  const viewReview = () => {
    setViewingReview(true);
  };

  if (viewingReview) {
    return <AIReviewPage submissionId={submissionId} />;
  }

  const isTerminal = event?.status === 'COMPLETED' || event?.status === 'FAILED';

  /*
   * Before the first event resolves, connectionState is still DISCONNECTED.
   * Treat that as "loading" so the page does not momentarily claim the
   * submission is ready for review while its status is still unknown.
   */
  const showLiveBanner = !isTerminal && (connectionState !== 'DISCONNECTED' || !event);

  return (
    <main className="submission-result-shell">
      <button className="back-button" type="button" onClick={onBack}>← Back to dashboard</button>
      
      <section className="submission-result-hero">
        <p className="eyebrow">Submission {submissionId} / Execution result</p>
        
        {showLiveBanner ? (
          <div>
            <h1>{getStatusMessage(event?.status)}</h1>
            <div className="review-live-banner">
              <div className="loader loader--small"></div>
              <span>Live status: {event?.status || connectionState}</span>
            </div>
            {sseError && (
              <div className="review-inline-error" role="alert">
                Connection lost. Reconnecting...
                <button className="inline-retry" type="button" onClick={reconnect}>Reconnect now</button>
              </div>
            )}
            <LiveExecutionProgress event={event} />
          </div>
        ) : event?.status === 'FAILED' ? (
          <div className="review-state review-state--error">
            <div className="state-icon">!</div>
            <h2>Execution Failed</h2>
            <p>{event.result || 'An unexpected error occurred during execution.'}</p>
            <button className="review-button" type="button" onClick={onBack}>Return to dashboard</button>
          </div>
        ) : (
          <>
            <h1>Your submission is ready for review.</h1>
            <p>Execution results remain the source of truth. Request an AI review when you want additional feedback on quality, complexity, and security.</p>
          </>
        )}
      </section>

      {isTerminal && event?.status === 'COMPLETED' && (
        <>
          <section className="review-overview">
            <div className="score-panel">
              <div className="score-panel__topline">
                <span className="section-kicker">Score</span>
                <span className="score-label">{event.result}</span>
              </div>
              <div className="score-value">{event.score}</div>
              <div className="score-track">
                <span style={{ width: `${event.score || 0}%` }}></span>
              </div>
            </div>
            <div className="summary-panel">
              <div className="section-kicker">Execution Details</div>
              <div className="review-grid" style={{ marginTop: '20px' }}>
                <div>
                  <div className="complexity-label">Test Cases</div>
                  <div className="complexity-meaning">{event.passedTests} / {event.totalTests} passed</div>
                </div>
                <div>
                  <div className="complexity-label">Status</div>
                  <div className="complexity-meaning">{event.status}</div>
                </div>
                <div>
                  <div className="complexity-label">Timestamp</div>
                  <div className="complexity-meaning">{new Date(event.timestamp).toLocaleString()}</div>
                </div>
              </div>
            </div>
          </section>

          <section className="review-request-panel" aria-labelledby="request-review-title" style={{ marginTop: '28px' }}>
            <div>
              <p className="section-kicker">Optional next step</p>
              <h2 id="request-review-title">
                {existingReview === null ? 'Checking for AI review...' : existingReview ? 'AI code review available' : 'Request AI code review'}
              </h2>
              <p>
                {existingReview === null 
                  ? 'Loading review status...'
                  : existingReview
                  ? 'An AI review has been generated for this submission. Click below to view the detailed analysis.'
                  : 'The review runs asynchronously. You will see it move from queued to processing and then to its final result.'}
              </p>
            </div>
            {existingReview === null ? (
              <button className="review-button" type="button" disabled>
                <span className="loader loader--small" style={{ display: 'inline-block', width: '16px', height: '16px', marginRight: '8px' }}></span>
                Checking...
              </button>
            ) : existingReview ? (
              <button className="review-button" type="button" onClick={viewReview}>
                View AI Review
              </button>
            ) : (
              <button className="review-button" type="button" disabled={isRequesting} onClick={() => void requestReview()}>
                {isRequesting ? 'Queueing review...' : 'Request AI review'}
              </button>
            )}
          </section>

          {reviewError && (
            <div className="review-inline-error" role="alert">
              {reviewError}
              <button className="inline-retry" type="button" onClick={() => void requestReview()}>Retry</button>
            </div>
          )}
        </>
      )}
    </main>
  );
}

export default SubmissionResultPage;
