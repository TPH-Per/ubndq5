import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios';

/**
 * API Service - Cấu hình Axios để gọi Backend API
 * 
 * Bao gồm:
 * - Base URL configuration
 * - Request interceptor: Tự động thêm JWT token
 * - Response interceptor: Xử lý lỗi chung
 */

// Tạo axios instance với cấu hình mặc định
const api: AxiosInstance = axios.create({
    baseURL: 'http://localhost:8081/api',  // URL của Spring Boot backend
    timeout: 10000,                         // Timeout 10 giây
    headers: {
        'Content-Type': 'application/json',
    },
});

/**
 * Request Interceptor
 * 
 * Chạy TRƯỚC mỗi request gửi đi.
 * Dùng để tự động thêm JWT token vào header.
 */
api.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        // Lấy token từ localStorage
        const token = localStorage.getItem('token');

        // Nếu có token, thêm vào header Authorization
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        return config;
    },
    (error: AxiosError) => {
        return Promise.reject(error);
    }
);

/**
 * Response Interceptor
 * 
 * Chạy SAU mỗi response nhận về.
 * Dùng để xử lý lỗi chung (401, 500, etc.)
 */
api.interceptors.response.use(
    (response) => {
        // Response thành công - trả về data
        return response;
    },
    (error: AxiosError) => {
        // Xử lý lỗi
        if (error.response) {
            const status = error.response.status;

            switch (status) {
                case 401:
                    // Token hết hạn hoặc không hợp lệ
                    console.warn('Unauthorized - Token expired or invalid');
                    // Xóa token và redirect về login
                    localStorage.removeItem('token');
                    localStorage.removeItem('user');
                    // Chỉ redirect nếu không phải đang ở trang login
                    if (!window.location.pathname.includes('/login')) {
                        window.location.href = '/login';
                    }
                    break;

                case 403:
                    console.warn('Forbidden - Access denied');
                    break;

                case 500:
                    console.error('Server Error:', error.response.data);
                    break;
            }
        } else if (error.request) {
            // Request được gửi nhưng không nhận được response
            console.error('Network Error - Server không phản hồi');
        }

        return Promise.reject(error);
    }
);

export default api;

// =============================================
// AUTH API - Các hàm gọi API authentication
// =============================================

export interface LoginRequest {
    maNhanVien: string;
    password: string;
}

export interface UserData {
    id: number;
    maNhanVien: string;
    hoTen: string;
    email: string;
    soDienThoai: string;
    roleName: string;
    roleDisplayName: string;
    tenQuay: string | null;
    quayId: number | null;
    trangThai: boolean;
    lanDangNhapCuoi: string | null;
}

export interface LoginResponse {
    token: string;
    tokenType: string;
    expiresIn: number;
    user: UserData;
}

export interface ApiResponse<T> {
    success: boolean;
    code: string;
    message: string;
    data: T;
    timestamp: string;
}

/**
 * Đăng nhập
 */
export const authApi = {
    login: (data: LoginRequest) =>
        api.post<ApiResponse<LoginResponse>>('/auth/login', data),

    logout: () =>
        api.post<ApiResponse<null>>('/auth/logout'),

    getMe: () =>
        api.get<ApiResponse<UserData>>('/auth/me'),

    validateToken: () =>
        api.get<ApiResponse<{ valid: boolean }>>('/auth/validate'),
};

// =============================================
// USER API - Admin quản lý users
// =============================================

export interface CreateUserRequest {
    maNhanVien: string;
    hoTen: string;
    email: string;
    soDienThoai?: string;
    password: string;
    roleId: number;
    quayId?: number;
}

export interface UpdateUserRequest {
    hoTen?: string;
    email?: string;
    soDienThoai?: string;
    password?: string;
    roleId?: number;
    quayId?: number;
    trangThai?: boolean;
}

