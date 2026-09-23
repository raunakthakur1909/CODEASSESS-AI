interface StrengthsListProps {
  strengths: string[];
}

function StrengthsList({ strengths }: StrengthsListProps) {
  return (
    <section className="review-section strengths-section" aria-labelledby="strengths-title">
      <div className="section-heading">
        <span className="section-marker" aria-hidden="true" />
        <h2 id="strengths-title">Strengths</h2>
        <span className="section-count">{strengths.length}</span>
      </div>
      {strengths.length > 0 ? (
        <ul className="strength-list">
          {strengths.map((strength, index) => (
            <li key={`${strength}-${index}`}>
              <span className="strength-icon" aria-hidden="true">+</span>
              <span>{strength}</span>
            </li>
          ))}
        </ul>
      ) : (
        <p className="empty-copy">No strengths were recorded for this review.</p>
      )}
    </section>
  );
}

export default StrengthsList;
