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

  status: ReviewStatus;

  createdAt: string;
  completedAt?: string;
}