export const userApi = {
    // Lấy danh sách tất cả users
    getAll: () =>
        api.get<ApiResponse<UserData[]>>('/admin/users'),

    // Lấy user theo ID
    getById: (id: number) =>
        api.get<ApiResponse<UserData>>(`/admin/users/${id}`),

    // Tạo user mới
    create: (data: CreateUserRequest) =>
        api.post<ApiResponse<UserData>>('/admin/users', data),

    // Cập nhật user
    update: (id: number, data: UpdateUserRequest) =>
        api.put<ApiResponse<UserData>>(`/admin/users/${id}`, data),

    // Xóa user (soft delete)
    delete: (id: number) =>
        api.delete<ApiResponse<null>>(`/admin/users/${id}`),

    // Reset password
    resetPassword: (id: number, newPassword: string) =>
        api.post<ApiResponse<UserData>>(`/admin/users/${id}/reset-password`, { newPassword }),
};

export interface RoleData {
    id: number;
    roleName: string;
    displayName: string;
}

export const roleApi = {
    getAll: () =>
        api.get<ApiResponse<RoleData[]>>('/admin/roles'),
};

// =============================================
// PROFILE API - Staff cập nhật thông tin cá nhân
// =============================================

export interface UpdateProfileRequest {
    hoTen?: string;
    email?: string;
    soDienThoai?: string;
    oldPassword?: string;
    newPassword?: string;
}

export const profileApi = {
    // Lấy thông tin profile của user đang đăng nhập
    getMyProfile: () =>
        api.get<ApiResponse<UserData>>('/profile'),

    // Cập nhật thông tin profile của user đang đăng nhập
    updateMyProfile: (data: UpdateProfileRequest) =>
        api.put<ApiResponse<UserData>>('/profile', data),
};

// =============================================
// QUAY API - Admin CRUD quầy
// =============================================

export interface QuayData {
    id: number;
    maQuay: string;
    tenQuay: string;
    viTri: string;
    prefixSo: string;
    chuyenMonId: number;
    tenChuyenMon: string;
    trangThai: boolean;
    ghiChu: string;
    ngayTao: string;
    soNhanVien: number;
}

export interface CreateQuayRequest {
    maQuay: string;
    tenQuay: string;
    viTri?: string;
    prefixSo: string;
    chuyenMonId: number;
    ghiChu?: string;
}

export interface UpdateQuayRequest {
    tenQuay?: string;
    viTri?: string;
    prefixSo?: string;
    chuyenMonId?: number;
    ghiChu?: string;
    trangThai?: boolean;
}

export const quayApi = {
    // Lấy danh sách tất cả quầy
    getAll: () =>
        api.get<ApiResponse<QuayData[]>>('/admin/quays'),

    // Lấy quầy theo ID
    getById: (id: number) =>
        api.get<ApiResponse<QuayData>>(`/admin/quays/${id}`),

    // Tạo quầy mới
    create: (data: CreateQuayRequest) =>
        api.post<ApiResponse<QuayData>>('/admin/quays', data),

    // Cập nhật quầy
    update: (id: number, data: UpdateQuayRequest) =>
        api.put<ApiResponse<QuayData>>(`/admin/quays/${id}`, data),

    // Xóa quầy (soft delete)
    delete: (id: number) =>
        api.delete<ApiResponse<null>>(`/admin/quays/${id}`),
};

// =============================================
// CHUYEN MON API - Quản lý chuyên môn
// =============================================

export interface ChuyenMonData {
    id: number;
    maChuyenMon: string;
    tenChuyenMon: string;
    moTa: string;
    trangThai: boolean;
    ngayTao: string;
    soQuay: number;
    soThuTuc: number;
}

export interface CreateChuyenMonRequest {
    maChuyenMon: string;
    tenChuyenMon: string;
    moTa?: string;
}

export interface UpdateChuyenMonRequest {
    tenChuyenMon?: string;
    moTa?: string;
    trangThai?: boolean;
}

