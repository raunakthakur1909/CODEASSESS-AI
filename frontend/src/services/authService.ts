import type { RegisterRequest, LoginRequest, LoginResponse, MessageResponse } from '../types/auth';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api';

export const authService = {
  /**
   * Register a new user
   */
  async register(data: RegisterRequest): Promise<MessageResponse> {
    const response = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Registration failed');
    }

    return response.json();
  },

  /**
   * Login user
   */
  async login(data: LoginRequest): Promise<LoginResponse> {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Login failed');
    }

    const loginResponse = await response.json();
    
    // Store token in localStorage
    localStorage.setItem('accessToken', loginResponse.accessToken);
    
    return loginResponse;
  },

  /**
   * Logout user
   */
  logout(): void {
    localStorage.removeItem('accessToken');
  },

  /**
   * Get stored token
   */
  getToken(): string | null {
    return localStorage.getItem('accessToken');
  },

  /**
   * Check if user is authenticated
   */
  isAuthenticated(): boolean {
    return !!this.getToken();
  },

  /**
   * Decode JWT token payload
   */
  decodeToken(token: string): any {
    try {
      const payload = token.split('.')[1];
      return JSON.parse(atob(payload));
    } catch {
      return null;
    }
  },

  /**
   * Get current user info from token
   */
  getCurrentUser(): { email: string; role: string } | null {
    const token = this.getToken();
    if (!token) return null;

    const decoded = this.decodeToken(token);
    if (!decoded) return null;

    return {
      email: decoded.sub,
      role: decoded.role,
    };
  },
};
