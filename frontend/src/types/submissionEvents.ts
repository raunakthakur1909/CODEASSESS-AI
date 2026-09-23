export type SubmissionEventStatus =
  | 'QUEUED'
  | 'RUNNING'
  | 'EVALUATING'
  | 'COMPLETED'
  | 'FAILED';

export interface SubmissionEvent {
  submissionId: number;
  status: SubmissionEventStatus;
  result: string | null;
  score: number | null;
  passedTests: number;
  totalTests: number;
  currentTestCase?: number;
  timestamp: string;
}
