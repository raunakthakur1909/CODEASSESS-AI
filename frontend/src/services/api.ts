import { authService } from './authService';

const API_BASE_URL = '/api';

const getHeaders = () => {
    const token = authService.getToken();
    return {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    };
};

const handleResponse = async (response: Response) => {
    if (!response.ok) {
        if (response.status === 401) {
            authService.logout();
            if (!window.location.pathname.includes('/login')) {
                window.location.href = '/login?expired=true';
            }
        }
        let errorData = {};
        try {
            errorData = await response.json();
        } catch (e) {
            // ignore
        }
        const error = new Error('API Request failed');
        (error as any).response = { status: response.status, data: errorData };
        throw error;
    }
    
    // Check if it's 204 No Content
    if (response.status === 204) {
        return { data: null };
    }
    
    const data = await response.json();
    return { data };
};

export const api = {
    get: async <T>(url: string) => {
        const response = await fetch(`${API_BASE_URL}${url}`, {
            method: 'GET',
            headers: getHeaders()
        });
        return handleResponse(response) as Promise<{ data: T }>;
    },
    
    post: async <T>(url: string, body?: any) => {
        const response = await fetch(`${API_BASE_URL}${url}`, {
            method: 'POST',
            headers: getHeaders(),
            body: body ? JSON.stringify(body) : undefined
        });
        return handleResponse(response) as Promise<{ data: T }>;
    },
    
    put: async <T>(url: string, body: any) => {
        const response = await fetch(`${API_BASE_URL}${url}`, {
            method: 'PUT',
            headers: getHeaders(),
            body: JSON.stringify(body)
        });
        return handleResponse(response) as Promise<{ data: T }>;
    },
    
    patch: async <T>(url: string, body?: any) => {
        const response = await fetch(`${API_BASE_URL}${url}`, {
            method: 'PATCH',
            headers: getHeaders(),
            body: body ? JSON.stringify(body) : undefined
        });
        return handleResponse(response) as Promise<{ data: T }>;
    },
    
    delete: async (url: string) => {
        const response = await fetch(`${API_BASE_URL}${url}`, {
            method: 'DELETE',
            headers: getHeaders()
        });
        return handleResponse(response);
    }
};

export default api;
