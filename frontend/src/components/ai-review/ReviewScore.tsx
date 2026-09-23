interface ReviewScoreProps {
  score: number;
}

function scoreLabel(score: number): string {
  if (score >= 90) return 'Excellent';
  if (score >= 75) return 'Strong';
  if (score >= 60) return 'Needs refinement';
  return 'Needs attention';
}

function ReviewScore({ score }: ReviewScoreProps) {
  const safeScore = Math.min(100, Math.max(0, score));

  return (
    <section className="score-panel" aria-label="AI review score">
      <div className="score-panel__topline">
        <p className="section-kicker">AI score</p>
        <span className="score-label">{scoreLabel(safeScore)}</span>
      </div>
      <div className="score-value">{safeScore}</div>
      <div className="score-denominator">/ 100</div>
      <div
        className="score-track"
        role="progressbar"
        aria-label="AI review score"
        aria-valuemin={0}
        aria-valuemax={100}
        aria-valuenow={safeScore}
      >
        <span style={{ width: `${safeScore}%` }} />
      </div>
    </section>
  );
}

export default ReviewScore;
