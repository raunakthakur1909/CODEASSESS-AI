import React from 'react';
import type { SubmissionEvent } from '../../types/submissionEvents';

interface LiveExecutionProgressProps {
  event: SubmissionEvent | null;
}

const LiveExecutionProgress: React.FC<LiveExecutionProgressProps> = ({ event }) => {
  if (!event || event.status === 'QUEUED') {
    return null; // Don't show anything if no event or just queued
  }

  const { status, passedTests, totalTests, currentTestCase, result } = event;

  // Render test cases generically
  const renderTestCases = () => {
    if (!totalTests) return null;

    const tests = [];
    for (let i = 1; i <= totalTests; i++) {
      let state = '○ Pending';
      let color = '#666';

      if (status === 'COMPLETED' || status === 'FAILED') {
        // In terminal state, we just show passed or failed
        if (i <= passedTests) {
          state = '✓ Passed';
          color = '#2e7d32';
        } else if (i === passedTests + 1 && result !== 'ACCEPTED') {
          // If we failed fast on a specific test case, show it.
          // Since we do run-all except compilation, the ones after first failure might also be failed or passed.
          // But our SSE doesn't track exact arrays of results, it just tracks passed count.
          // Wait, the user asked for:
          // Test 1    ✓ Passed
          // Test 2    ✓ Passed
          // Test 3    ● Running
          // Test 4    ○ Pending
          // If terminal, we just show the final passedTests count as Passed, others as Failed/Pending?
          // The prompt says "Terminal state removes the live execution state."
          // So maybe we don't render this list at all in terminal state? Or just render the final result.
        }
      } else {
        // Live state
        if (currentTestCase && i < currentTestCase) {
          state = '✓ Passed'; // Optimistic display during execution, though we might have failed it. Wait, run-all means we don't fail fast! So i < currentTestCase means it FINISHED. We only know overall passedTests. Let's assume passedTests updates as we go.
          // Actually, if we are on test 3, tests 1 and 2 are done.
          // Are they passed? If passedTests == 2, yes both passed.
          // If passedTests == 1, one failed. We don't have individual arrays.
          // Let's just use simple logic: if it's < currentTestCase, it's done. 
        } else if (i === currentTestCase) {
          state = '● Running';
          color = '#0277bd';
        }
      }

      // Refined logic for live display:
      if (status === 'EVALUATING') {
        if (i === currentTestCase) {
          state = '● Running';
          color = '#0277bd';
        } else if (currentTestCase && i < currentTestCase) {
          // It's finished. Was it passed? 
          // If we assume sequential evaluation, the number of passed tests so far is in `passedTests`.
          // We can't know exactly which one failed without an array, but we can just say "Finished" or guess based on passedTests.
          // The user example:
          // Test 1    ✓ Passed
          // Test 2    ✓ Passed
          // Test 3    ● Running
          // Test 4    ○ Pending
          state = i <= passedTests ? '✓ Passed' : '✗ Failed';
          color = i <= passedTests ? '#2e7d32' : '#c62828';
        }
      }

      tests.push(
        <div key={i} style={{ display: 'flex', gap: '16px', marginBottom: '8px', color }}>
          <span style={{ width: '80px' }}>Test {i}</span>
          <span>{state}</span>
        </div>
      );
    }
    return tests;
  };

  const progressPercent = totalTests > 0 
    ? ((currentTestCase ? currentTestCase - 1 : passedTests) / totalTests) * 100 
    : 0;

  return (
    <div style={{ marginTop: '24px', padding: '16px', border: '1px solid #e0e0e0', borderRadius: '8px' }}>
      <h4 style={{ margin: '0 0 16px 0' }}>Execution Progress</h4>
      
      {status === 'EVALUATING' && (
        <>
          <div style={{ width: '100%', height: '8px', backgroundColor: '#e0e0e0', borderRadius: '4px', marginBottom: '16px' }}>
            <div style={{ 
              width: `${progressPercent}%`, 
              height: '100%', 
              backgroundColor: '#0277bd', 
              borderRadius: '4px',
              transition: 'width 0.3s ease'
            }} />
          </div>
          <div>{renderTestCases()}</div>
        </>
      )}

      {(status === 'COMPLETED' || status === 'FAILED') && (
        <div>
          <p style={{ fontWeight: 'bold', color: result === 'ACCEPTED' ? '#2e7d32' : '#c62828' }}>
            Final Result: {result || 'ERROR'}
          </p>
          <p>Score: {event.score}/100</p>
          <p>Passed {passedTests} of {totalTests} test cases</p>
        </div>
      )}
    </div>
  );
};

export default LiveExecutionProgress;
