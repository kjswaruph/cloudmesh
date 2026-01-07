/**
 * Centralized REST API Client for CloudMesh
 * All API endpoints are defined here for consistency
 */

const API_BASE =
  process.env.NEXT_PUBLIC_API_BASE ||
  process.env.NEXT_PUBLIC_API_URL ||
  process.env.NEXT_PUBLIC_GATEWAY_URL ||
  '';

interface FetchOptions extends RequestInit {
  params?: Record<string, string>;
}

/**
 * Generic API fetch wrapper with error handling
 */
async function apiFetch<T>(
  endpoint: string,
  options: FetchOptions = {}
): Promise<T> {
  const { params, ...fetchOptions } = options;

  let url = `${API_BASE}${endpoint}`;
  
  // Add query parameters if provided
  if (params) {
    const queryString = new URLSearchParams(params).toString();
    url += `?${queryString}`;
  }

  const response = await fetch(url, {
    credentials: 'include',
    cache: 'no-store',
    headers: {
      'Content-Type': 'application/json',
      ...fetchOptions.headers,
    },
    ...fetchOptions,
  });

  // Handle non-JSON responses (e.g., 204 No Content)
  if (response.status === 204) {
    return {} as T;
  }

  const data = await response.json();

  if (!response.ok) {
    throw new Error(data.error || data.message || `API Error: ${response.status}`);
  }

  return data;
}

// ============================================================================
// Authentication API (/auth)
// ============================================================================

