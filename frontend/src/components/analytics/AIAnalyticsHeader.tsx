interface AIAnalyticsHeaderProps {
  assessmentId: number;
  onBack: () => void;
}

/**
 * Page header for the AI Analytics dashboard.
 */
function AIAnalyticsHeader({ assessmentId, onBack }: AIAnalyticsHeaderProps) {
  return (
    <header className="an-header" aria-label="AI Analytics header">
      <div className="an-header__meta">
        <p className="an-header__kicker">
          Assessment #{assessmentId}
        </p>
        <h1 className="an-header__title">AI Insights & Analytics</h1>
        <p className="an-header__subtitle">
          Aggregate code quality intelligence derived from automated AI reviews.
        </p>
      </div>

      <button
        type="button"
        className="an-back-btn"
        onClick={onBack}
        aria-label="Back to dashboard"
      >
        ← Back
      </button>
    </header>
  );
}

export default AIAnalyticsHeader;
