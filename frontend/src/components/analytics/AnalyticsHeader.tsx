interface AnalyticsHeaderProps {
  assessmentId: number;
  title: string;
  /** called when the user clicks the back button */
  onBack: () => void;
}

/**
 * Page header for the Admin Analytics Dashboard.
 * Shows the assessment title, a kicker with the assessment ID,
 * and a back button to return to the main dashboard.
 */
function AnalyticsHeader({ assessmentId, title, onBack }: AnalyticsHeaderProps) {
  return (
    <header className="an-header" aria-label="Analytics header">
      <div className="an-header__meta">
        <p className="an-header__kicker">
          Analytics — Assessment #{assessmentId}
        </p>
        <h1 className="an-header__title">{title}</h1>
        <p className="an-header__subtitle">
          Aggregate submission metrics across all candidates
        </p>
      </div>

      <button
        id="analytics-back-btn"
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

export default AnalyticsHeader;
