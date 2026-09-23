import type { ReviewStatus } from '../../types/aiReview';

interface ReviewStateProps {
  status: ReviewStatus;
  submissionId: number;
  onRetry: () => void;
}

const stateCopy: Record<ReviewStatus, { title: string; message: string; action?: string }> = {
  PENDING: {
    title: 'Your review is queued',
    message: 'The review worker will pick up this submission shortly.',
  },
  PROCESSING: {
    title: 'The AI is reviewing your code',
    message: 'We are analyzing quality, complexity, performance, and security.',
  },
  COMPLETED: {
    title: 'Your review is ready',
    message: 'The structured review has finished processing.',
  },
  FAILED: {
    title: 'The review could not be completed',
    message: 'The worker reported a failure. Check again for the latest state.',
    action: 'Check again',
  },
  SKIPPED: {
    title: 'The review was skipped',
    message: 'No AI review result is available for this submission.',
    action: 'Check again',
  },
};

function ReviewState({ status, submissionId, onRetry }: ReviewStateProps) {
  const copy = stateCopy[status];

  return (
    <section className="review-state review-state--progress" data-status={status} role="status" aria-live="polite">
      <span className="loader" aria-hidden="true" />
      <p className="eyebrow">Submission {submissionId} / {status}</p>
      <h2>{copy.title}</h2>
      <p>{copy.message}</p>
      {copy.action && <button className="review-button" type="button" onClick={onRetry}>{copy.action}</button>}
    </section>
  );
}

export default ReviewState;
