interface OptimizationListProps {
  suggestions: string[];
}

type SuggestionPriority = 'HIGH' | 'MEDIUM' | 'LOW';

function inferPriority(suggestion: string): SuggestionPriority {
  const text = suggestion.toLowerCase();
  if (/security|vulnerab|crash|incorrect|bug|critical|must/.test(text)) return 'HIGH';
  if (/performance|complexity|slow|memory|consider|should/.test(text)) return 'MEDIUM';
  return 'LOW';
}

function OptimizationList({ suggestions }: OptimizationListProps) {
  return (
    <section className="review-section optimization-section" aria-labelledby="optimization-title">
      <div className="section-heading">
        <span className="section-marker" aria-hidden="true" />
        <h2 id="optimization-title">Optimization suggestions</h2>
        <span className="section-count">{suggestions.length}</span>
      </div>
      {suggestions.length > 0 ? (
        <div className="optimization-list">
          {suggestions.map((suggestion, index) => {
            const priority = inferPriority(suggestion);
            return (
              <article className="optimization-item" data-priority={priority} key={`${suggestion}-${index}`}>
                <div className="optimization-item__topline">
                  <span className="priority-badge" data-priority={priority}>{priority} PRIORITY</span>
                  <span className="optimization-index">0{index + 1}</span>
                </div>
                <p>{suggestion}</p>
                <span className="action-label">Actionable recommendation</span>
              </article>
            );
          })}
        </div>
      ) : (
        <p className="empty-copy">No optimization suggestions were recorded for this review.</p>
      )}
    </section>
  );
}

export default OptimizationList;
