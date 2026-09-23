interface AnalyticsEmptyStateProps {
  assessmentId: number;
  /** called when the user clicks "Reload" */
  onRetry: () => void;
}

/**
 * Shown when the assessment has no candidates or no submissions yet.
 * Gives the admin a friendly message and a reload shortcut.
 */
function AnalyticsEmptyState({ assessmentId, onRetry }: AnalyticsEmptyStateProps) {
  return (
    <div className="an-empty" role="status" aria-live="polite">
      <div className="an-empty__icon" aria-hidden="true">📊</div>
      <h2>No data yet</h2>
      <p>
        Assessment #{assessmentId} has no submissions to report on. Once candidates
        start submitting solutions the metrics will appear here automatically.
      </p>
      <button
        id="analytics-empty-reload-btn"
        type="button"
        className="an-retry-btn"
        onClick={onRetry}
        style={{ marginTop: '20px' }}
      >
        Reload
      </button>
    </div>
  );
}

export default AnalyticsEmptyState;
