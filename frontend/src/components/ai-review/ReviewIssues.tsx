import { useMemo, useState } from 'react';
import type { CodeReviewIssue, ReviewCategory, ReviewSeverity } from '../../types/aiReview';

interface ReviewIssuesProps {
  issues: CodeReviewIssue[];
}

const severityRank: Record<ReviewSeverity, number> = {
  CRITICAL: 0,
  HIGH: 1,
  MEDIUM: 2,
  LOW: 3,
};

const severityLabels: Record<ReviewSeverity, string> = {
  CRITICAL: 'Immediate attention',
  HIGH: 'High impact',
  MEDIUM: 'Worth addressing',
  LOW: 'Minor improvement',
};

function formatCategory(category: ReviewCategory): string {
  return category.replaceAll('_', ' ');
}

function ReviewIssues({ issues }: ReviewIssuesProps) {
  const [selectedCategory, setSelectedCategory] = useState<'ALL' | ReviewCategory>('ALL');
  const categories = useMemo(
    () => Array.from(new Set(issues.map((issue) => issue.category))),
    [issues],
  );
  const visibleIssues = useMemo(
    () => issues
      .filter((issue) => selectedCategory === 'ALL' || issue.category === selectedCategory)
      .sort((first, second) => severityRank[first.severity] - severityRank[second.severity]),
    [issues, selectedCategory],
  );

  return (
    <section className="review-section review-section--issues">
      <div className="section-heading">
        <span className="section-marker" aria-hidden="true" />
        <h2>Issues to consider</h2>
        <span className="issue-count">{issues.length}</span>
      </div>
      {issues.length > 0 && (
        <div className="issue-filters" aria-label="Filter review issues">
          <button
            className="issue-filter"
            data-active={selectedCategory === 'ALL'}
            type="button"
            onClick={() => setSelectedCategory('ALL')}
          >
            All <span>{issues.length}</span>
          </button>
          {categories.map((category) => {
            const count = issues.filter((issue) => issue.category === category).length;
            return (
              <button
                className="issue-filter"
                data-active={selectedCategory === category}
                key={category}
                type="button"
                onClick={() => setSelectedCategory(category)}
              >
                {formatCategory(category)} <span>{count}</span>
              </button>
            );
          })}
        </div>
      )}
      {visibleIssues.length > 0 ? (
        <div className="issue-list">
          {visibleIssues.map((issue, index) => (
            <article className="issue-item" key={`${issue.title || issue.message}-${index}`}>
              <div className="issue-meta" style={{ display: 'flex', alignItems: 'center', flexWrap: 'wrap', gap: '0.5rem' }}>
                <span className="severity-badge" data-severity={issue.severity}>{issue.severity}</span>
                <span>{formatCategory(issue.category)}</span>
                <span className="severity-label">{severityLabels[issue.severity]}</span>
                {issue.lineStart && (
                  <span style={{ marginLeft: 'auto', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                    Line {issue.lineStart}{issue.lineEnd && issue.lineEnd !== issue.lineStart ? `-${issue.lineEnd}` : ''}
                  </span>
                )}
              </div>
              {issue.title && <h3 style={{ margin: '0.5rem 0', fontSize: '1.05rem', color: 'var(--text-primary)' }}>{issue.title}</h3>}
              <p style={{ marginTop: '0.25rem' }}>{issue.message}</p>
              <div className="issue-suggestion">
                <strong>Remediation</strong>
                <span>{issue.suggestion}</span>
              </div>
            </article>
          ))}
        </div>
      ) : issues.length > 0 ? (
        <p className="empty-copy">No issues match this category.</p>
      ) : (
        <p className="empty-copy">No issues were identified in this review.</p>
      )}
    </section>
  );
}

export default ReviewIssues;
