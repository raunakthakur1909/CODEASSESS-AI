interface ReviewListProps {
  title: string;
  items: string[];
  tone: 'positive' | 'accent' | 'warning';
  emptyMessage: string;
}

function ReviewList({ title, items, tone, emptyMessage }: ReviewListProps) {
  return (
    <section className={`review-section review-section--${tone}`}>
      <div className="section-heading">
        <span className="section-marker" aria-hidden="true" />
        <h2>{title}</h2>
      </div>
      {items.length > 0 ? (
        <ul className="review-list">
          {items.map((item, index) => <li key={`${item}-${index}`}>{item}</li>)}
        </ul>
      ) : (
        <p className="empty-copy">{emptyMessage}</p>
      )}
    </section>
  );
}

export default ReviewList;