export const authApi = {
  /**
   * POST /auth/login - Login user
   */
  login: async (username: string, password: string) => {
    return apiFetch<{ token?: string; user?: any }>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    });
  },

  /**
   * POST /auth/signup - Register new user
   */
  signup: async (data: {
    username: string;
    password: string;
    email: string;
    firstName: string;
    lastName: string;
  }) => {
    return apiFetch<{ token?: string; user?: any }>('/auth/signup', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  /**
   * POST /auth/logout - Logout user
   */
  logout: async () => {
    return apiFetch<void>('/auth/logout', {
      method: 'POST',
    });
  },

  /**
   * GET /auth/me - Get current user info
   */
  me: async () => {
    return apiFetch<any>('/auth/me');
  },
};

// ============================================================================
// Projects API (/api/projects)
// ============================================================================

export const projectsApi = {
  /**
   * GET /api/projects - List all projects
   */
  list: async () => {
    return apiFetch<any[]>('/api/projects');
  },

  /**
   * GET /api/projects/{id} - Get project by ID
   */
  get: async (projectId: string) => {
    return apiFetch<any>(`/api/projects/${projectId}`);
  },

  /**
   * POST /api/projects - Create new project
   */
  create: async (data: {
    name: string;
    description?: string;
    tags?: Record<string, string>;
  }) => {
    return apiFetch<any>('/api/projects', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  /**
   * PUT /api/projects/{id} - Update project
   */
  update: async (projectId: string, data: {
    name?: string;
    description?: string;
    tags?: Record<string, string>;
  }) => {
    return apiFetch<any>(`/api/projects/${projectId}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  },

  /**
   * DELETE /api/projects/{id} - Delete project
   */
  delete: async (projectId: string) => {
    return apiFetch<void>(`/api/projects/${projectId}`, {
      method: 'DELETE',
    });
  },

  /**
   * GET /api/projects/{id}/resources - List project resources
   */
  getResources: async (projectId: string) => {
    return apiFetch<any[]>(`/api/projects/${projectId}/resources`);
  },

  /**
   * GET /api/projects/{id}/summary - Get project summary stats
   */
  getSummary: async (projectId: string) => {
    return apiFetch<any>(`/api/projects/${projectId}/summary`);
  },

  /**
   * POST /api/projects/{id}/assign/{resId} - Manually assign resource
   */
  assignResource: async (projectId: string, resId: string) => {
    return apiFetch<any>(`/api/projects/${projectId}/assign/${resId}`, {
      method: 'POST',
    });
  },

  /**
   * POST /api/projects/{id}/rules - Create assignment rule
   */
  createRule: async (
    projectId: string,
    rule: {
      tagConditions: Record<string, any>;
      priority: number;
      description: string;
    }
  ) => {
    return apiFetch<any>(`/api/projects/${projectId}/rules`, {
      method: 'POST',
      body: JSON.stringify(rule),
    });
  },
};

// ============================================================================
// Credentials API (/api/credentials)
// ============================================================================

export const credentialsApi = {
  /**
   * GET /api/credentials - List credentials
   */
  list: async () => {
    return apiFetch<any[]>('/api/credentials');
  },

  /**
   * POST /api/credentials/aws - Connect AWS
   */
  connectAWS: async (data: {
    roleArn: string;
    externalId: string;
    friendlyName: string;
  }) => {
    return apiFetch<any>('/api/credentials/aws', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  /**
   * POST /api/credentials/gcp - Connect GCP
   */
  connectGCP: async (data: {
    serviceAccountJson: string;
    projectId: string;
    friendlyName: string;
  }) => {
    return apiFetch<any>('/api/credentials/gcp', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  /**
   * POST /api/credentials/azure - Connect Azure
   */
  connectAzure: async (data: {
    clientId: string;
    clientSecret: string;
    tenantId: string;
    subscriptionId: string;
  }) => {
    return apiFetch<any>('/api/credentials/azure', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  /**
   * POST /api/credentials/digitalocean - Connect DigitalOcean
   */
  connectDigitalOcean: async (data: {
    apiToken: string;
    friendlyName: string;
  }) => {
    return apiFetch<any>('/api/credentials/digitalocean', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  /**
   * DELETE /api/credentials/{id} - Delete credential
   */
  delete: async (id: string) => {
    return apiFetch<void>(`/api/credentials/${id}`, {
      method: 'DELETE',
    });
  },
};

// ============================================================================
// Costs API (/api/costs)
// ============================================================================

export const costsApi = {
  /**
   * GET /api/costs/credentials/{id} - Get costs for credential
   */
  getCredentialCosts: async (
    credentialId: string,
    startDate: string,
    endDate: string
  ) => {
    return apiFetch<any>(`/api/costs/credentials/${credentialId}`, {
      params: { startDate, endDate },
    });
  },

  /**
   * GET /api/costs/projects/{id}/total - Get total project cost
   */
  getProjectTotal: async (
    projectId: string,
    startDate: string,
    endDate: string
  ) => {
    return apiFetch<any>(`/api/costs/projects/${projectId}/total`, {
      params: { startDate, endDate },
    });
  },

  /**
   * GET /api/costs/credentials/{id}/trend - Get daily cost trend
   */
  getCredentialTrend: async (
    credentialId: string,
    startDate: string,
    endDate: string
  ) => {
    return apiFetch<any>(`/api/costs/credentials/${credentialId}/trend`, {
      params: { startDate, endDate },
    });
  },
};

// ============================================================================
// Users API (/api/users)
// ============================================================================

export const usersApi = {
  /**
   * GET /api/users - List users (Admin/Search)
   */
  list: async () => {
    return apiFetch<any[]>('/api/users');
  },

  /**
   * GET /api/users/me - Get authenticated user
   */
  me: async () => {
    return apiFetch<any>('/api/users/me');
  },

  /**
   * POST /api/users - Create user (Admin)
   */
  create: async (data: {
    username: string;
    password: string;
    email: string;
    firstName: string;
    lastName: string;
    role?: string;
  }) => {
    return apiFetch<any>('/api/users', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },
};

// Export a combined API object for convenience
export const api = {
  auth: authApi,
  projects: projectsApi,
  credentials: credentialsApi,
  costs: costsApi,
  users: usersApi,
};

export default api;