export const chuyenMonApi = {
    // Lấy danh sách tất cả chuyên môn
    getAll: () =>
        api.get<ApiResponse<ChuyenMonData[]>>('/admin/chuyenmons'),

    // Lấy chuyên môn theo ID
    getById: (id: number) =>
        api.get<ApiResponse<ChuyenMonData>>(`/admin/chuyenmons/${id}`),

    // Tạo chuyên môn mới
    create: (data: CreateChuyenMonRequest) =>
        api.post<ApiResponse<ChuyenMonData>>('/admin/chuyenmons', data),

    // Cập nhật chuyên môn
    update: (id: number, data: UpdateChuyenMonRequest) =>
        api.put<ApiResponse<ChuyenMonData>>(`/admin/chuyenmons/${id}`, data),

    // Xóa chuyên môn (soft delete)
    delete: (id: number) =>
        api.delete<ApiResponse<null>>(`/admin/chuyenmons/${id}`),
};

// =============================================
// LOAI THU TUC API - Quản lý loại thủ tục
// =============================================

export interface LoaiThuTucData {
    id: number;
    maThuTuc: string;
    tenThuTuc: string;
    moTa: string | null;
    chuyenMonId: number;
    tenChuyenMon: string;
    thoiGianXuLy: number;
    giayToYeuCau: string | null;
    formSchema: Record<string, unknown> | null;
    thuTu: number;
    trangThai: boolean;
    ngayTao: string;
    soHoSo: number;
}

export interface CreateLoaiThuTucRequest {
    maThuTuc: string;
    tenThuTuc: string;
    moTa?: string;
    chuyenMonId: number;
    thoiGianXuLy?: number;
    giayToYeuCau?: string;
    formSchema?: Record<string, unknown>;
    thuTu?: number;
}

export interface UpdateLoaiThuTucRequest {
    tenThuTuc?: string;
    moTa?: string;
    chuyenMonId?: number;
    thoiGianXuLy?: number;
    giayToYeuCau?: string;
    formSchema?: Record<string, unknown>;
    thuTu?: number;
    trangThai?: boolean;
}

export const loaiThuTucApi = {
    // Lấy danh sách tất cả loại thủ tục (Admin)
    getAll: () =>
        api.get<ApiResponse<LoaiThuTucData[]>>('/admin/loaithutucs'),

    // Lấy loại thủ tục theo ID
    getById: (id: number) =>
        api.get<ApiResponse<LoaiThuTucData>>(`/admin/loaithutucs/${id}`),

    // Lấy loại thủ tục theo chuyên môn
    getByChuyenMon: (chuyenMonId: number) =>
        api.get<ApiResponse<LoaiThuTucData[]>>(`/admin/loaithutucs/by-chuyenmon/${chuyenMonId}`),

    // Tạo loại thủ tục mới
    create: (data: CreateLoaiThuTucRequest) =>
        api.post<ApiResponse<LoaiThuTucData>>('/admin/loaithutucs', data),

    // Cập nhật loại thủ tục
    update: (id: number, data: UpdateLoaiThuTucRequest) =>
        api.put<ApiResponse<LoaiThuTucData>>(`/admin/loaithutucs/${id}`, data),

    // Xóa loại thủ tục (soft delete)
    delete: (id: number) =>
        api.delete<ApiResponse<null>>(`/admin/loaithutucs/${id}`),
};

// Public API cho citizens (không cần đăng nhập)
export const publicLoaiThuTucApi = {
    // Lấy danh sách loại thủ tục đang hoạt động
    getAll: () =>
        api.get<ApiResponse<LoaiThuTucData[]>>('/public/loaithutucs'),

    // Lấy loại thủ tục theo ID
    getById: (id: number) =>
        api.get<ApiResponse<LoaiThuTucData>>(`/public/loaithutucs/${id}`),

    // Lấy loại thủ tục theo chuyên môn
    getByChuyenMon: (chuyenMonId: number) =>
        api.get<ApiResponse<LoaiThuTucData[]>>(`/public/loaithutucs/by-chuyenmon/${chuyenMonId}`),
};

// =============================================
// QUEUE MANAGEMENT API - Staff quản lý hàng chờ
// =============================================

