import api from './api';

export interface Submission {
    id: number;
    language: string;
    status: string;
    result: string;
    score: number;
    passedTests: number;
    totalTests: number;
    submittedAt: string;
    sourceCode?: string;
}

export const submissionService = {
    getMyProblemSubmissions: async (problemId: number): Promise<Submission[]> => {
        const response = await api.get(`/problems/${problemId}/submissions/my`);
        return response.data as Submission[];
    },
    
    getSubmission: async (id: number): Promise<Submission> => {
        const response = await api.get(`/submissions/${id}`);
        return response.data as Submission;
    },
    
    submitCode: async (problemId: number, language: string, sourceCode: string): Promise<Submission> => {
        const response = await api.post(`/problems/${problemId}/submissions`, {
            language,
            sourceCode
        });
        return response.data as Submission;
    }
};
