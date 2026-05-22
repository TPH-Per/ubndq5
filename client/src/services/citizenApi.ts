// Citizen API Service
// Use VITE_API_BASE_URL in production build, fallback to Vite proxy in development.
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api/citizen';

interface ApiEnvelope<T> {
    success: boolean;
    code?: string;
    message?: string;
    data: T;
}

export async function safeFetch<T>(url: string, options?: RequestInit): Promise<ApiEnvelope<T>> {
    const response = await fetch(url, {
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            'ngrok-skip-browser-warning': 'true',
            ...options?.headers,
        },
        ...options,
    });

    const contentType = response.headers.get('content-type') ?? '';
    if (!contentType.includes('application/json')) {
        throw new Error(`API error ${response.status}: unexpected response format`);
    }

    let data: ApiEnvelope<T>;
    try {
        data = await response.json();
    } catch {
        throw new Error(`API error ${response.status}: invalid JSON response`);
    }

    if (!response.ok || data.success === false) {
        throw new Error(data.message ?? data.code ?? `API request failed (${response.status})`);
    }

    return data;
}

// Generic fetch wrapper with error handling
async function fetchAPI<T>(endpoint: string, options?: RequestInit): Promise<T> {
    const data = await safeFetch<T>(`${API_BASE_URL}${endpoint}`, options);
    return data.data;
}

// ==================== TYPES ====================

export interface Specialty {
    id: number;
    name: string;
    description: string;
}

export interface Procedure {
    id: number;
    code: string;
    name: string;
    description: string;
    processingDays: number;
    requiredDocuments: string[];
}

export interface TimeSlot {
    time: string;
    available: number;
    maxCapacity: number;
}

export interface AvailableSlotsResponse {
    date: string;
    slots: TimeSlot[];
}

export interface CreateAppointmentRequest {
    procedureId: number;
    appointmentDate: string;
    appointmentTime: string;
    citizenName: string;
    citizenCccd: string;      // bắt buộc, 12 chữ số
    citizenPhone: string;
    citizenEmail?: string;
    zaloId: string;           // bắt buộc — định danh người đặt lịch
    zaloName?: string;
    notes?: string;            // Ghi chú tuỳ chọn từ công dân
}

export interface AppointmentResponse {
    id: number;
    code: string;
    queueDisplay: string;
    appointmentDate: string;
    appointmentTime: string;
    procedureName: string;
    status: string;
    zaloLinked?: boolean;
}

export interface ApplicationSummary {
    id: number;
    code: string;
    procedureName: string;
    status: string;
    queueDisplay?: string | null;
    createdAt: string;
    appointmentDate?: string;
    appointmentTime?: string;
    citizenName?: string;
    citizenCccd?: string;
    counter?: string | null;
    zaloLinked?: boolean;
}

export interface ApplicationHistory {
    action: string;
    statusFrom: string | null;
    statusTo: string;
    createdAt: string;
    staffName: string | null;
    content: string | null;
}

export interface ApplicationDetail extends ApplicationSummary {
    procedureCode?: string;
    statusCode?: number;
    deadline?: string | null;
    phone?: string | null;
    requiredDocuments?: string[];
    description?: string | null;
}

export interface QueueStatus {
    ticketNumber: number;
    ticketDisplay: string;
    currentServing: number;
    waitingCount: number;
    estimatedWaitMinutes: number;
    status: string;
    procedureName: string;
}

export interface FeedbackReply {
    id: number;
    content: string;
    staffName: string;
    createdAt: string;
}

export interface FeedbackRequest {
    type: number; // 1=Góp ý, 2=Khiếu nại, 3=Khen ngợi
    title?: string;
    content?: string;
    citizenCccd: string;    // 12 digits CCCD
    citizenName?: string;
    phone?: string;
    applicationId?: number; // optional: link to application
    zaloId?: string;
    rating?: number;        // 1-5 star rating
}

export interface Feedback {
    id: number;
    feedbackCode: string;
    type: number;
    title: string;
    content: string;
    status: string | number;
    createdAt: string;
    applicationCode?: string;
    applicationId?: number;
    replies: FeedbackReply[];
}

// ==================== API FUNCTIONS ====================

/**
 * Get all specialties/procedure types
 */
export async function getSpecialties(): Promise<Specialty[]> {
    return fetchAPI<Specialty[]>('/specialties');
}

/**
 * Get procedures by specialty
 */
export async function getProcedures(specialtyId?: number): Promise<Procedure[]> {
    const params = specialtyId ? `?specialtyId=${specialtyId}` : '';
    return fetchAPI<Procedure[]>(`/procedures${params}`);
}

/**
 * Get available time slots for a date (no PII in URL)
 */
export async function getAvailableSlots(date: string): Promise<AvailableSlotsResponse> {
    return fetchAPI<AvailableSlotsResponse>(`/appointments/available-slots?date=${date}`);
}

