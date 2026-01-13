import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSimulation } from '../../context/SimulationContext';
import {
  Search, Clock, FileText, ChevronRight, Calendar as CalendarIcon,
  MapPin, Camera, Scan, CheckCircle2,
  Briefcase, GraduationCap, Heart, Scale, HardHat, FileCheck, Landmark
} from 'lucide-react';
import { cn } from '../../lib/utils';
import { QUEUE_DATA } from '../../data/mock';
import { Button } from '../../components/ui/Button';
import { format, addDays, isSameDay } from 'date-fns';
import { motion, AnimatePresence } from 'framer-motion';

// New Categories List with Assigned Counters
const BOOKING_CATEGORIES = [
  {
    id: '1',
    name: 'CÔNG THƯƠNG – TÀI CHÍNH',
    icon: Briefcase,
    color: 'text-blue-600',
    bg: 'bg-blue-50',
    counterId: 'A',
    services: [
      { id: '1-1', name: 'Đăng ký thành lập hộ kinh doanh' },
      { id: '1-2', name: 'Thay đổi nội dung đăng ký hộ kinh doanh' },
      { id: '1-3', name: 'Tạm ngừng kinh doanh của hộ kinh doanh' },
      { id: '1-4', name: 'Chấm dứt hoạt động hộ kinh doanh' }
    ]
  },
  {
    id: '2',
    name: 'ĐẤT ĐAI – MÔI TRƯỜNG',
    icon: MapPin,
    color: 'text-green-600',
    bg: 'bg-green-50',
    counterId: 'B',
    services: [
      { id: '2-1', name: 'Cấp Giấy chứng nhận QSDĐ lần đầu' },
      { id: '2-2', name: 'Đăng ký biến động đất đai' },
      { id: '2-3', name: 'Chuyển mục đích sử dụng đất' },
      { id: '2-4', name: 'Tách thửa hoặc hợp thửa đất' }
    ]
  },
  {
    id: '3',
    name: 'GIÁO DỤC & ĐÀO TẠO – VĂN HÓA & THỂ THAO',
    icon: GraduationCap,
    color: 'text-orange-600',
    bg: 'bg-orange-50',
    counterId: 'C',
    services: [
      { id: '3-1', name: 'Đăng ký nhập học mầm non' },
      { id: '3-2', name: 'Đăng ký nhập học tiểu học' },
      { id: '3-3', name: 'Xác nhận hoàn thành chương trình tiểu học' }
    ]
  },
  {
    id: '4',
    name: 'NỘI VỤ - DÂN TỘC & TÔN GIÁO – Y TẾ',
    icon: Heart,
    color: 'text-red-600',
    bg: 'bg-red-50',
    counterId: 'A',
    services: [
      { id: '4-1', name: 'Bảo hiểm y tế hộ gia đình' },
      { id: '4-2', name: 'Xác nhận tình trạng hôn nhân' },
      { id: '4-3', name: 'Mai táng phí' }
    ]
  },
  {
    id: '5',
    name: 'SAO Y – CHỨNG THỰC',
    icon: FileCheck,
    color: 'text-purple-600',
    bg: 'bg-purple-50',
    counterId: 'B',
    services: [
      { id: '5-1', name: 'Chứng thực bản sao từ bản chính' },
      { id: '5-2', name: 'Chứng thực chữ ký' },
      { id: '5-3', name: 'Chứng thực hợp đồng giao dịch' }
    ]
  },
  {
    id: '6',
    name: 'TƯ PHÁP – HỘ TỊCH',
    icon: Scale,
    color: 'text-indigo-600',
    bg: 'bg-indigo-50',
    counterId: 'C',
    services: [
      { id: '6-1', name: 'Đăng ký khai sinh' },
      { id: '6-2', name: 'Đăng ký kết hôn' },
      { id: '6-3', name: 'Đăng ký khai tử' },
      { id: '6-4', name: 'Cấp bản sao trích lục hộ tịch' }
    ]
  },
  {
    id: '7',
    name: 'XÂY DỰNG',
    icon: HardHat,
    color: 'text-yellow-600',
    bg: 'bg-yellow-50',
    counterId: 'A',
    services: [
      { id: '7-1', name: 'Cấp giấy phép xây dựng nhà ở riêng lẻ' },
      { id: '7-2', name: 'Gia hạn giấy phép xây dựng' },
      { id: '7-3', name: 'Điều chỉnh giấy phép xây dựng' }
    ]
  },
];

