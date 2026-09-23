import api from './api';

export interface RunRequest {
    language: string;
    sourceCode: string;
}

export interface RunResult {
    testCaseId: number;
    input: string;
    expectedOutput: string;
    actualOutput?: string;
    passed: boolean;
    errorMessage?: string;
    time?: number;
    memory?: number;
}

export interface RunResponse {
    status: string;
    passedTests: number;
    totalTests: number;
    compilerOutput?: string;
    testResults: RunResult[];
}

export const runService = {
    executeRun: async (assessmentId: number, problemId: number, data: RunRequest) => {
        const response = await api.post<RunResponse>(
            `/assessments/${assessmentId}/problems/${problemId}/run`,
            data
        );
        return response.data;
    }
};