export interface LichHenData {
    id: number;
    maLichHen: string;
    soThuTu: number;
    soThuTuDisplay: string;
    cccd: string;
    hoTenCongDan: string;
    soDienThoai: string | null;
    tenThuTuc: string;
    maThuTuc: string;
    tenQuay: string;
    maQuay: string;
    ngayHen: string;
    thoiGianDuKien: string | null;
    thoiGianGoiSo: string | null;
    thoiGianBatDauXuLy: string | null;
    thoiGianKetThuc: string | null;
    trangThai: number;
    trangThaiText: string;
    tenNhanVienXuLy: string | null;
    lyDoHuy: string | null;
}

export interface QueueDashboardData {
    counterId: number;
    counterName: string;
    counterCode: string;
    currentProcessing: ApplicationData | null;
    waitingList: ApplicationData[];
    totalWaiting: number;
    totalCompleted: number;
    totalCancelled: number;
    averageProcessingTime: number | null;
}

// Application data from queue response
export interface ApplicationData {
    id: number;
    applicationCode: string;
    procedureId: number;
    procedureCode: string;
    procedureName: string;
    citizenId: string;
    citizenName: string;
    citizenPhone: string | null;
    zaloAccountId: number | null;
    zaloName: string | null;
    currentPhase: number;
    phaseName: string;
    queueNumber: number;
    queuePrefix: string;
    queueDisplay: string;
    appointmentDate: string | null;
    expectedTime: string | null;
    deadline: string | null;
    priority: number;
    priorityName: string;
    cancelReason: string | null;
    cancelType: number | null;
    createdAt: string;
    updatedAt: string | null;
}

export interface QueueStatusRequest {
    trangThai?: number;
    lyDo?: string;
    ghiChu?: string;
}

export const queueApi = {
    // Lấy dashboard tổng hợp (quầy, lượt đang xử lý, danh sách chờ, thống kê)
    getDashboard: () =>
        api.get<ApiResponse<QueueDashboardData>>('/staff/queue/dashboard'),

    // Lấy danh sách người đang chờ
    getWaitingList: () =>
        api.get<ApiResponse<LichHenData[]>>('/staff/queue/waiting'),

    // Lấy lượt đang xử lý hiện tại
    getCurrentProcessing: () =>
        api.get<ApiResponse<LichHenData | null>>('/staff/queue/current'),

    // Gọi số tiếp theo (hoặc số cụ thể)
    callNext: (id?: number) => api.post<ApiResponse<LichHenData>>('/staff/queue/call-next', { id }),

    // Lấy danh sách slot hẹn bổ sung
    getSlots: (date: string) => api.get<ApiResponse<{
        morning: { time: string; booked: boolean }[];
        afternoon: { time: string; booked: boolean }[];
    }>>('/staff/queue/slots', { params: { date } }),

    // Hẹn bổ sung
    supplement: (id: number, data: { appointmentDate: string; appointmentTime: string }) =>
        api.post<ApiResponse<LichHenData>>(`/staff/queue/${id}/supplement`, data),

    // Tiếp nhận hồ sơ (chuyển trạng thái sang RECEIVED)
    receive: (id: number, data?: { appointmentDate: string; expectedTime: string }) =>
        api.post<ApiResponse<LichHenData>>(`/staff/queue/${id}/receive`, data),

    // Hoàn thành lượt đang xử lý
    complete: (id: number, ghiChu?: string) =>
        api.post<ApiResponse<LichHenData>>(`/staff/queue/${id}/complete`, { ghiChu }),

    // Hủy / Đánh dấu khách không đến
    cancel: (id: number, lyDo: string, trangThai?: number) =>
        api.post<ApiResponse<LichHenData>>(`/staff/queue/${id}/cancel`, {
            lyDo,
            trangThai: trangThai ?? 3  // Default: KHONG_DEN = 3
        }),
};

// =============================================
// HOSO MANAGEMENT API - Staff quản lý hồ sơ
// =============================================

export interface HoSoData {
    id: number;
    maHoSo: string;
    cccd: string;
    hoTenCongDan: string;
    soDienThoai: string | null;
    tenThuTuc: string;
    maThuTuc: string;
    tenQuay: string;
    trangThai: number;
    trangThaiText: string;
    doUuTien: number;
    ngayNop: string;
    hanXuLy: string | null;
    ngayHoanThanh: string | null;
    nguonGoc: string;
    maLichHen: string | null;
}

