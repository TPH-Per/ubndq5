import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSimulation } from '../../context/SimulationContext';
import * as api from '../../services/citizenApi';
import Swal from 'sweetalert2';
import {
  Search, Clock, FileText, ChevronRight, Calendar as CalendarIcon,
  MapPin, Camera, Scan, CheckCircle2, Loader2,
  Briefcase, GraduationCap, Heart, Scale, HardHat, FileCheck, Landmark, AlertCircle
} from 'lucide-react';
import { cn } from '../../lib/utils';
import { Button } from '../../components/ui/Button';
import { format, addDays, isSameDay } from 'date-fns';
import { vi } from 'date-fns/locale';
import { motion, AnimatePresence } from 'framer-motion';

// Mappings for icons/colors based on specialty name (since API only returns ID and Name)
const CATEGORY_STYLES: Record<string, any> = {
  'Đất đai': { icon: MapPin, color: 'text-green-600', bg: 'bg-green-50' },
  'Kinh doanh': { icon: Briefcase, color: 'text-blue-600', bg: 'bg-blue-50' },
  'Hộ tịch': { icon: Heart, color: 'text-red-600', bg: 'bg-red-50' },
  'Xây dựng': { icon: HardHat, color: 'text-yellow-600', bg: 'bg-yellow-50' },
  'Tư pháp': { icon: Scale, color: 'text-indigo-600', bg: 'bg-indigo-50' },
  'Giáo dục': { icon: GraduationCap, color: 'text-orange-600', bg: 'bg-orange-50' },
  'Sao y': { icon: FileCheck, color: 'text-purple-600', bg: 'bg-purple-50' },
  'default': { icon: Landmark, color: 'text-gray-600', bg: 'bg-gray-50' }
};

const getCategoryStyle = (name: string) => {
  const key = Object.keys(CATEGORY_STYLES).find(k => name.includes(k)) || 'default';
  return CATEGORY_STYLES[key];
};

const DobInput = ({ value, onChange }: { value: string, onChange: (val: string) => void }) => {
  const parts = value ? value.split('-') : ['', '', ''];
  const pY = parts[0] || '';
  const pM = parts[1] || '';
  const pD = parts[2] || '';

  const days = Array.from({ length: 31 }, (_, i) => String(i + 1).padStart(2, '0'));
  const months = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0'));
  const currentYear = new Date().getFullYear();
  const years = Array.from({ length: 100 }, (_, i) => String(currentYear - i));

  const update = (newD: string, newM: string, newY: string) => {
    if (newD && newM && newY) {
      onChange(`${newY}-${newM}-${newD}`);
    } else {
      // Incomplete date format, keep parsing or store partial
      onChange(`${newY || 'yyyy'}-${newM || 'mm'}-${newD || 'dd'}`);
    }
  }

  return (
    <div className="flex gap-2 w-full">
      <select
        value={pD.length === 2 && pD !== 'dd' ? pD : ''}
        onChange={e => update(e.target.value, pM, pY)}
        className="flex-1 p-3 bg-white border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none"
      >
        <option value="" disabled>Ngày</option>
        {days.map(day => <option key={day} value={day}>{day}</option>)}
      </select>
      <select
        value={pM.length === 2 && pM !== 'mm' ? pM : ''}
        onChange={e => update(pD, e.target.value, pY)}
        className="flex-1 p-3 bg-white border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none"
      >
        <option value="" disabled>Tháng</option>
        {months.map(month => <option key={month} value={month}>{month}</option>)}
      </select>
      <select
        value={pY.length === 4 && pY !== 'yyyy' ? pY : ''}
        onChange={e => update(pD, pM, e.target.value)}
        className="flex-1 p-3 bg-white border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none"
      >
        <option value="" disabled>Năm</option>
        {years.map(year => <option key={year} value={year}>{year}</option>)}
      </select>
    </div>
  );
};

