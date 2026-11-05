export type ReviewStatus =
  | "PENDING"
  | "PROCESSING"
  | "COMPLETED"
  | "FAILED"
  | "SKIPPED";

export type ReviewSeverity =
  | "LOW"
  | "MEDIUM"
  | "HIGH"
  | "CRITICAL";

export type ReviewCategory =
  | "CORRECTNESS"
  | "PERFORMANCE"
  | "CODE_QUALITY"
  | "SECURITY"
  | "MAINTAINABILITY"
  | "STYLE";

export interface CodeReviewIssue {
  severity: ReviewSeverity;
  category: ReviewCategory;
  message: string;
  suggestion: string;
  lineStart?: number;
  lineEnd?: number;
  title: string;
}

export interface CodeReviewOptimization {
  currentApproach: string;
  optimizedApproach: string;
  currentTimeComplexity: string;
  currentSpaceComplexity: string;
  optimizedTimeComplexity: string;
  optimizedSpaceComplexity: string;
  explanation: string;
  optimizedCode: string;
}

export interface CodeReviewEdgeCase {
  description: string;
  input: string;
  expectedBehavior: string;
  reason: string;
}

export interface CodeReviewFailureDiagnosis {
  testCaseId: string;
  failureReason: string;
  likelyFault: string;
  explanation: string;
  suggestedFix: string;
}

export interface CodeReview {
  id: number;
  submissionId: number;

  overallScore: number;
  summary: string;

  timeComplexity: string;
  spaceComplexity: string;

  strengths: string[];
  optimizationSuggestions: string[];
  securityIssues: string[];

  issues: CodeReviewIssue[];

  correctedCode?: string;
  explanationOfCorrection?: string;
  optimizationAnalysis?: CodeReviewOptimization;
  generatedEdgeCases?: CodeReviewEdgeCase[];
  failureDiagnoses?: CodeReviewFailureDiagnosis[];

  status: ReviewStatus;

  createdAt: string;
  completedAt?: string;
}
