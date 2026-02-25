// Citizen API Service - Connect to Backend at port 8081
// In development, Vite proxy forwards /api/* to localhost:8081
// In production, use the full backend URL

const API_BASE_URL = import.meta.env.PROD
    ? 'http://localhost:8081/api/citizen'  // Production: direct to backend
    : '/api/citizen';                       // Development: use Vite proxy

// Generic fetch wrapper with error handling
async function fetchAPI<T>(endpoint: string, options?: RequestInit): Promise<T> {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        headers: {
            'Content-Type': 'application/json',
            ...options?.headers,
        },
        ...options,
    });

    const data = await response.json();

    if (!response.ok || data.success === false) {
        throw new Error(data.message || 'API request failed');
    }

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
    maxCapacity: number;  // Backend returns 'maxCapacity' not 'total'
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
    citizenId: string;
    phoneNumber: string;
    notes?: string;
    // Zalo account info (optional - for Zalo Mini App)
    zaloId?: string;
    zaloName?: string;
}

export interface AppointmentResponse {
    id: number;
    code: string;
    queueDisplay: string;
    appointmentDate: string;
    appointmentTime: string;
    procedureName: string;
    status: string;
    zaloLinked?: boolean;  // Whether Zalo account was linked
}

export interface ApplicationDetail {
    id: number;
    code: string;
    procedureName: string;
    status: string;
    queueDisplay: string;
    createdAt: string;
    appointmentDate?: string;
    appointmentTime?: string;
    citizenName: string;
    citizenId: string;
    zaloLinked?: boolean;
}

export interface ApplicationHistory {
    action: string;
    phaseFrom: string | null;
    phaseTo: string;
    timestamp: string;
    staff: string | null;
    counter: string | null;
    content: string | null;
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

export interface FeedbackRequest {
    type: number; // 1=Góp ý, 2=Khiếu nại, 3=Khen ngợi
    title: string;
    content: string;
    citizenId: string;
    citizenName?: string;
    phone?: string;
    applicationId?: number;
}

export interface Feedback {
    id: number;
    feedbackCode: string;
    type: number;
    title: string;
    content: string;
    status: string;
    createdAt: string;
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
 * Get available time slots for a date
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
 * Get citizen's appointments
 */
export async function getMyAppointments(cccd: string, status?: string): Promise<ApplicationDetail[]> {
    let params = `?cccd=${cccd}`;
    if (status) params += `&status=${status}`;
    return fetchAPI<ApplicationDetail[]>(`/appointments${params}`);
}

/**
 * Cancel an appointment
 */
export async function cancelAppointment(id: number, cccd: string): Promise<void> {
    return fetchAPI<void>(`/appointments/${id}/cancel?cccd=${cccd}`, {
        method: 'POST',
    });
}

/**
 * Get appointment detail by ID (for queue tracking)
 */
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
}

export async function getAppointmentDetail(id: number, cccd: string): Promise<AppointmentDetail> {
    return fetchAPI<AppointmentDetail>(`/appointments/${id}?cccd=${cccd}`);
}

/**
 * Get citizen's applications (hồ sơ)
 */
export async function getMyApplications(cccd: string, status?: string): Promise<ApplicationDetail[]> {
    let params = `?cccd=${cccd}`;
    if (status) params += `&status=${status}`;
    return fetchAPI<ApplicationDetail[]>(`/applications${params}`);
}

/**
 * Get application detail
 */
export async function getApplicationDetail(id: number, cccd: string): Promise<ApplicationDetail> {
    return fetchAPI<ApplicationDetail>(`/applications/${id}?cccd=${cccd}`);
}

/**
 * Get application history
 */
export async function getApplicationHistory(id: number, cccd: string): Promise<ApplicationHistory[]> {
    return fetchAPI<ApplicationHistory[]>(`/applications/${id}/history?cccd=${cccd}`);
}

/**
 * Check queue status by ticket code
 */
export async function getQueueStatus(ticketCode: string): Promise<QueueStatus> {
    return fetchAPI<QueueStatus>(`/queue/${ticketCode}`);
}

/**
 * Submit feedback/report
 */
export async function submitFeedback(data: FeedbackRequest): Promise<{ id: number; title: string; status: string }> {
    return fetchAPI<{ id: number; title: string; status: string }>('/reports', {
        method: 'POST',
        body: JSON.stringify(data),
    });
}

/**
 * Get citizen's feedbacks
 */
export async function getMyFeedbacks(cccd: string): Promise<Feedback[]> {
    return fetchAPI<Feedback[]>(`/reports?cccd=${cccd}`);
}
