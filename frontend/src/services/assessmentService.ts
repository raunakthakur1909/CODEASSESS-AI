import api from './api';

export interface Assessment {
    id: number;
    title: string;
    description: string;
    instructions?: string;
    durationMinutes: number;
    totalMarks: number;
    createdBy: number;
    status: 'DRAFT' | 'PUBLISHED' | 'CLOSED';
    createdAt: string;
    updatedAt: string;
    startedAt?: string;
}

export interface CreateAssessmentRequest {
    title: string;
    description: string;
    instructions?: string;
    durationMinutes: number;
}

export const assessmentService = {
    getAllAssessments: async () => {
        const response = await api.get<Assessment[]>('/assessments');
        return response.data;
    },

    getAvailableAssessments: async () => {
        const response = await api.get<Assessment[]>('/assessments/available');
        return response.data;
    },

    getMyAssessments: async () => {
        const response = await api.get<Assessment[]>('/assessments/my');
        return response.data;
    },

    getAssessment: async (id: number) => {
        const response = await api.get<Assessment>(`/assessments/${id}`);
        return response.data;
    },

    createAssessment: async (data: CreateAssessmentRequest) => {
        const response = await api.post<Assessment>('/assessments', data);
        return response.data;
    },

    updateAssessment: async (id: number, data: CreateAssessmentRequest) => {
        const response = await api.put<Assessment>(`/assessments/${id}`, data);
        return response.data;
    },

    deleteAssessment: async (id: number) => {
        await api.delete(`/assessments/${id}`);
    },

    publishAssessment: async (id: number) => {
        const response = await api.patch<Assessment>(`/assessments/${id}/publish`);
        return response.data;
    },
    
    duplicateAssessment: async (id: number) => {
        const response = await api.post<Assessment>(`/assessments/${id}/duplicate`);
        return response.data;
    }
};