export const BookingFlow = () => {
  const navigate = useNavigate();
  const { bookAppointment, myAppointments, citizenName, setCitizenName, citizenId, setCitizenId } = useSimulation();
  const [step, setStep] = useState(1);

  // Data state
  const [categories, setCategories] = useState<api.Specialty[]>([]);
  const [procedures, setProcedures] = useState<api.Procedure[]>([]);
  const [availableSlots, setAvailableSlots] = useState<api.TimeSlot[]>([]);

  // Selection state
  const [selectedCategory, setSelectedCategory] = useState<api.Specialty | null>(null);
  const [selectedService, setSelectedService] = useState<api.Procedure | null>(null);
  const [selectedDate, setSelectedDate] = useState<Date | null>(null);
  const [selectedTime, setSelectedTime] = useState<string | null>(null);

  const [formData, setFormData] = useState({ fullName: citizenName, phone: '0901234567', cccd: citizenId, birthDate: '', notes: '' });
  const [searchTerm, setSearchTerm] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isBooking, setIsBooking] = useState(false);
  const [latestBooking, setLatestBooking] = useState<api.AppointmentResponse | null>(null);

  // Fetch Specialties on mount
  useEffect(() => {
    const fetchSpecialties = async () => {
      setIsLoading(true);
      try {
        const data = await api.getSpecialties();
        setCategories(data);
      } catch (error) {
        console.error("Error fetching specialties:", error);
      } finally {
        setIsLoading(false);
      }
    };
    fetchSpecialties();
  }, []);

  // Fetch Procedures when category changes
  useEffect(() => {
    if (selectedCategory) {
      const fetchProcedures = async () => {
        setIsLoading(true);
        try {
          const data = await api.getProcedures(selectedCategory.id);
          setProcedures(data);
        } catch (error) {
          console.error("Error fetching procedures:", error);
        } finally {
          setIsLoading(false);
        }
      };

      // If we selected a category that has "procedures" (not just a specialty), in real app we filter by specialty API
      fetchProcedures();
    }
  }, [selectedCategory]);

  // Fetch slots when date changes
  useEffect(() => {
    if (selectedDate) {
      const fetchSlots = async () => {
        setIsLoading(true);
        try {
          const dateStr = format(selectedDate, 'yyyy-MM-dd');
          const data = await api.getAvailableSlots(dateStr);
          // Filter slots that have availability > 0
          setAvailableSlots(data.slots.filter(s => s.available > 0));
        } catch (error) {
          console.error("Error fetching slots:", error);
        } finally {
          setIsLoading(false);
        }
      };
      fetchSlots();
      setSelectedTime(null); // Reset time when date changes
    }
  }, [selectedDate]);

  const handleBooking = async () => {
    if (!selectedService || !selectedDate || !selectedTime) return;

    if (!formData.birthDate) {
      Swal.fire({
        icon: 'warning',
        title: 'Thiếu thông tin',
        text: 'Vui lòng nhập ngày sinh của bạn.',
        confirmButtonColor: '#3b82f6'
      });
      return;
    }

    const birthDate = new Date(formData.birthDate);
    const today = new Date();
    let age = today.getFullYear() - birthDate.getFullYear();
    const m = today.getMonth() - birthDate.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) {
      age--;
    }

    if (age < 15) {
      Swal.fire({
        icon: 'error',
        title: 'Chưa đủ tuổi',
        text: 'Công dân phải đủ từ 15 tuổi trở lên mới được phép đặt lịch hẹn trực tuyến.',
        confirmButtonColor: '#3b82f6'
      });
      return;
    }

    setIsBooking(true);
    // Update context user info if changed
    if (formData.fullName !== citizenName) setCitizenName(formData.fullName);

    const result = await bookAppointment({
      procedureId: selectedService.id,
      procedureName: selectedService.name,
      date: selectedDate,
      time: selectedTime,
      phone: formData.phone,
      notes: formData.notes || undefined
    });

    setIsBooking(false);

    if (result) {
      setLatestBooking(result);
      setStep(5);
    } else {
      Swal.fire({
        icon: 'error',
        title: 'Lỗi',
        text: 'Có lỗi xảy ra khi đặt lịch. Vui lòng thử lại.',
        confirmButtonColor: '#3b82f6'
      });
    }
  };

  // Get the latest booked appointment for the success screen
  const latestAppointment = myAppointments[0];

  // --- Step 1: Select Category ---
  const Step1 = () => {
    const filteredCategories = categories.filter(cat =>
      cat.name.toLowerCase().includes(searchTerm.toLowerCase())
    );

    if (isLoading && categories.length === 0) {
      return (
        <div className="flex justify-center p-8">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
        </div>
      );
    }

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
            filteredCategories.map((cat, idx) => {
              const style = getCategoryStyle(cat.name);
              const Icon = style.icon;

              return (
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
                  <div className={cn("h-12 w-12 rounded-2xl flex items-center justify-center shrink-0 shadow-inner", style.bg, style.color)}>
                    <Icon className="h-6 w-6" />
                  </div>
                  <div className="flex-1 flex items-center justify-center">
                    <h3 className="font-bold text-gray-800 text-xs uppercase leading-snug">{cat.name}</h3>
                  </div>
                </motion.div>
              );
            })
          ) : (
            <div className="col-span-2 text-center py-8 text-gray-500">
              Không tìm thấy lĩnh vực phù hợp
            </div>
          )}
        </div>
      </motion.div>
    );
  };

  // --- Step 2: Select Service ---
  const Step2 = () => {
    // Services are now fetched into 'procedures' state
    const style = selectedCategory ? getCategoryStyle(selectedCategory.name) : CATEGORY_STYLES['default'];
    const Icon = style.icon;

    if (isLoading && procedures.length === 0) {
      return (
        <div className="flex justify-center p-8">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
        </div>
      );
    }

    return (
      <motion.div
        initial={{ opacity: 0, x: 20 }}
        animate={{ opacity: 1, x: 0 }}
        exit={{ opacity: 0, x: -20 }}
        className="p-4 space-y-4"
      >
        <div className="bg-blue-50 p-4 rounded-xl flex items-start gap-4">
          <div className="p-2 bg-white rounded-lg shadow-sm shrink-0">
            <Icon className="h-6 w-6 text-primary" />
          </div>
          <div>
            <p className="text-xs text-blue-600 font-bold uppercase mb-1">Đang chọn dịch vụ thuộc</p>
            <h3 className="font-bold text-gray-900 leading-tight">{selectedCategory?.name}</h3>
          </div>
        </div>

        <div className="space-y-2">
          {procedures.length > 0 ? (
            procedures.map((service, idx) => (
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
            ))
          ) : (
            <div className="text-center py-8 text-gray-500">
              Chưa có thủ tục nào trong lĩnh vực này
            </div>
          )}
        </div>
      </motion.div>
    );
  };

  // --- Step 3: Select Date & Time ---
  const Step3 = () => {
    const today = new Date();
    // Generate 21 days and keep only weekdays (Mon–Fri), up to 14 working days
    const isWeekend = (d: Date) => d.getDay() === 0 || d.getDay() === 6;
    const dates = Array.from({ length: 28 }, (_, i) => addDays(today, i))
      .filter(d => !isWeekend(d))
      .slice(0, 14);

    return (
      <motion.div
        initial={{ opacity: 0, x: 20 }}
        animate={{ opacity: 1, x: 0 }}
        exit={{ opacity: 0, x: -20 }}
        className="p-4 space-y-6"
      >
        <div className="bg-white rounded-2xl shadow-sm p-4 border border-gray-100">
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-bold text-lg text-gray-800">Chọn ngày hẹn</h3>
            <div className="flex gap-2">
              <span className="text-xs font-medium bg-blue-50 text-blue-600 px-2 py-1 rounded-lg">
                {format(today, "'Tháng' MM, yyyy", { locale: vi })}
              </span>
            </div>
          </div>

          <div className="grid grid-cols-5 gap-1 text-center mb-2">
            {['T2', 'T3', 'T4', 'T5', 'T6'].map((d, i) => (
              <span key={i} className="text-[10px] font-bold text-gray-400 uppercase">{d}</span>
            ))}
          </div>

          <div className="grid grid-cols-5 gap-y-3 gap-x-1">
            {/* Offset: align first date to correct weekday column (Mon=0 ... Fri=4) */}
            {Array.from({ length: ((dates[0]?.getDay() ?? 1) + 6) % 7 }).map((_, i) => <div key={`empty-${i}`} />)}

            {dates.map((date, i) => {
              const isSelected = selectedDate && isSameDay(date, selectedDate);
              const isToday = isSameDay(date, today);

              return (
                <button
                  key={date.toISOString()}
                  onClick={() => setSelectedDate(date)}
                  className={cn(
                    "aspect-square rounded-xl flex flex-col items-center justify-center relative transition-all duration-200 border-2",
                    isSelected
                      ? "bg-primary border-primary text-white shadow-lg shadow-blue-200 scale-105 z-10"
                      : "bg-white border-transparent hover:bg-gray-50 text-gray-700 hover:border-gray-100"
                  )}
                >
                  <span className={cn("text-xs font-bold", isSelected ? "text-white" : "text-gray-700")}>
                    {format(date, 'd')}
                  </span>
                  <span className={cn("text-[9px]", isSelected ? "text-white/80" : "text-gray-400")}>
                    {format(date, 'EEE', { locale: vi })}
                  </span>
                  {isToday && !isSelected && (
                    <span className="absolute bottom-1 w-1 h-1 bg-primary rounded-full" />
                  )}
                </button>
              );
            })}
          </div>
        </div>

        {/* Time Slots Selection */}
        <AnimatePresence>
          {selectedDate && (
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              className="space-y-4"
            >
              <h3 className="font-bold text-gray-900 flex items-center gap-2">
                <Clock className="h-5 w-5 text-primary" />
                Giờ ước tính ({availableSlots.length} khung giờ)
              </h3>

              {isLoading ? (
                <div className="flex justify-center p-4">
                  <Loader2 className="h-6 w-6 animate-spin text-gray-400" />
                </div>
              ) : availableSlots.length > 0 ? (
                <div className="grid grid-cols-3 gap-3">
                  {availableSlots.map((slot) => {
                    const isToday = selectedDate ? isSameDay(selectedDate, new Date()) : false;
                    const [hours, minutes] = slot.time.split(':').map(Number);
                    const slotTimeCheck = new Date();
                    slotTimeCheck.setHours(hours, minutes, 0, 0);
                    const isPast = isToday && slotTimeCheck < new Date();

                    return (
                      <button
                        key={slot.time}
                        onClick={() => !isPast && setSelectedTime(slot.time)}
                        disabled={isPast}
                        className={cn(
                          "py-3 px-2 rounded-xl border text-sm font-semibold transition-all shadow-sm",
                          isPast
                            ? "bg-gray-100 border-gray-200 text-gray-400 cursor-not-allowed opacity-70"
                            : selectedTime === slot.time
                              ? "bg-primary border-primary text-white ring-2 ring-primary/20"
                              : "bg-white border-gray-100 text-gray-700 hover:border-primary/50 hover:bg-blue-50"
                        )}
                      >
                        {slot.time}
                        <span className={cn(
                          "block text-[10px] font-normal mt-0.5",
                          isPast ? "text-gray-400" : selectedTime === slot.time ? "text-blue-100" : "text-gray-400"
                        )}>
                          {isPast ? 'Đã qua' : 'Còn trống'}
                        </span>
                      </button>
                    );
                  })}
                </div>
              ) : (
                <div className="text-center p-4 bg-gray-50 rounded-xl text-gray-500 text-sm">
                  Không còn lịch trống cho ngày này
                </div>
              )}

              <Button
                fullWidth
                size="lg"
                onClick={() => setStep(4)}
                disabled={!selectedTime}
                className="shadow-lg shadow-blue-200 mt-4"
              >
                Xác nhận thời gian
              </Button>
            </motion.div>
          )}
        </AnimatePresence>
      </motion.div>
    );
  };

  // --- Step 4: Form ---
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
        <div className="flex flex-col gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Ngày sinh</label>
            <DobInput
              value={formData.birthDate}
              onChange={(val) => setFormData({ ...formData, birthDate: val })}
            />
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
          <input
            type="text"
            value={formData.cccd}
            onChange={(e) => {
              const val = e.target.value.replace(/\D/g, '').slice(0, 12);
              setFormData({ ...formData, cccd: val });
              setCitizenId(val);
            }}
            className="w-full p-3 bg-white border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary font-mono tracking-wider"
            placeholder="Nhập số CCCD (12 số)"
            maxLength={12}
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Ghi chú / Mô tả hồ sơ <span className="text-gray-400 font-normal">(không bắt buộc)</span></label>
          <textarea
            value={formData.notes}
            onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
            rows={2}
            placeholder="Nhập ghi chú hoặc mô tả thêm về nhu cầu của bạn..."
            className="w-full p-3 bg-white border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary resize-none"
          />
        </div>
      </form>

      <div className="bg-yellow-50 p-4 rounded-xl">
        <h4 className="font-bold text-sm text-yellow-800 mb-2">Giấy tờ bắt buộc</h4>
        <ul className="space-y-2">
          {(() => {
            const docs: string[] = selectedService?.requiredDocuments ?? [];
            return docs.length > 0
              ? docs.map((doc: string, i: number) => (
                  <li key={i} className="flex items-start gap-2 text-xs text-yellow-700">
                    <div className="mt-0.5 h-3 w-3 rounded border border-yellow-600 flex-shrink-0" />
                    <span>{doc}</span>
                  </li>
                ))
              : (
                  <li className="flex items-start gap-2 text-xs text-yellow-700">
                    <div className="mt-0.5 h-3 w-3 rounded border border-yellow-600 flex-shrink-0" />
                    <span>CCCD gắn chip (Bản chính + Photo)</span>
                  </li>
                );
          })()}
        </ul>
      </div>

      <div className="pt-4">
        <Button fullWidth size="lg" onClick={handleBooking} disabled={isBooking}>
          {isBooking ? (
            <span className="flex items-center gap-2">
              <Loader2 className="h-4 w-4 animate-spin" /> Đang xử lý...
            </span>
          ) : "Xác nhận đặt lịch"}
        </Button>
      </div>
    </div>
  );

  // --- Step 5: Success ---
  const Step5 = () => {
    // Falls back to myAppointments[0] just in case latestBooking is null, 
    // but ideally latestBooking is set
    const booking: any = latestBooking ||
      (myAppointments.length > 0 ? {
        queueDisplay: myAppointments[0].queueNumber,
        date: myAppointments[0].date,
        time: myAppointments[0].time,
        counter: myAppointments[0].counter,
        procedureName: myAppointments[0].procedure,
        id: myAppointments[0].id
      } : { queueDisplay: '---' });

    return (
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
            <h3 className="text-4xl font-bold mt-1">{booking.queueDisplay}</h3>
          </div>
          <div className="p-6 space-y-4 text-left">
            <div className="flex items-center gap-3">
              <CalendarIcon className="h-5 w-5 text-gray-400" />
              <div>
                <p className="text-xs text-gray-500">Thời gian</p>
                <p className="font-medium text-sm">
                  {booking.appointmentDate || booking.date} • {booking.appointmentTime || booking.time}
                </p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <FileText className="h-5 w-5 text-gray-400" />
              <div>
                <p className="text-xs text-gray-500">Dịch vụ</p>
                <p className="font-medium text-sm">{booking.procedureName || booking.procedure}</p>
              </div>
            </div>

            {/* Mô tả thủ tục */}
            {selectedService?.description && (
              <div className="flex items-start gap-3 pt-3 border-t border-gray-100">
                <AlertCircle className="h-5 w-5 text-blue-500 flex-shrink-0 mt-0.5" />
                <div>
                  <p className="text-xs text-blue-600 font-bold uppercase tracking-wide mb-0.5">Mô tả thủ tục</p>
                  <p className="text-sm text-gray-700 leading-relaxed">{selectedService.description}</p>
                </div>
              </div>
            )}

            {/* Giấy tờ cần mang theo */}
            {(() => {
              const docs: string[] = selectedService?.requiredDocuments ?? [];
              return docs.length > 0 ? (
                <div className="pt-3 border-t border-yellow-100">
                  <div className="bg-yellow-50 rounded-xl p-4">
                    <p className="text-xs text-yellow-700 font-bold uppercase tracking-wide mb-2 flex items-center gap-1.5">
                      <span className="inline-block w-2 h-2 rounded-full bg-yellow-500" />
                      Giấy tờ cần mang theo
                    </p>
                    <ul className="space-y-1.5">
                      {docs.map((doc: string, idx: number) => (
                        <li key={idx} className="flex items-start gap-2 text-xs text-yellow-800">
                          <span className="mt-0.5 h-3.5 w-3.5 rounded border border-yellow-500 flex-shrink-0 flex items-center justify-center text-[8px] font-bold text-yellow-600">{idx + 1}</span>
                          <span>{doc}</span>
                        </li>
                      ))}
                    </ul>
                  </div>
                </div>
              ) : null;
            })()}
          </div>
        </div>

        <div className="flex gap-3 w-full">
          <Button
            onClick={() => navigate(`/citizen/queue/${booking.id}`)} // Use numeric application id for tracking
            className="flex-1 bg-yellow-500 hover:bg-yellow-600 text-white"
          >
            Theo dõi trực tiếp
          </Button>
          <Button variant="outline" onClick={() => navigate('/citizen')} className="flex-1">
            Về trang chủ
          </Button>
        </div>
      </div>
    )
  };

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
