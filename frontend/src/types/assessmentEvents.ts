export type AssessmentEventStatus = 'DRAFT' | 'PUBLISHED' | 'CLOSED' | 'ACTIVE' | 'COMPLETED';
export type AssessmentEventType = 'ASSESSMENT_STARTED' | 'ASSESSMENT_UPDATED' | 'ASSESSMENT_EXPIRING' | 'ASSESSMENT_EXPIRED' | 'ASSESSMENT_SUBMITTED';

export interface AssessmentEvent {
  assessmentId: number;
  type: AssessmentEventType;
  status: AssessmentEventStatus;
  serverTime: string; // ISO 8601 string
  expiresAt: string | null; // ISO 8601 string, null if no time limit
}
