import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useSimulation } from '../../context/SimulationContext';
import { MapPin, ChevronLeft, Bell, Navigation, Loader2, FileText, AlertCircle } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { motion } from 'framer-motion';
import * as api from '../../services/citizenApi';
import Swal from 'sweetalert2';

export const QueueTracking = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { zaloId, refreshAppointments } = useSimulation();

  const [appointment, setAppointment] = useState<api.AppointmentDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [cancelling, setCancelling] = useState(false);

  useEffect(() => {
    const fetchAppointment = async () => {
      if (!id || !zaloId) return;

      try {
        setLoading(true);
        const data = await api.getAppointmentDetail(parseInt(id), zaloId);
        setAppointment(data);
      } catch (err: any) {
        setError(err.message || 'Không thể tải thông tin lịch hẹn');
      } finally {
        setLoading(false);
      }
    };

    fetchAppointment();
    // Refresh every 30 seconds
    const interval = setInterval(fetchAppointment, 30000);
    return () => clearInterval(interval);
  }, [id, zaloId]);

  if (loading) {
    return (
      <div className="min-h-full bg-gray-50 flex items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-blue-600" />
      </div>
    );
  }

  if (error || !appointment) {
    return (
      <div className="min-h-full bg-gray-50 flex flex-col items-center justify-center p-4">
        <p className="text-red-500 mb-4">{error || 'Không tìm thấy lịch hẹn'}</p>
        <Button onClick={() => navigate('/citizen/appointments')}>Quay lại</Button>
      </div>
    );
  }

  const isServing = appointment.status === 'PROCESSING';
  const isCompleted = appointment.status === 'COMPLETED';
  const isCancelled = appointment.status === 'CANCELLED';
  const peopleAhead = appointment.peopleAhead;

  // Chỉ được hủy khi trạng thái là PENDING hoặc IN_QUEUE (chưa được gọi)
  const canCancel = appointment.status === 'PENDING' || appointment.status === 'IN_QUEUE';

  const handleCancel = async () => {
    const result = await Swal.fire({
      title: 'Xác nhận hủy lịch hẹn',
      text: `Bạn có chắc muốn hủy lịch hẹn ${appointment.queueDisplay}? Hành động này không thể hoàn tác.`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#ef4444',
      cancelButtonColor: '#6b7280',
      confirmButtonText: 'Hủy lịch hẹn',
      cancelButtonText: 'Giữ lại',
    });

    if (!result.isConfirmed) return;

    try {
      setCancelling(true);
      await api.cancelAppointment(parseInt(id!), zaloId);
      await refreshAppointments();
      Swal.fire({
        icon: 'success',
        title: 'Đã hủy lịch hẹn',
        text: 'Lịch hẹn của bạn đã được hủy thành công.',
        timer: 2000,
        showConfirmButton: false,
      });
      navigate('/citizen/appointments');
    } catch (err: any) {
      Swal.fire({
        icon: 'error',
        title: 'Không thể hủy',
        text: err.message || 'Có lỗi xảy ra, vui lòng thử lại.',
      });
    } finally {
      setCancelling(false);
    }
  };

  return (
    <div className="min-h-full bg-gray-50 flex flex-col">
      {/* Sticky Live Banner */}
      <div className="bg-blue-600 text-white p-4 sticky top-0 z-40 shadow-md">
        <div className="flex justify-between items-start mb-2">
          <div className="flex items-center gap-2">
            <span className="relative flex h-3 w-3">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-red-400 opacity-75"></span>
              <span className="relative inline-flex rounded-full h-3 w-3 bg-red-500"></span>
            </span>
            <span className="font-bold text-sm uppercase tracking-wider">Trạng thái • TRỰC TIẾP</span>
          </div>
          <button onClick={() => navigate('/citizen/appointments')} className="text-white/80 hover:text-white">
            <ChevronLeft className="h-6 w-6" />
          </button>
        </div>

        <div className="flex items-end justify-between mt-4">
          <div>
            <p className="text-blue-200 text-xs mb-1">Đang phục vụ</p>
            <h1 className="text-4xl font-bold">{appointment.currentServing || '--'}</h1>
          </div>
          <div className="bg-yellow-400 text-blue-900 px-4 py-2 rounded-lg shadow-lg transform translate-y-8">
            <p className="text-xs font-bold opacity-80 uppercase">Số của bạn</p>
            <h2 className="text-3xl font-bold">{appointment.queueDisplay}</h2>
          </div>
        </div>
      </div>

      <div className="flex-1 p-4 pt-10 space-y-6">
        {/* Status Card */}
        <div className="bg-white rounded-2xl shadow-sm p-6 text-center space-y-4">
          {isCompleted ? (
            <div>
              <div className="h-16 w-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <Bell className="h-8 w-8 text-green-600" />
              </div>
              <h3 className="text-xl font-bold text-gray-900">Đã hoàn thành</h3>
              <p className="text-gray-500">Phiên làm việc đã kết thúc. Xin cảm ơn!</p>
            </div>
          ) : isServing ? (
            <motion.div
              initial={{ scale: 0.9 }} animate={{ scale: 1 }}
              transition={{ repeat: Infinity, duration: 1, repeatType: "reverse" }}
            >
              <div className="h-16 w-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <Bell className="h-8 w-8 text-blue-600" />
              </div>
              <h3 className="text-xl font-bold text-blue-600">Đến lượt bạn!</h3>
              <p className="text-gray-500">Vui lòng đến {appointment.counter || 'Quầy tiếp nhận'} ngay lập tức.</p>
            </motion.div>
          ) : (
            <>
              <div className="grid grid-cols-2 gap-4 divide-x divide-gray-100">
                <div>
                  <p className="text-3xl font-bold text-gray-900">{peopleAhead}</p>
                  <p className="text-xs text-gray-400 uppercase mt-1">Người chờ trước</p>
                </div>
                <div>
                  <p className="text-3xl font-bold text-gray-900">~{appointment.estimatedWaitMinutes}</p>
                  <p className="text-xs text-gray-400 uppercase mt-1">Phút chờ</p>
                </div>
              </div>

              {/* Visual Queue */}
              <div className="py-4">
                <div className="flex items-center justify-center gap-2">
                  {[...Array(Math.min(5, peopleAhead))].map((_, i) => (
                    <div key={i} className="h-2 w-2 rounded-full bg-gray-300" />
                  ))}
                  <div className="h-4 w-4 rounded-full bg-yellow-400 ring-4 ring-yellow-100" />
                  <div className="h-2 w-2 rounded-full bg-gray-200" />
                </div>
              </div>
            </>
          )}
        </div>

        {/* Appointment Info */}
        <div className="bg-white rounded-xl p-4 shadow-sm space-y-3">
          <div className="flex items-start gap-3">
            <FileText className="h-5 w-5 text-gray-400 mt-0.5 flex-shrink-0" />
            <div>
              <p className="font-bold text-gray-900">{appointment.procedureName}</p>
              <p className="text-xs text-gray-400">Mã: {appointment.code}</p>
              {appointment.appointmentDate && (
                <p className="text-xs text-gray-500 mt-0.5">
                  Ngày hẹn: {appointment.appointmentDate}{appointment.appointmentTime && ` - ${appointment.appointmentTime}`}
                </p>
              )}
            </div>
          </div>

          {/* Mô tả thủ tục */}
          {appointment.description && (
            <div className="flex items-start gap-3 pt-3 border-t border-gray-100">
              <AlertCircle className="h-5 w-5 text-blue-500 flex-shrink-0 mt-0.5" />
              <div>
                <p className="text-xs text-blue-600 font-bold uppercase tracking-wide mb-0.5">Mô tả thủ tục</p>
                <p className="text-sm text-gray-700 leading-relaxed">{appointment.description}</p>
              </div>
            </div>
          )}

          {/* Giấy tờ cần mang theo */}
          {(() => {
            const raw = appointment.requiredDocuments;
            const docs: string[] = Array.isArray(raw)
              ? raw
              : typeof raw === 'string' && (raw as string).trim()
                ? (raw as string).split('\n').map((s: string) => s.trim()).filter(Boolean)
                : [];
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

        {/* Location Info */}
        <div className="bg-white rounded-xl p-4 shadow-sm flex items-center gap-4">
          <div className="h-12 w-12 bg-gray-100 rounded-lg flex items-center justify-center">
            <MapPin className="h-6 w-6 text-gray-500" />
          </div>
          <div className="flex-1">
            <h4 className="font-bold text-gray-900">{appointment.counter || 'Quầy tiếp nhận'} - Tầng 1</h4>
            <p className="text-xs text-gray-500">Trung tâm Hành chính công Quận 5</p>
          </div>
          <Button size="icon" variant="outline">
            <Navigation className="h-5 w-5" />
          </Button>
        </div>

        {/* Actions */}
        {canCancel && (
          <div className="space-y-3">
            <Button
              fullWidth
              variant="outline"
              className="text-red-500 border-red-200 hover:bg-red-50 disabled:opacity-50 disabled:cursor-not-allowed"
              onClick={handleCancel}
              disabled={cancelling}
            >
              {cancelling ? (
                <span className="flex items-center gap-2">
                  <Loader2 className="h-4 w-4 animate-spin" /> Đang hủy...
                </span>
              ) : 'Hủy lịch hẹn'}
            </Button>
          </div>
        )}
        {isCancelled && (
          <div className="bg-red-50 border border-red-100 rounded-xl p-4 text-center">
            <p className="text-red-600 font-medium">Lịch hẹn đã bị hủy</p>
          </div>
        )}
      </div>
    </div>
  );
};