export const BookingFlow = () => {
  const navigate = useNavigate();
  const { bookAppointment, myAppointments } = useSimulation();
  const [step, setStep] = useState(1);
  const [selectedCategory, setSelectedCategory] = useState<any>(null);
  const [selectedService, setSelectedService] = useState<any>(null);
  const [selectedDate, setSelectedDate] = useState<Date | null>(null);
  const [formData, setFormData] = useState({ fullName: 'Nguyen Van A', phone: '0912345678' });
  const [searchTerm, setSearchTerm] = useState('');

  const handleBooking = () => {
    bookAppointment({
      procedureName: selectedService?.name || selectedCategory?.name,
      date: selectedDate,
      ...formData
    });
    setStep(5);
  };

  // Get the latest booked appointment for the success screen
  const latestAppointment = myAppointments[0];

  // --- Step 1: Select Category ---
  const Step1 = () => {
    const filteredCategories = BOOKING_CATEGORIES.filter(cat =>
      cat.name.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
      <motion.div
        initial={{ opacity: 0, x: 20 }}
        animate={{ opacity: 1, x: 0 }}
        exit={{ opacity: 0, x: -20 }}
        className="space-y-4 p-4"
      >
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
          <input
            type="text"
            placeholder="Tìm kiếm lĩnh vực..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-3 bg-white rounded-xl border-none shadow-sm focus:ring-2 focus:ring-primary/20"
          />
        </div>

        <div className="grid grid-cols-2 gap-3">
          {filteredCategories.length > 0 ? (
            filteredCategories.map((cat, idx) => (
              <motion.div
                key={cat.id}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: idx * 0.05 }}
                onClick={() => {
                  setSelectedCategory(cat);
                  setStep(2);
                }}
                className="bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex flex-col items-center gap-3 cursor-pointer active:scale-95 transition-all hover:shadow-md text-center h-full"
              >
                <div className={cn("h-12 w-12 rounded-2xl flex items-center justify-center shrink-0 shadow-inner", cat.bg, cat.color)}>
                  <cat.icon className="h-6 w-6" />
                </div>
                <div className="flex-1 flex items-center justify-center">
                  <h3 className="font-bold text-gray-800 text-xs uppercase leading-snug">{cat.name}</h3>
                </div>
              </motion.div>
            ))
          ) : (
            <div className="col-span-2 text-center py-8 text-gray-500">
              Không tìm thấy lĩnh vực phù hợp
            </div>
          )}
        </div>
      </motion.div>
    );
  };

  // --- Step 2: Select Service (New) ---
  const Step2 = () => {
    const services = selectedCategory?.services || [];

    return (
      <motion.div
        initial={{ opacity: 0, x: 20 }}
        animate={{ opacity: 1, x: 0 }}
        exit={{ opacity: 0, x: -20 }}
        className="p-4 space-y-4"
      >
        <div className="bg-blue-50 p-4 rounded-xl flex items-start gap-4">
          <div className="p-2 bg-white rounded-lg shadow-sm shrink-0">
            {selectedCategory?.icon && <selectedCategory.icon className="h-6 w-6 text-primary" />}
          </div>
          <div>
            <p className="text-xs text-blue-600 font-bold uppercase mb-1">Đang chọn dịch vụ thuộc</p>
            <h3 className="font-bold text-gray-900 leading-tight">{selectedCategory?.name}</h3>
          </div>
        </div>

        <div className="space-y-2">
          {services.map((service: any, idx: number) => (
            <motion.div
              key={service.id}
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: idx * 0.05 }}
              onClick={() => {
                setSelectedService(service);
                setStep(3);
              }}
              className="bg-white p-4 rounded-xl border border-gray-100 flex items-center justify-between cursor-pointer active:scale-[0.98] transition-all hover:border-primary hover:shadow-md group"
            >
              <span className="font-medium text-gray-700 text-sm group-hover:text-primary transition-colors">{service.name}</span>
              <ChevronRight className="h-5 w-5 text-gray-300 group-hover:text-primary transition-colors" />
            </motion.div>
          ))}
        </div>
      </motion.div>
    );
  };

  // --- Step 3: Select Date (Old Step 2) ---
  const Step3 = () => {
    const today = new Date();
    const dates = Array.from({ length: 14 }, (_, i) => addDays(today, i));

    return (
      <motion.div
        initial={{ opacity: 0, x: 20 }}
        animate={{ opacity: 1, x: 0 }}
        exit={{ opacity: 0, x: -20 }}
        className="p-4 space-y-6"
      >
        <div className="bg-white rounded-2xl shadow-sm p-4 border border-gray-100">
          <div className="flex items-center justify-between mb-6">
            <h3 className="font-bold text-lg text-gray-800">Tháng 12/2025</h3>
            <div className="flex gap-2">
              <button className="p-2 hover:bg-gray-100 rounded-lg text-gray-600 transition-colors"><ChevronRight className="h-5 w-5 rotate-180" /></button>
              <button className="p-2 hover:bg-gray-100 rounded-lg text-gray-600 transition-colors"><ChevronRight className="h-5 w-5" /></button>
            </div>
          </div>

          <div className="grid grid-cols-7 gap-1 text-center mb-4">
            {['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'].map((d, i) => (
              <span key={i} className="text-xs font-bold text-gray-400 uppercase">{d}</span>
            ))}
          </div>

          <div className="grid grid-cols-7 gap-y-4 gap-x-2">
            <div /><div />
            {dates.map((date, i) => {
              const isSelected = selectedDate && isSameDay(date, selectedDate);
              const availability = i % 3 === 0 ? 'high' : i % 2 === 0 ? 'med' : 'low';

              return (
                <button
                  key={date.toISOString()}
                  onClick={() => setSelectedDate(date)}
                  className={cn(
                    "h-10 w-10 rounded-xl flex flex-col items-center justify-center relative transition-all duration-300",
                    isSelected ? "bg-primary text-white shadow-lg shadow-blue-200 scale-105" : "hover:bg-gray-50 text-gray-700"
                  )}
                >
                  <span className={cn("text-sm font-bold", isSelected ? "text-white" : "text-gray-700")}>{format(date, 'd')}</span>
                  {!isSelected && (
                    <span className={cn(
                      "h-1.5 w-1.5 rounded-full mt-1",
                      availability === 'high' ? "bg-red-400" : availability === 'med' ? "bg-yellow-400" : "bg-green-400"
                    )} />
                  )}
                </button>
              );
            })}
          </div>
        </div>

        <AnimatePresence>
          {selectedDate && (
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: 20 }}
              className="bg-white rounded-xl p-5 shadow-lg border border-primary/10 relative overflow-hidden"
            >
              <div className="absolute top-0 right-0 w-20 h-20 bg-primary/5 rounded-full -mr-10 -mt-10" />

              <div className="flex justify-between items-center mb-6 relative z-10">
                <div>
                  <p className="text-xs font-medium text-gray-500 uppercase tracking-wide mb-1">Ngày hẹn</p>
                  <h3 className="text-2xl font-black text-primary">{format(selectedDate, 'dd/MM/yyyy')}</h3>
                </div>
                <div className="text-right">
                  <p className="text-xs font-medium text-gray-500 uppercase tracking-wide mb-1">Còn trống</p>
                  <div className="flex items-center justify-end gap-1">
                    <span className="h-2 w-2 rounded-full bg-green-500 animate-pulse" />
                    <h3 className="text-2xl font-black text-gray-900">12</h3>
                  </div>
                </div>
              </div>
              <Button fullWidth size="lg" onClick={() => setStep(4)} className="shadow-lg shadow-blue-200">
                Xác nhận ngày hẹn
              </Button>
            </motion.div>
          )}
        </AnimatePresence>
      </motion.div>
    );
  };

  // --- Step 4: Form (Old Step 5) ---
  const Step4 = () => (
    <div className="p-4 space-y-6">
      <div className="bg-white p-6 rounded-2xl border-2 border-dashed border-primary/30 flex flex-col items-center justify-center text-center gap-3">
        <div className="h-12 w-12 bg-blue-50 rounded-full flex items-center justify-center">
          <Scan className="h-6 w-6 text-primary" />
        </div>
        <div>
          <h3 className="font-bold text-gray-900">Quét mã QR CCCD</h3>
          <p className="text-xs text-gray-500 mt-1">Tự động điền thông tin cá nhân</p>
        </div>
        <Button variant="outline" size="sm" className="gap-2">
          <Camera className="h-4 w-4" /> Mở Camera
        </Button>
      </div>

      <div className="flex items-center gap-4">
        <div className="h-px bg-gray-200 flex-1" />
        <span className="text-xs text-gray-400 font-medium">HOẶC NHẬP TAY</span>
        <div className="h-px bg-gray-200 flex-1" />
      </div>

      <form className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Họ và tên</label>
          <input
            type="text"
            value={formData.fullName}
            onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
            className="w-full p-3 bg-white border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary"
          />
        </div>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Ngày sinh</label>
            <input type="date" className="w-full p-3 bg-white border border-gray-200 rounded-lg text-sm" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Số điện thoại</label>
            <input
              type="tel"
              value={formData.phone}
              onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
              className="w-full p-3 bg-white border border-gray-200 rounded-lg text-sm"
            />
          </div>
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Số CCCD/CMND</label>
          <input type="text" defaultValue="001098000123" className="w-full p-3 bg-white border border-gray-200 rounded-lg text-sm" />
        </div>
      </form>

      <div className="bg-yellow-50 p-4 rounded-xl">
        <h4 className="font-bold text-sm text-yellow-800 mb-2">Giấy tờ bắt buộc</h4>
        <ul className="space-y-2">
          {[1, 2].map(i => (
            <li key={i} className="flex items-start gap-2 text-xs text-yellow-700">
              <div className="mt-0.5 h-3 w-3 rounded border border-yellow-600 flex-shrink-0" />
              <span>Bản gốc CCCD và 01 bản photo công chứng</span>
            </li>
          ))}
        </ul>
      </div>

      <div className="pt-4">
        <Button fullWidth size="lg" onClick={handleBooking}>Xác nhận đặt lịch</Button>
      </div>
    </div>
  );

  // --- Step 5: Success (Old Step 6) ---
  const Step5 = () => (
    <div className="p-6 flex flex-col items-center justify-center min-h-[80vh] text-center">
      <motion.div
        initial={{ scale: 0 }}
        animate={{ scale: 1 }}
        className="h-20 w-20 bg-green-100 rounded-full flex items-center justify-center mb-6"
      >
        <CheckCircle2 className="h-10 w-10 text-green-600" />
      </motion.div>

      <h2 className="text-2xl font-bold text-gray-900 mb-2">Đặt lịch thành công!</h2>
      <p className="text-gray-500 text-sm mb-8">Lịch hẹn của bạn đã được ghi nhận.</p>

      <div className="bg-white w-full rounded-2xl shadow-card border border-gray-100 overflow-hidden mb-6">
        <div className="bg-primary p-4 text-white">
          <p className="text-xs opacity-80 uppercase tracking-wider">Số thứ tự</p>
          <h3 className="text-4xl font-bold mt-1">{latestAppointment?.queueNumber}</h3>
        </div>
        <div className="p-6 space-y-4 text-left">
          <div className="flex items-center gap-3">
            <CalendarIcon className="h-5 w-5 text-gray-400" />
            <div>
              <p className="text-xs text-gray-500">Thời gian</p>
              <p className="font-medium text-sm">{latestAppointment?.date} • {latestAppointment?.time}</p>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <MapPin className="h-5 w-5 text-gray-400" />
            <div>
              <p className="text-xs text-gray-500">Địa điểm</p>
              <p className="font-medium text-sm">{latestAppointment?.counter}</p>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <FileText className="h-5 w-5 text-gray-400" />
            <div>
              <p className="text-xs text-gray-500">Dịch vụ</p>
              <p className="font-medium text-sm">{latestAppointment?.procedure}</p>
            </div>
          </div>
        </div>
      </div>

      <div className="flex gap-3 w-full">
        <Button
          onClick={() => navigate(`/citizen/queue/${latestAppointment?.id}`)}
          className="flex-1 bg-yellow-500 hover:bg-yellow-600 text-white"
        >
          Theo dõi trực tiếp
        </Button>
        <Button variant="outline" onClick={() => navigate('/citizen')} className="flex-1">
          Về trang chủ
        </Button>
      </div>
    </div>
  );

  return (
    <div className="min-h-full bg-gray-50">
      {/* Progress Header */}
      {step <= 5 && (
        <div className="bg-white p-4 pb-2 sticky top-0 z-40">
          <div className="flex gap-2 mb-2">
            {[1, 2, 3, 4, 5].map(i => (
              <div key={i} className={cn("h-1 flex-1 rounded-full", i <= step ? "bg-primary" : "bg-gray-200")} />
            ))}
          </div>
          <h2 className="font-bold text-lg">
            {step === 1 && "Chọn lĩnh vực"}
            {step === 2 && "Chọn dịch vụ"}
            {step === 3 && "Chọn ngày hẹn"}
            {step === 4 && "Thông tin của bạn"}
          </h2>
        </div>
      )}

      <AnimatePresence mode="wait">
        {step === 1 && <Step1 key="step1" />}
        {step === 2 && <Step2 key="step2" />}
        {step === 3 && <Step3 key="step3" />}
        {step === 4 && <Step4 key="step4" />}
        {step === 5 && <Step5 key="step5" />}
      </AnimatePresence>
    </div>
  );
};
