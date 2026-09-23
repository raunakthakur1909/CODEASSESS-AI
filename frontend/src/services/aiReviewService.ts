import type { CodeReview } from '../types/aiReview';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api';

async function readError(response: Response, fallbackMessage: string): Promise<Error> {
  try {
    const error = await response.json() as { message?: string };
    return new Error(error.message || fallbackMessage);
  } catch {
    return new Error(fallbackMessage);
  }
}

async function authenticatedRequest(
  path: string,
  options: RequestInit = {},
): Promise<Response> {
  const token = localStorage.getItem('accessToken');
  const headers = new Headers(options.headers);
  headers.set('Accept', 'application/json');

  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  return fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  });
}

async function parseReviewResponse(response: Response, fallbackMessage: string): Promise<CodeReview> {
  if (!response.ok) {
    throw await readError(response, fallbackMessage);
  }

  return response.json() as Promise<CodeReview>;
}

export const aiReviewService = {
  /**
   * Queue an AI review. The backend returns 202 with a PENDING review.
   */
  async requestReview(submissionId: number): Promise<CodeReview> {
    const response = await authenticatedRequest(
      `/submissions/${submissionId}/ai-review`,
      { method: 'POST' },
    );

    if (response.status !== 202) {
      if (!response.ok) {
        throw await readError(response, 'Unable to queue AI review');
      }

      throw new Error(`Unexpected response while queuing AI review: ${response.status}`);
    }

    return response.json() as Promise<CodeReview>;
  },

  /**
   * Get the current review state for a submission.
   */
  async getReviewBySubmission(submissionId: number): Promise<CodeReview> {
    const response = await authenticatedRequest(
      `/submissions/${submissionId}/ai-review`,
    );

    return parseReviewResponse(response, 'Unable to load the AI review');
  },

  /**
   * Get all reviews belonging to the authenticated candidate.
   */
  async getMyReviews(): Promise<CodeReview[]> {
    const response = await authenticatedRequest('/ai-reviews/my');

    if (!response.ok) {
      throw await readError(response, 'Unable to load your AI reviews');
    }

    return response.json() as Promise<CodeReview[]>;
  },

  /**
   * Get one review by its ID. The backend applies candidate/admin authorization.
   */
  async getReviewById(reviewId: number): Promise<CodeReview> {
    const response = await authenticatedRequest(`/ai-reviews/${reviewId}`);

    return parseReviewResponse(response, 'Unable to load the AI review');
  },
};