/**
 * Create a new appointment
 */
export async function createAppointment(data: CreateAppointmentRequest): Promise<AppointmentResponse> {
    return fetchAPI<AppointmentResponse>('/appointments', {
        method: 'POST',
        body: JSON.stringify(data),
    });
}

/**
 * Get citizen's appointments by Zalo account
 * Maps to: POST /api/citizen/appointments/search
 */
export async function getMyAppointments(zaloId: string, status?: string): Promise<ApplicationSummary[]> {
    return fetchAPI<ApplicationSummary[]>('/appointments/search', {
        method: 'POST',
        body: JSON.stringify({ zaloId, status }),
    });
}

/**
 * Cancel an appointment — authenticated by zaloId
 * Maps to: POST /api/citizen/appointments/{id}/cancel
 */
export async function cancelAppointment(id: number, zaloId: string): Promise<void> {
    return fetchAPI<void>(`/appointments/${id}/cancel`, {
        method: 'POST',
        body: JSON.stringify({ zaloId }),
    });
}

export interface AppointmentDetail {
    id: number;
    code: string;
    procedureName: string;
    procedureCode: string;
    status: string;
    queueDisplay: string;
    queueNumber: number;
    citizenName: string;
    createdAt: string;
    deadline: string | null;
    peopleAhead: number;
    estimatedWaitMinutes: number;
    currentServing: string | null;
    appointmentDate: string | null;
    appointmentTime: string | null;
    counter: string | null;
    description: string | null;
    requiredDocuments: string[];
}

export interface ZaloProfileSyncRequest {
    zaloId: string;
    zaloName?: string;
    avatar?: string;
    oaUserId?: string;
    phoneNumber?: string;
}

export interface ZaloProfileSyncResponse {
    zaloId: string;
    zaloName?: string;
    avatar?: string;
    oaUserId?: string;
    phoneNumber?: string;
    isActive: boolean;
    lastSyncedAt?: string;
}

/**
 * Get appointment detail — authenticated by zaloId
 * Maps to: POST /api/citizen/appointments/{id}/view
 */
export async function getAppointmentDetail(id: number, zaloId: string): Promise<AppointmentDetail> {
    return fetchAPI<AppointmentDetail>(`/appointments/${id}/view`, {
        method: 'POST',
        body: JSON.stringify({ zaloId }),
    });
}

/**
 * Get citizen's applications (hồ sơ) by Zalo account
 * Maps to: POST /api/citizen/applications/search
 */
export async function getMyApplications(zaloId: string, status?: string): Promise<ApplicationSummary[]> {
    return fetchAPI<ApplicationSummary[]>('/applications/search', {
        method: 'POST',
        body: JSON.stringify({ zaloId, status }),
    });
}

/**
 * Get application detail — authenticated by zaloId
 * Maps to: POST /api/citizen/applications/{id}/view
 */
export async function getApplicationDetail(id: number, zaloId: string): Promise<ApplicationDetail> {
    return fetchAPI<ApplicationDetail>(`/applications/${id}/view`, {
        method: 'POST',
        body: JSON.stringify({ zaloId }),
    });
}

/**
 * Get application history — authenticated by zaloId
 * Maps to: POST /api/citizen/applications/{id}/history
 */
export async function getApplicationHistory(id: number, zaloId: string): Promise<ApplicationHistory[]> {
    return fetchAPI<ApplicationHistory[]>(`/applications/${id}/history`, {
        method: 'POST',
        body: JSON.stringify({ zaloId }),
    });
}

/**
 * Check queue status by ticket code (no PII in URL)
 * Maps to: GET /api/citizen/queue/{ticketCode}
 */
export async function getQueueStatus(ticketCode: string): Promise<QueueStatus> {
    return fetchAPI<QueueStatus>(`/queue/${ticketCode}`);
}

/**
 * Submit feedback/report — CCCD in body via POST
 * Maps to: POST /api/citizen/reports
 */
export async function submitFeedback(data: FeedbackRequest): Promise<{ id: number; title: string; status: string }> {
    return fetchAPI<{ id: number; title: string; status: string }>('/reports', {
        method: 'POST',
        body: JSON.stringify(data),
    });
}

/**
 * Get citizen's feedbacks — CCCD + zaloId sent in POST body, never in URL.
 * zaloId is used to verify ownership on the backend.
 * Maps to: POST /api/citizen/reports/search
 */
export async function getMyFeedbacks(cccd: string, zaloId?: string): Promise<Feedback[]> {
    return fetchAPI<Feedback[]>('/reports/search', {
        method: 'POST',
        body: JSON.stringify({ cccd, zaloId }),
    });
}

export async function syncZaloProfile(data: ZaloProfileSyncRequest): Promise<ZaloProfileSyncResponse> {
    return fetchAPI<ZaloProfileSyncResponse>('/zalo/profile/sync', {
        method: 'POST',
        body: JSON.stringify(data),
    });
}
