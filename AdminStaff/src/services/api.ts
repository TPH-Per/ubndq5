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
