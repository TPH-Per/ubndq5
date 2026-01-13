import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSimulation } from '../../context/SimulationContext';
import { Calendar, Clock, MapPin, ChevronRight, AlertCircle, CheckCircle2, XCircle } from 'lucide-react';
import { cn } from '../../lib/utils';
import { Button } from '../../components/ui/Button';

export const MyAppointments = () => {
  const navigate = useNavigate();
  const { myAppointments } = useSimulation();
  const [activeTab, setActiveTab] = useState<'upcoming' | 'completed' | 'cancelled'>('upcoming');

  const filteredAppointments = myAppointments.filter(apt => {
    if (activeTab === 'upcoming') return ['upcoming', 'waiting', 'ready', 'serving'].includes(apt.status);
    return apt.status === activeTab;
  });

  const tabLabels = {
    upcoming: 'Sắp tới',
    completed: 'Hoàn thành',
    cancelled: 'Đã huỷ'
  };

  const StatusBadge = ({ status }: { status: string }) => {
    const styles = {
      upcoming: 'bg-blue-100 text-blue-700',
      waiting: 'bg-yellow-100 text-yellow-700',
      ready: 'bg-green-100 text-green-700',
      serving: 'bg-green-500 text-white animate-pulse',
      completed: 'bg-gray-100 text-gray-600',
      cancelled: 'bg-red-50 text-red-600'
    };
    
    const labels = {
      upcoming: 'Sắp tới',
      waiting: 'Đang chờ',
      ready: 'Sẵn sàng',
      serving: 'Đang phục vụ',
      completed: 'Hoàn thành',
      cancelled: 'Đã huỷ'
    };
    
    return (
      <span className={cn("px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wide", styles[status as keyof typeof styles])}>
        {labels[status as keyof typeof labels] || status}
      </span>
    );
  };

  return (
    <div className="min-h-full bg-gray-50 flex flex-col">
      {/* Tabs */}
      <div className="bg-white p-2 sticky top-0 z-10 shadow-sm">
        <div className="flex bg-gray-100 p-1 rounded-lg">
          {['upcoming', 'completed', 'cancelled'].map((tab) => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab as any)}
              className={cn(
                "flex-1 py-2 text-xs font-medium rounded-md capitalize transition-all",
                activeTab === tab ? "bg-white text-primary shadow-sm" : "text-gray-500 hover:text-gray-700"
              )}
            >
              {tabLabels[tab as keyof typeof tabLabels]}
            </button>
          ))}
        </div>
      </div>

      {/* List */}
      <div className="p-4 space-y-4 flex-1">
        {filteredAppointments.length > 0 ? (
          filteredAppointments.map((apt) => (
            <div 
              key={apt.id}
              onClick={() => ['upcoming', 'waiting', 'ready', 'serving'].includes(apt.status) && navigate(`/citizen/queue/${apt.id}`)}
              className="bg-white rounded-xl p-4 shadow-sm border border-gray-100 active:scale-[0.99] transition-transform cursor-pointer"
            >
              <div className="flex justify-between items-start mb-3">
                <div className="flex items-center gap-3">
                  <div className={cn(
                    "h-12 w-12 rounded-xl flex flex-col items-center justify-center font-bold text-lg",
                    apt.status === 'cancelled' ? "bg-gray-100 text-gray-400" : "bg-blue-50 text-primary"
                  )}>
                    <span className="text-[10px] uppercase opacity-70">Số</span>
                    {apt.queueNumber}
                  </div>
                  <div>
                    <h3 className="font-bold text-gray-900">{apt.procedure}</h3>
                    <p className="text-xs text-gray-500">{apt.date}</p>
                  </div>
                </div>
                <StatusBadge status={apt.status} />
              </div>

              <div className="space-y-2 mb-4">
                <div className="flex items-center gap-2 text-sm text-gray-600">
                  <Clock className="h-4 w-4 text-gray-400" />
                  <span>{apt.time} (Dự kiến)</span>
                </div>
                <div className="flex items-center gap-2 text-sm text-gray-600">
                  <MapPin className="h-4 w-4 text-gray-400" />
                  <span>{apt.counter || 'Chưa xếp quầy'}</span>
                </div>
              </div>

              {['upcoming', 'waiting', 'ready', 'serving'].includes(apt.status) && (
                <Button fullWidth size="sm" variant="secondary" className="text-xs h-8">
                  Theo dõi trực tiếp <ChevronRight className="h-3 w-3 ml-1" />
                </Button>
              )}
            </div>
          ))
        ) : (
          <div className="flex flex-col items-center justify-center py-12 text-center">
            <div className="h-16 w-16 bg-gray-100 rounded-full flex items-center justify-center mb-4">
              <Calendar className="h-8 w-8 text-gray-300" />
            </div>
            <h3 className="font-bold text-gray-900">Không có lịch hẹn</h3>
            <p className="text-sm text-gray-500 mt-1">Bạn không có lịch hẹn nào trong mục này.</p>
            {activeTab === 'upcoming' && (
              <Button onClick={() => navigate('/citizen/booking/step1')} className="mt-4">
                Đặt lịch ngay
              </Button>
            )}
          </div>
        )}
      </div>
    </div>
  );
};
