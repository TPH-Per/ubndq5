import React, { useState, useMemo } from 'react';
import { useSimulation } from '../../context/SimulationContext';
import { Button } from '../../components/ui/Button';
import { Clock, User, FileText, CheckCircle, XCircle, AlertTriangle, ChevronLeft, ChevronRight, Calendar } from 'lucide-react';
import { cn } from '../../lib/utils';
import { motion, AnimatePresence } from 'framer-motion';
import { format, addDays, subDays, isSameDay } from 'date-fns';

export const QueueManagement = () => {
  const { approveToProcessing, cancelAppointment, getQueueDataByDate } = useSimulation();
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [cancelModal, setCancelModal] = useState<{ id: string, name: string } | null>(null);
  const [cancelReason, setCancelReason] = useState('');

  // Fetch data for selected date
  const queueData = useMemo(() => getQueueDataByDate(selectedDate), [selectedDate, getQueueDataByDate]);
  const isToday = isSameDay(selectedDate, new Date());
  const isFuture = selectedDate > new Date() && !isToday;

  const handleCancelSubmit = () => {
    if (cancelModal && cancelReason) {
      cancelAppointment(cancelModal.id, cancelReason);
      setCancelModal(null);
      setCancelReason('');
    }
  };

  const StatusBadge = ({ status }: { status: string }) => {
    const styles = {
      waiting: 'bg-gray-100 text-gray-700',
      ready: 'bg-green-100 text-green-700',
      serving: 'bg-blue-500 text-white animate-pulse',
      completed: 'bg-green-100 text-green-800',
      cancelled: 'bg-red-50 text-red-600',
      processing: 'bg-indigo-100 text-indigo-700',
      upcoming: 'bg-blue-50 text-blue-600'
    };
    
    const labels = {
      waiting: 'Đang chờ',
      ready: 'Sẵn sàng',
      serving: 'Đang phục vụ',
      completed: 'Hoàn thành',
      cancelled: 'Đã huỷ',
      processing: 'Đã tiếp nhận',
      upcoming: 'Sắp tới'
    };

    return (
      <span className={cn("px-2.5 py-0.5 rounded-full text-xs font-bold whitespace-nowrap", styles[status as keyof typeof styles])}>
        {labels[status as keyof typeof labels] || status}
      </span>
    );
  };

  return (
    <div className="space-y-6">
      {/* Header & Date Controls */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Quản lý hàng chờ & Lịch hẹn</h1>
          <p className="text-gray-500 text-sm">Theo dõi trạng thái hồ sơ và lịch hẹn theo ngày.</p>
        </div>
        
        <div className="flex items-center gap-3 bg-white p-1.5 rounded-xl shadow-sm border border-gray-200">
          <button 
            onClick={() => setSelectedDate(subDays(selectedDate, 1))}
            className="p-2 hover:bg-gray-100 rounded-lg text-gray-600"
          >
            <ChevronLeft className="h-5 w-5" />
          </button>
          
          <div className="flex items-center gap-2 px-2 min-w-[140px] justify-center">
            <Calendar className="h-4 w-4 text-primary" />
            <span className="font-bold text-sm text-gray-900">
              {format(selectedDate, 'dd/MM/yyyy')}
            </span>
            {isToday && <span className="text-[10px] bg-blue-100 text-blue-700 px-1.5 py-0.5 rounded font-bold">Hôm nay</span>}
          </div>

          <button 
            onClick={() => setSelectedDate(addDays(selectedDate, 1))}
            className="p-2 hover:bg-gray-100 rounded-lg text-gray-600"
          >
            <ChevronRight className="h-5 w-5" />
          </button>
        </div>
      </div>

      {/* Stats Summary for Selected Date */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
          <p className="text-xs text-gray-500 uppercase font-bold">Tổng số</p>
          <p className="text-2xl font-bold text-gray-900">{queueData.length}</p>
        </div>
        <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
          <p className="text-xs text-gray-500 uppercase font-bold">Hoàn thành/Tiếp nhận</p>
          <p className="text-2xl font-bold text-green-600">
            {queueData.filter(i => ['completed', 'processing'].includes(i.status)).length}
          </p>
        </div>
        <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
          <p className="text-xs text-gray-500 uppercase font-bold">Đã huỷ</p>
          <p className="text-2xl font-bold text-red-600">
            {queueData.filter(i => i.status === 'cancelled').length}
          </p>
        </div>
        <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
          <p className="text-xs text-gray-500 uppercase font-bold">Đang chờ/Sắp tới</p>
          <p className="text-2xl font-bold text-blue-600">
            {queueData.filter(i => ['waiting', 'upcoming', 'ready'].includes(i.status)).length}
          </p>
        </div>
      </div>

      {/* Main Table */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-gray-50 border-b border-gray-200 text-xs uppercase text-gray-500 font-semibold tracking-wider">
                <th className="px-6 py-4">Số thứ tự</th>
                <th className="px-6 py-4">Thông tin công dân</th>
                <th className="px-6 py-4">Thủ tục</th>
                <th className="px-6 py-4">Thời gian</th>
                <th className="px-6 py-4">Trạng thái</th>
                <th className="px-6 py-4 text-right">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {queueData.length > 0 ? (
                queueData.map((item) => (
                  <tr key={item.id} className="hover:bg-blue-50/30 transition-colors group">
                    <td className="px-6 py-4">
                      <span className={cn(
                        "font-bold px-3 py-1 rounded-lg text-sm",
                        item.status === 'cancelled' ? "bg-gray-100 text-gray-400 decoration-line-through" : "bg-blue-100 text-blue-800"
                      )}>
                        {item.queueNumber}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <div className="h-8 w-8 bg-gray-100 rounded-full flex items-center justify-center text-gray-500">
                          <User className="h-4 w-4" />
                        </div>
                        <div>
                          <p className="font-medium text-gray-900">{item.citizenName}</p>
                          <p className="text-xs text-gray-500">ID: 001...123</p>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-2 text-sm text-gray-700">
                        <FileText className="h-4 w-4 text-gray-400" />
                        {item.procedure}
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-2 text-sm text-gray-700">
                        <Clock className="h-4 w-4 text-gray-400" />
                        {item.time}
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex flex-col items-start gap-1">
                        <StatusBadge status={item.status} />
                        {item.status === 'cancelled' && item.cancelReason && (
                          <span className="text-[10px] text-red-400 italic">Lý do: {item.cancelReason}</span>
                        )}
                      </div>
                    </td>
                    <td className="px-6 py-4 text-right">
                      {/* Only show actions for 'waiting' or 'ready' status AND if it's Today */}
                      {isToday && ['waiting', 'ready'].includes(item.status) ? (
                        <div className="flex items-center justify-end gap-2">
                          <Button 
                            size="sm" 
                            variant="success"
                            className="gap-2"
                            onClick={() => approveToProcessing(item.id)}
                          >
                            <CheckCircle className="h-4 w-4" />
                            Tiếp nhận
                          </Button>
                          <Button 
                            size="sm" 
                            variant="outline"
                            className="text-red-600 border-red-200 hover:bg-red-50"
                            onClick={() => setCancelModal({ id: item.id, name: item.citizenName })}
                          >
                            <XCircle className="h-4 w-4" />
                          </Button>
                        </div>
                      ) : (
                        <span className="text-xs text-gray-400 italic">
                          {isFuture ? 'Chưa đến giờ' : 'Đã kết thúc'}
                        </span>
                      )}
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={6} className="px-6 py-12 text-center text-gray-400">
                    <div className="flex flex-col items-center gap-2">
                      <Clock className="h-8 w-8 opacity-20" />
                      <p>Không có dữ liệu cho ngày này.</p>
                    </div>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Cancel Modal */}
      <AnimatePresence>
        {cancelModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
            <motion.div 
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              className="bg-white rounded-2xl shadow-xl w-full max-w-md overflow-hidden"
            >
              <div className="p-6">
                <div className="flex items-center gap-3 text-red-600 mb-4">
                  <div className="h-10 w-10 bg-red-100 rounded-full flex items-center justify-center">
                    <AlertTriangle className="h-5 w-5" />
                  </div>
                  <h3 className="text-lg font-bold">Huỷ lịch hẹn</h3>
                </div>
                
                <p className="text-gray-600 mb-4">
                  Bạn có chắc chắn muốn huỷ lịch hẹn của <span className="font-bold text-gray-900">{cancelModal.name}</span>?
                  Hành động này không thể hoàn tác.
                </p>

                <div className="space-y-2">
                  <label className="text-sm font-medium text-gray-700">Lý do huỷ:</label>
                  <select 
                    className="w-full p-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-red-500/20 focus:border-red-500"
                    value={cancelReason}
                    onChange={(e) => setCancelReason(e.target.value)}
                  >
                    <option value="">-- Chọn lý do --</option>
                    <option value="Sai thông tin hồ sơ">Sai thông tin hồ sơ</option>
                    <option value="Không đến đúng giờ">Không đến đúng giờ</option>
                    <option value="Thiếu giấy tờ gốc">Thiếu giấy tờ gốc</option>
                    <option value="Khác">Khác</option>
                  </select>
                </div>
              </div>
              
              <div className="bg-gray-50 p-4 flex justify-end gap-3">
                <Button variant="ghost" onClick={() => setCancelModal(null)}>Đóng</Button>
                <Button 
                  variant="danger" 
                  disabled={!cancelReason}
                  onClick={handleCancelSubmit}
                >
                  Xác nhận Huỷ
                </Button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
};
