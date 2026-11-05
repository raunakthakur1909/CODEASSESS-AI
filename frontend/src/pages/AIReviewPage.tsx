import { useState } from 'react';
import ComplexityCard from '../components/ai-review/ComplexityCard';
import ReviewHeader from '../components/ai-review/ReviewHeader';
import ReviewIssues from '../components/ai-review/ReviewIssues';
import ReviewScore from '../components/ai-review/ReviewScore';
import StrengthsList from '../components/ai-review/StrengthsList';
import OptimizationList from '../components/ai-review/OptimizationList';
import ReviewState from '../components/ai-review/ReviewState';
import { useAIReviewPolling } from '../hooks/useAIReviewPolling';
import './review.css';
import './review-tabs.css';

interface AIReviewPageProps {
  submissionId: number;
}

function AIReviewPage({ submissionId }: AIReviewPageProps) {
  const { review, isPolling, error, retry } = useAIReviewPolling(submissionId);
  const [activeTab, setActiveTab] = useState<'OVERVIEW' | 'FAILURES' | 'FIX' | 'OPTIMIZE' | 'EDGE_CASES'>('OVERVIEW');

  if (error && !review) {
    return (
      <main className="review-shell">
        <div className="review-state review-state--error">
          <span className="state-icon" aria-hidden="true">!</span>
          <p className="eyebrow">Submission {submissionId}</p>
          <h1>Review unavailable</h1>
          <p>{error}</p>
          <button className="review-button" type="button" onClick={retry}>Try again</button>
        </div>
      </main>
    );
  }

  if (!review) {
    return (
      <main className="review-shell">
        <div className="review-state review-state--loading" aria-live="polite">
          <span className="loader" aria-hidden="true" />
          <p className="eyebrow">Submission {submissionId}</p>
          <h1>Loading your review</h1>
          <p>Checking the review queue for the latest result.</p>
        </div>
      </main>
    );
  }

  return (
    <main className="review-shell">
      <ReviewHeader submissionId={submissionId} status={review.status} />

      {isPolling && (
        <div className="review-live-banner" role="status">
          <span className="loader loader--small" aria-hidden="true" />
          Updating review status
        </div>
      )}

      {error && (
        <div className="review-inline-error" role="alert">
          <span>{error}</span>
          <button className="inline-retry" type="button" onClick={retry}>Try again</button>
        </div>
      )}

      {review.status === 'COMPLETED' ? (
        <>
          <div className="review-tabs">
            <button className={`tab-button ${activeTab === 'OVERVIEW' ? 'active' : ''}`} onClick={() => setActiveTab('OVERVIEW')}>Overview</button>
            <button className={`tab-button ${activeTab === 'FAILURES' ? 'active' : ''}`} onClick={() => setActiveTab('FAILURES')}>Diagnose Failures</button>
            <button className={`tab-button ${activeTab === 'FIX' ? 'active' : ''}`} onClick={() => setActiveTab('FIX')}>Fix Code</button>
            <button className={`tab-button ${activeTab === 'OPTIMIZE' ? 'active' : ''}`} onClick={() => setActiveTab('OPTIMIZE')}>Optimize</button>
            <button className={`tab-button ${activeTab === 'EDGE_CASES' ? 'active' : ''}`} onClick={() => setActiveTab('EDGE_CASES')}>Edge Cases</button>
          </div>

          {activeTab === 'OVERVIEW' && (
            <>
              <div className="review-overview">
                <ReviewScore score={review.overallScore} />
                <section className="summary-panel">
                  <p className="section-kicker">Reviewer summary</p>
                  <h2>{review.summary}</h2>
                  <p className="summary-note">A structured review of correctness, quality, performance, and maintainability.</p>
                </section>
              </div>

              <div className="complexity-grid">
                <ComplexityCard label="Time complexity" value={review.timeComplexity} accent="lime" />
                <ComplexityCard label="Space complexity" value={review.spaceComplexity} accent="coral" />
              </div>

              <div className="review-grid">
                <StrengthsList strengths={review.strengths} />
                <OptimizationList suggestions={review.optimizationSuggestions} />
              </div>

              <section className="review-section review-section--warning" aria-labelledby="security-title">
                <div className="section-heading">
                  <span className="section-marker" aria-hidden="true" />
                  <h2 id="security-title">Security findings</h2>
                  <span className="section-count">{review.securityIssues.length}</span>
                </div>
                {review.securityIssues.length > 0 ? (
                  <ul className="review-list">
                    {review.securityIssues.map((issue, index) => <li key={`${issue}-${index}`}>{issue}</li>)}
                  </ul>
                ) : (
                  <p className="empty-copy">No security findings were identified.</p>
                )}
              </section>
              <ReviewIssues issues={review.issues} />
            </>
          )}

          {activeTab === 'FAILURES' && (
            <section className="review-section">
              <div className="section-heading">
                <span className="section-marker" aria-hidden="true" />
                <h2>Failed Tests Diagnosis</h2>
              </div>
              {review.failureDiagnoses && review.failureDiagnoses.length > 0 ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
                  {review.failureDiagnoses.map((fd, i) => (
                    <div key={i} style={{ padding: '1rem', background: 'var(--bg-elevated)', borderRadius: 'var(--radius-md)' }}>
                      <h3 style={{ marginTop: 0, color: 'var(--danger-text)' }}>Test Case: {fd.testCaseId}</h3>
                      <p><strong>Reason:</strong> {fd.failureReason}</p>
                      <p><strong>Likely Fault:</strong> {fd.likelyFault}</p>
                      <p><strong>Explanation:</strong> {fd.explanation}</p>
                      <div style={{ padding: '0.75rem', background: 'rgba(255,255,255,0.05)', borderLeft: '3px solid var(--primary)', marginTop: '0.5rem' }}>
                        <strong>Suggested Fix:</strong> {fd.suggestedFix}
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="empty-copy">No test failures diagnosed.</p>
              )}
            </section>
          )}

          {activeTab === 'FIX' && (
            <section className="review-section">
              <div className="section-heading">
                <span className="section-marker" aria-hidden="true" />
                <h2>AI-Suggested Corrected Code</h2>
              </div>
              {review.correctedCode ? (
                <div>
                  <p style={{ color: 'var(--text-secondary)' }}><em>Note: The AI suggests this corrected implementation. It has not been executed unless shown in actual execution results.</em></p>
                  <p><strong>Explanation:</strong> {review.explanationOfCorrection}</p>
                  <pre className="code-block">{review.correctedCode}</pre>
                </div>
              ) : (
                <p className="empty-copy">No code corrections were suggested.</p>
              )}
            </section>
          )}

          {activeTab === 'OPTIMIZE' && (
            <section className="review-section">
              <div className="section-heading">
                <span className="section-marker" aria-hidden="true" />
                <h2>Optimization Analysis</h2>
              </div>
              {review.optimizationAnalysis ? (
                <div>
                  <div className="complexity-grid" style={{ marginBottom: '1.5rem' }}>
                    <ComplexityCard label="Current Time" value={review.optimizationAnalysis.currentTimeComplexity} accent="coral" />
                    <ComplexityCard label="Optimized Time" value={review.optimizationAnalysis.optimizedTimeComplexity} accent="lime" />
                    <ComplexityCard label="Current Space" value={review.optimizationAnalysis.currentSpaceComplexity} accent="coral" />
                    <ComplexityCard label="Optimized Space" value={review.optimizationAnalysis.optimizedSpaceComplexity} accent="lime" />
                  </div>
                  <p><strong>Current Approach:</strong> {review.optimizationAnalysis.currentApproach}</p>
                  <p><strong>Optimized Approach:</strong> {review.optimizationAnalysis.optimizedApproach}</p>
                  <p><strong>Explanation:</strong> {review.optimizationAnalysis.explanation}</p>
                  <h3 style={{ marginTop: '1.5rem' }}>Optimized Code</h3>
                  <pre className="code-block">{review.optimizationAnalysis.optimizedCode}</pre>
                </div>
              ) : (
                <p className="empty-copy">Code is already well optimized.</p>
              )}
            </section>
          )}

          {activeTab === 'EDGE_CASES' && (
            <section className="review-section">
              <div className="section-heading">
                <span className="section-marker" aria-hidden="true" />
                <h2>Generated Edge Cases</h2>
              </div>
              <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}><em>Note: These edge cases are suggestions for your own testing. They have not been run against your code.</em></p>
              {review.generatedEdgeCases && review.generatedEdgeCases.length > 0 ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  {review.generatedEdgeCases.map((ec, i) => (
                    <div key={i} style={{ padding: '1rem', background: 'var(--bg-elevated)', borderRadius: 'var(--radius-md)', borderLeft: '3px solid var(--accent)' }}>
                      <h4 style={{ margin: '0 0 0.5rem 0', color: 'var(--text-primary)' }}>{ec.description}</h4>
                      <p style={{ margin: '0.25rem 0' }}><strong>Input:</strong> <code>{ec.input}</code></p>
                      <p style={{ margin: '0.25rem 0' }}><strong>Expected:</strong> <code>{ec.expectedBehavior}</code></p>
                      <p style={{ margin: '0.25rem 0', color: 'var(--text-secondary)', fontSize: '0.9rem' }}>{ec.reason}</p>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="empty-copy">No additional edge cases suggested.</p>
              )}
            </section>
          )}
        </>
      ) : (
        <ReviewState status={review.status} submissionId={submissionId} onRetry={retry} />
      )}
    </main>
  );
}

export default AIReviewPage;
