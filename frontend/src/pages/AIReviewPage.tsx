import ComplexityCard from '../components/ai-review/ComplexityCard';
import ReviewHeader from '../components/ai-review/ReviewHeader';
import ReviewIssues from '../components/ai-review/ReviewIssues';
import ReviewScore from '../components/ai-review/ReviewScore';
import StrengthsList from '../components/ai-review/StrengthsList';
import OptimizationList from '../components/ai-review/OptimizationList';
import ReviewState from '../components/ai-review/ReviewState';
import { useAIReviewPolling } from '../hooks/useAIReviewPolling';
import './review.css';

interface AIReviewPageProps {
  submissionId: number;
}

function AIReviewPage({ submissionId }: AIReviewPageProps) {
  const { review, isPolling, error, retry } = useAIReviewPolling(submissionId);

  if (error && !review) {
    return (
      <main className="review-shell">
        <div className="review-state review-state--error">
          <span className="state-icon" aria-hidden="true">!</span>
          <p className="eyebrow">Submission {submissionId}</p>
          <h1>Review unavailable</h1>
          <p>{error}</p>
          <button className="review-button" type="button" onClick={retry}>Try again</button>
        </div>
      </main>
    );
  }

  if (!review) {
    return (
      <main className="review-shell">
        <div className="review-state review-state--loading" aria-live="polite">
          <span className="loader" aria-hidden="true" />
          <p className="eyebrow">Submission {submissionId}</p>
          <h1>Loading your review</h1>
          <p>Checking the review queue for the latest result.</p>
        </div>
      </main>
    );
  }

  return (
    <main className="review-shell">
      <ReviewHeader submissionId={submissionId} status={review.status} />

      {isPolling && (
        <div className="review-live-banner" role="status">
          <span className="loader loader--small" aria-hidden="true" />
          Updating review status
        </div>
      )}

      {error && (
        <div className="review-inline-error" role="alert">
          <span>{error}</span>
          <button className="inline-retry" type="button" onClick={retry}>Try again</button>
        </div>
      )}

      {review.status === 'COMPLETED' ? (
        <>
          <div className="review-overview">
            <ReviewScore score={review.overallScore} />
            <section className="summary-panel">
              <p className="section-kicker">Reviewer summary</p>
              <h2>{review.summary}</h2>
              <p className="summary-note">A structured review of correctness, quality, performance, and maintainability.</p>
            </section>
          </div>

          <div className="complexity-grid">
            <ComplexityCard label="Time complexity" value={review.timeComplexity} accent="lime" />
            <ComplexityCard label="Space complexity" value={review.spaceComplexity} accent="coral" />
          </div>

          <div className="review-grid">
            <StrengthsList strengths={review.strengths} />
            <OptimizationList suggestions={review.optimizationSuggestions} />
          </div>

          <section className="review-section review-section--warning" aria-labelledby="security-title">
            <div className="section-heading">
              <span className="section-marker" aria-hidden="true" />
              <h2 id="security-title">Security findings</h2>
              <span className="section-count">{review.securityIssues.length}</span>
            </div>
            {review.securityIssues.length > 0 ? (
              <ul className="review-list">
                {review.securityIssues.map((issue, index) => <li key={`${issue}-${index}`}>{issue}</li>)}
              </ul>
            ) : (
              <p className="empty-copy">No security findings were identified.</p>
            )}
          </section>
          <ReviewIssues issues={review.issues} />
        </>
      ) : (
        <ReviewState status={review.status} submissionId={submissionId} onRetry={retry} />
      )}
    </main>
  );
}

export default AIReviewPage;
