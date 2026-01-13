import { Calendar, FileText, User, Briefcase, CreditCard, Activity, Bell, FileCheck, AlertCircle } from 'lucide-react';

// --- Procedures ---
export const PROCEDURES = [
  {
    id: '1',
    name: 'Đăng ký khai sinh',
    description: 'Thủ tục đăng ký khai sinh cho trẻ em dưới 6 tuổi',
    time: '20 phút',
    docsCount: 3,
    icon: User,
    category: 'Hộ tịch'
  },
  {
    id: '2',
    name: 'Cấp lại thẻ CCCD',
    description: 'Cấp lại thẻ Căn cước công dân gắn chip',
    time: '15 phút',
    docsCount: 2,
    icon: CreditCard,
    category: 'CCCD'
  },
  {
    id: '3',
    name: 'Đăng ký kinh doanh',
    description: 'Đăng ký thành lập hộ kinh doanh cá thể',
    time: '30 phút',
    docsCount: 5,
    icon: Briefcase,
    category: 'Kinh doanh'
  },
  {
    id: '4',
    name: 'Chứng thực giấy tờ',
    description: 'Sao y bản chính các loại giấy tờ',
    time: '10 phút',
    docsCount: 1,
    icon: FileText,
    category: 'Công chứng'
  },
  {
    id: '5',
    name: 'Đăng ký kết hôn',
    description: 'Thủ tục đăng ký kết hôn tại UBND xã/phường',
    time: '45 phút',
    docsCount: 4,
    icon: User,
    category: 'Hộ tịch'
  }
];

// --- Appointments ---
export const MOCK_APPOINTMENTS = [
  {
    id: 'APT001',
    queueNumber: 'A058',
    date: '2025-12-26',
    time: '10:30',
    counter: 'Counter A - Tầng 1',
    procedure: 'Cấp lại thẻ CCCD',
    status: 'waiting', // waiting, serving, ready
    citizenName: 'Nguyễn Văn A'
  },
  {
    id: 'APT002',
    queueNumber: 'B012',
    date: '2025-12-28',
    time: '09:00',
    counter: 'Counter B - Tầng 2',
    procedure: 'Đăng ký khai sinh',
    status: 'upcoming',
    citizenName: 'Nguyễn Văn A'
  },
  {
    id: 'APT003',
    queueNumber: 'C005',
    date: '2025-11-15',
    time: '14:00',
    counter: 'Counter C - Tầng 1',
    procedure: 'Chứng thực giấy tờ',
    status: 'completed',
    citizenName: 'Nguyễn Văn A'
  },
  {
    id: 'APT004',
    queueNumber: 'A099',
    date: '2025-10-20',
    time: '08:30',
    counter: 'Counter A - Tầng 1',
    procedure: 'Đăng ký kinh doanh',
    status: 'cancelled',
    citizenName: 'Nguyễn Văn A'
  }
];

// --- Documents ---
export const MOCK_DOCUMENTS = [
  {
    id: 'DOC001',
    name: 'Sổ hộ khẩu.pdf',
    type: 'PDF',
    size: '2.5 MB',
    date: '2025-01-15',
    status: 'verified',
    category: 'Personal'
  },
  {
    id: 'DOC002',
    name: 'Giấy khai sinh.jpg',
    type: 'Image',
    size: '1.2 MB',
    date: '2025-02-20',
    status: 'verified',
    category: 'Personal'
  },
  {
    id: 'DOC003',
    name: 'Đơn đề nghị cấp CCCD.docx',
    type: 'Word',
    size: '500 KB',
    date: '2025-12-20',
    status: 'pending',
    category: 'Forms'
  },
  {
    id: 'DOC004',
    name: 'Giấy phép kinh doanh.pdf',
    type: 'PDF',
    size: '3.1 MB',
    date: '2025-11-05',
    status: 'rejected',
    category: 'Business'
  }
];

// --- News / Notifications ---
export const MOCK_NEWS = [
  {
    id: 1,
    title: 'Thông báo về việc thay đổi giờ làm việc bộ phận Một cửa',
    summary: 'Từ ngày 01/01/2026, bộ phận Một cửa sẽ làm việc từ 7:30 sáng.',
    date: '2 hours ago',
    image: 'https://images.unsplash.com/photo-1497366216548-37526070297c?auto=format&fit=crop&q=80&w=200'
  },
  {
    id: 2,
    title: 'Triển khai dịch vụ công trực tuyến mức độ 4',
    summary: 'Người dân có thể thực hiện 100% thủ tục tại nhà.',
    date: '1 day ago',
    image: 'https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?auto=format&fit=crop&q=80&w=200'
  },
  {
    id: 3,
    title: 'Hướng dẫn đăng ký tài khoản định danh điện tử VNeID',
    summary: 'Các bước chi tiết để kích hoạt tài khoản mức 2.',
    date: '3 days ago',
    image: 'https://images.unsplash.com/photo-1611162617474-5b21e879e113?auto=format&fit=crop&q=80&w=200'
  }
];

// --- Staff Dashboard Data ---
export const QUEUE_DATA = {
  currentServing: 'A045',
  yourNumber: 'A058',
  waitingCount: 12,
  avgWaitTime: 18,
  counters: [
    { id: 'A', name: 'Counter A', location: 'Tầng 1', current: 'A045', waiting: 12, status: 'active' },
    { id: 'B', name: 'Counter B', location: 'Tầng 2', current: 'B022', waiting: 5, status: 'active' },
    { id: 'C', name: 'Counter C', location: 'Tầng 1', current: '-', waiting: 0, status: 'closed' },
  ]
};

export const WAITING_LIST_MOCK = [
  { id: '1', queueNumber: 'A046', citizenName: 'Trần Thị B', procedure: 'CCCD', time: '10:45', status: 'ready', date: '2025-12-26' },
  { id: '2', queueNumber: 'A047', citizenName: 'Lê Văn C', procedure: 'Khai sinh', time: '11:00', status: 'waiting', date: '2025-12-26' },
  { id: '3', queueNumber: 'A048', citizenName: 'Phạm Thị D', procedure: 'Đăng ký KD', time: '11:15', status: 'waiting', date: '2025-12-26' },
  { id: '4', queueNumber: 'A049', citizenName: 'Hoàng Văn E', procedure: 'Công chứng', time: '11:30', status: 'waiting', date: '2025-12-26' },
  { id: '5', queueNumber: 'A050', citizenName: 'Vũ Thị F', procedure: 'CCCD', time: '11:45', status: 'waiting', date: '2025-12-26' },
  { id: '6', queueNumber: 'A051', citizenName: 'Ngô Văn G', procedure: 'Đất đai', time: '12:00', status: 'waiting', date: '2025-12-26' },
];

export const PROFILE_DATA = {
  name: 'Nguyễn Văn A',
  dob: '1990-01-01',
  id: '001090000001',
  phone: '0912345678',
  email: 'nguyenvana@email.com',
  address: 'Phường Gia Cẩm, TP Việt Trì, Phú Thọ',
  avatar: 'https://images.unsplash.com/photo-1633332755192-727a05c4013d?auto=format&fit=crop&q=80&w=200'
};