export interface HoSoDetailData extends HoSoData {
    email: string | null;
    diaChi: string | null;
    loaiThuTucId: number;
    thoiGianXuLyQuyDinh: number;
    thongTinHoSo: Record<string, unknown> | null;
    fileDinhKem: Record<string, unknown>[] | null;
    ghiChu: string | null;
    lichHen: LichHenData | null;
    lichSuXuLy: {
        nguoiXuLy: string;
        hanhDong: string;
        trangThaiCu: string;
        trangThaiMoi: string;
        noiDung: string;
        thoiGian: string;
    }[];
}

export interface HoSoDashboardData {
    tongSoHoSo: number;
    choXuLy: number;
    dangXuLy: number;
    hoanThanh: number;
    treHan: number;
}

export interface CreateHoSoRequest {
    cccd: string;
    hoTen: string;
    soDienThoai?: string;
    email?: string;
    diaChi?: string;
    loaiThuTucId: number;
    thongTinHoSo?: Record<string, unknown>;
    fileDinhKem?: Record<string, unknown>[];
    ghiChu?: string;
    doUuTien?: number;
    confirmDuplicate?: boolean;
}

export interface UpdateHoSoRequest {
    hoTen?: string;
    soDienThoai?: string;
    diaChi?: string;
    thongTinHoSo?: Record<string, unknown>;
    fileDinhKem?: Record<string, unknown>[];
    ghiChu?: string;
    doUuTien?: number;
    ngayHoanThanh?: string;
}

export interface ChangeStatusRequest {
    trangThaiMoi: number;
    noiDung?: string;
    ghiChu?: string;
    ngayHen?: string;
    gioHen?: string;
}

export const hoSoApi = {
    // Lấy dashboard thống kê
    getDashboard: () =>
        api.get<ApiResponse<HoSoDashboardData>>('/staff/hoso/dashboard'),

    // Lấy danh sách hồ sơ (có thể filter theo trạng thái)
    getList: (trangThai?: number) =>
        api.get<ApiResponse<HoSoData[]>>('/staff/hoso', { params: { trangThai } }),

    // Lấy chi tiết hồ sơ
    getById: (id: number) =>
        api.get<ApiResponse<HoSoDetailData>>(`/staff/hoso/${id}`),

    // Tạo hồ sơ mới
    create: (data: CreateHoSoRequest) =>
        api.post<ApiResponse<HoSoData>>('/staff/hoso', data),

    // Cập nhật thông tin hồ sơ
    update: (id: number, data: UpdateHoSoRequest) =>
        api.put<ApiResponse<HoSoData>>(`/staff/hoso/${id}`, data),

    // Tạo hồ sơ từ lịch hẹn
    createFromLichHen: (lichHenId: number) =>
        api.post<ApiResponse<HoSoData>>(`/staff/hoso/from-lichhen/${lichHenId}`),

    // Cập nhật trạng thái
    updateStatus: (id: number, data: ChangeStatusRequest) =>
        api.put<ApiResponse<HoSoData>>(`/staff/hoso/${id}/status`, data),
};

// =============================================
// FEEDBACK API - Staff quản lý phản ánh
// =============================================

export interface Feedback {
    id: number;
    type: number;
    title: string;
    content: string;
    citizenName: string;
    citizenId: string;
    applicationCode: string;
    status: number;
    createdAt: string;
    replies: Reply[];
}

export interface Reply {
    id: number;
    content: string;
    staffName: string;
    createdAt: string;
}

export const feedbackApi = {
    getList: (status?: number) => api.get<ApiResponse<Feedback[]>>('/staff/feedbacks', { params: { status } }),
    getDetail: (id: number) => api.get<ApiResponse<Feedback>>(`/staff/feedbacks/${id}`),
    reply: (id: number, content: string) => api.post<ApiResponse<Feedback>>(`/staff/feedbacks/${id}/reply`, { content })
};

