import api from './api';

export type ProgrammingLanguage = 'JAVA' | 'PYTHON' | 'CPP' | 'C';

export interface Problem {
    id: number;
    assessmentId: number;
    title: string;
    description: string;
    difficulty: 'EASY' | 'MEDIUM' | 'HARD';
    points: number;
    inputFormat?: string;
    outputFormat?: string;
    constraints?: string;
    starterCode?: string | null;
    starterLanguage?: ProgrammingLanguage | null;
    createdAt: string;
    updatedAt: string;
}

export interface CreateProblemRequest {
    title: string;
    description: string;
    difficulty: 'EASY' | 'MEDIUM' | 'HARD';
    points: number;
    inputFormat?: string;
    outputFormat?: string;
    constraints?: string;
    starterCode?: string | null;
    starterLanguage?: ProgrammingLanguage | null;
}

export interface TestCase {
    id: number;
    problemId: number;
    input: string;
    expectedOutput?: string;
    hidden?: boolean;
    timeLimitMs: number;
    memoryLimitMb: number;
}

export interface CreateTestCaseRequest {
    input: string;
    expectedOutput: string;
    hidden: boolean;
    timeLimitMs: number;
    memoryLimitMb: number;
}

export const problemService = {
    getProblems: async (assessmentId: number) => {
        const response = await api.get<Problem[]>(`/assessments/${assessmentId}/problems`);
        return response.data;
    },

    getProblem: async (problemId: number) => {
        const response = await api.get<Problem>(`/problems/${problemId}`);
        return response.data;
    },

    createProblem: async (assessmentId: number, data: CreateProblemRequest) => {
        const response = await api.post<Problem>(`/assessments/${assessmentId}/problems`, data);
        return response.data;
    },

    updateProblem: async (problemId: number, data: CreateProblemRequest) => {
        const response = await api.put<Problem>(`/problems/${problemId}`, data);
        return response.data;
    },

    deleteProblem: async (problemId: number) => {
        await api.delete(`/problems/${problemId}`);
    },

    getTestCases: async (problemId: number) => {
        const response = await api.get<TestCase[]>(`/problems/${problemId}/test-cases`);
        return response.data;
    },

    createTestCase: async (problemId: number, data: CreateTestCaseRequest) => {
        const response = await api.post<TestCase>(`/problems/${problemId}/test-cases`, data);
        return response.data;
    },

    deleteTestCase: async (testCaseId: number) => {
        await api.delete(`/test-cases/${testCaseId}`);
    }
};
