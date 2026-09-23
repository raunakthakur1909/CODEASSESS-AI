import api from './api';

export interface SaveDraftRequest {
    language: string;
    sourceCode: string;
}

export interface DraftResponse {
    id: number;
    candidateId: number;
    assessmentId: number;
    problemId: number;
    language: string;
    sourceCode: string;
    updatedAt: string;
}

export const draftService = {
    saveDraft: async (assessmentId: number, problemId: number, data: SaveDraftRequest) => {
        const response = await api.post<DraftResponse>(
            `/assessments/${assessmentId}/problems/${problemId}/drafts`,
            data
        );
        return response.data;
    },

    getDraft: async (assessmentId: number, problemId: number) => {
        try {
            const response = await api.get<DraftResponse>(
                `/assessments/${assessmentId}/problems/${problemId}/drafts`
            );
            return response.data;
        } catch (error: any) {
            if (error.response?.status === 404) {
                return null;
            }
            throw error;
        }
    },
    
    // Fallback local storage methods
    saveLocalDraft: (assessmentId: number, problemId: number, language: string, sourceCode: string) => {
        const key = `draft_${assessmentId}_${problemId}`;
        localStorage.setItem(key, JSON.stringify({ language, sourceCode, updatedAt: new Date().toISOString() }));
    },
    
    getLocalDraft: (assessmentId: number, problemId: number) => {
        const key = `draft_${assessmentId}_${problemId}`;
        const data = localStorage.getItem(key);
        if (data) {
            return JSON.parse(data) as { language: string, sourceCode: string, updatedAt: string };
        }
        return null;
    }
};
