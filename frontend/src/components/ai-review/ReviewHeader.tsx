import type { ReviewStatus } from '../../types/aiReview';

interface ReviewHeaderProps {
  submissionId: number;
  status: ReviewStatus;
}

const statusLabels: Record<ReviewStatus, string> = {
  PENDING: 'AI review queued',
  PROCESSING: 'AI is reviewing your code',
  COMPLETED: 'AI review completed',
  FAILED: 'AI review failed',
  SKIPPED: 'AI review skipped',
};

function ReviewHeader({ submissionId, status }: ReviewHeaderProps) {
  return (
    <header className="review-header">
      <div>
        <p className="eyebrow">CodeAssess AI / Submission {submissionId}</p>
        <h1>AI code review</h1>
        <p className="review-status" data-status={status}>
          <span className="status-dot" aria-hidden="true" />
          {statusLabels[status]}
        </p>
      </div>
      <span className="status-badge" data-status={status}>{status}</span>
    </header>
  );
}

export default ReviewHeader;
