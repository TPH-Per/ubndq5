import React, { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Calendar, List, FileText, Info, ChevronRight, Activity, X } from 'lucide-react';
import { cn } from '../../lib/utils';
import { useSimulation } from '../../context/SimulationContext';
import { MOCK_NEWS } from '../../data/mock';
import heroLogo from '../../data/citizen_logo_blue_bg.png';
import { motion, AnimatePresence } from 'framer-motion';

export const CitizenHome = () => {
  const navigate = useNavigate();
  const { myAppointments } = useSimulation();

  // Draggable button state
  const [position, setPosition] = useState({ x: 300, y: 500 });
  const [isDragging, setIsDragging] = useState(false);
  const [showActiveCard, setShowActiveCard] = useState(false);
  const dragStartPos = useRef({ x: 0, y: 0 });
  const buttonRef = useRef<HTMLDivElement>(null);

  // Handle Dragging
  const handleTouchStart = (e: React.TouchEvent | React.MouseEvent) => {
    setIsDragging(false);
    const clientX = 'touches' in e ? e.touches[0].clientX : (e as React.MouseEvent).clientX;
    const clientY = 'touches' in e ? e.touches[0].clientY : (e as React.MouseEvent).clientY;
    dragStartPos.current = { x: clientX - position.x, y: clientY - position.y };
  };

  const handleTouchMove = (e: React.TouchEvent | React.MouseEvent) => {
    setIsDragging(true);
    const clientX = 'touches' in e ? e.touches[0].clientX : (e as React.MouseEvent).clientX;
    const clientY = 'touches' in e ? e.touches[0].clientY : (e as React.MouseEvent).clientY;

    // Boundary checks could be added here
    setPosition({
      x: clientX - dragStartPos.current.x,
      y: clientY - dragStartPos.current.y
    });
  };

  const handleClick = () => {
    if (!isDragging) {
      setShowActiveCard(!showActiveCard);
    }
  };

  // Check if there is an active appointment (waiting or serving)
  const activeAppointment = myAppointments.find(a =>
    ['waiting', 'serving', 'ready'].includes(a.status)
  );

  const ActionCard = ({ title, subtitle, icon: Icon, color, onClick, badge, delay }: any) => (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay: delay * 0.1 }}
      whileTap={{ scale: 0.96 }}
      onClick={onClick}
      className="bg-white p-4 rounded-2xl shadow-sm border border-gray-100 flex flex-col gap-3 relative overflow-hidden group hover:shadow-md transition-shadow cursor-pointer h-full"
    >
      <div className={cn("p-3 rounded-xl w-fit", color)}>
        <Icon className="h-6 w-6 text-white" />
      </div>
      <div>
        <h3 className="font-bold text-gray-900 leading-tight mb-1">{title}</h3>
        {subtitle && <p className="text-xs text-gray-500 line-clamp-2">{subtitle}</p>}
      </div>

      {badge && (
        <span className="absolute top-3 right-3 bg-red-500 text-white text-[10px] font-bold px-1.5 py-0.5 rounded-full shadow-sm z-10">
          {badge}
        </span>
      )}

      {/* Decorative circle */}
      <div className={cn("absolute -bottom-4 -right-4 w-20 h-20 rounded-full opacity-5 pointer-events-none transition-transform group-hover:scale-150", color.replace('bg-', 'text-'))} />
    </motion.div>
  );

  return (
    <div className="pb-6 bg-gray-50 min-h-full">
      {/* Hero Section */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        className="relative bg-primary text-white overflow-hidden rounded-b-[30px] shadow-lg mb-6 -mt-1 h-[23vh] flex flex-col justify-center"
      >
        {/* Background Decor */}
        <div className="absolute top-0 right-0 w-64 h-64 bg-white/10 rounded-full -mr-20 -mt-20 blur-3xl pointer-events-none animate-pulse"></div>
        <div className="absolute bottom-0 left-0 w-40 h-40 bg-blue-400/20 rounded-full -ml-10 -mb-10 blur-2xl pointer-events-none"></div>

        <div className="relative z-10 px-6 flex flex-row items-center justify-between gap-4 h-full py-4">
          <div className="flex flex-col items-start text-left justify-center z-10">
            <h2 className="text-[10px] font-bold uppercase tracking-widest text-blue-100 mb-1">
              UBND Phường Chợ Lớn
            </h2>
            <h1 className="text-xl font-black uppercase leading-none tracking-tight mb-3">
              Chợ Lớn HCMC<br />
              <span className="text-yellow-400">OneTouch</span>
            </h1>
            <div className="inline-flex items-center gap-1.5 bg-white/10 backdrop-blur-md border border-white/20 px-3 py-1 rounded-full">
              <div className="h-1.5 w-1.5 rounded-full bg-green-400 animate-pulse shadow-[0_0_8px_rgba(74,222,128,0.8)]"></div>
              <span className="text-[10px] font-bold uppercase tracking-wider text-white/90">Số hóa hành chính công</span>
            </div>
          </div>

          <motion.div
            initial={{ scale: 0.8, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            transition={{ delay: 0.2, type: "spring" }}
            className="relative"
          >
            <div className="absolute inset-0 bg-white/20 blur-xl rounded-full transform scale-110"></div>
            <img
              src={heroLogo}
              alt="Quốc huy"
              className="h-[120px] aspect-square object-contain rounded-full border-2 border-white/30 shadow-2xl relative z-10"
            />
          </motion.div>
        </div>
      </motion.div>

      <div className="px-4 space-y-6">
        {/* Floating Active Appointment Component */}
        <AnimatePresence>
          {activeAppointment && (
            <>
              {/* Draggable Floating Button */}
              <motion.div
                key="floating-btn"
                initial={{ scale: 0 }}
                animate={{ scale: 1 }}
                exit={{ scale: 0 }}
                ref={buttonRef}
                style={{
                  position: 'fixed',
                  left: position.x,
                  top: position.y,
                  zIndex: 50,
                  touchAction: 'none'
                }}
                className="cursor-move"
                onTouchStart={handleTouchStart}
                onTouchMove={handleTouchMove}
                onMouseDown={handleTouchStart}
                onMouseMove={(e) => {
                  if (e.buttons === 1) handleTouchMove(e);
                }}
                onClick={handleClick}
                whileTap={{ scale: 0.9 }}
              >
                <div className="h-14 w-14 bg-gradient-to-r from-yellow-500 to-orange-500 rounded-full shadow-lg border-2 border-white flex items-center justify-center relative overflow-hidden">
                  <div className="absolute inset-0 bg-white/20 rounded-full animate-ping opacity-75"></div>
                  <Activity className="h-6 w-6 text-white relative z-10" />
                  <span className="absolute top-0 right-0 h-3 w-3 bg-red-500 border-2 border-white rounded-full z-20"></span>
                </div>
              </motion.div>

              {/* Modal Overlay / Card */}
              {showActiveCard && (
                <motion.div
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  exit={{ opacity: 0 }}
                  className="fixed inset-0 z-[60] flex items-center justify-center p-4 bg-black/40 backdrop-blur-[2px]"
                  onClick={() => setShowActiveCard(false)}
                >
                  <motion.div
                    initial={{ scale: 0.9, opacity: 0, y: 20 }}
                    animate={{ scale: 1, opacity: 1, y: 0 }}
                    exit={{ scale: 0.9, opacity: 0, y: 20 }}
                    className="w-full max-w-xs bg-white rounded-3xl shadow-2xl overflow-hidden"
                    onClick={(e) => e.stopPropagation()}
                  >
                    <div
                      onClick={() => {
                        navigate(`queue/${activeAppointment.id}`);
                        setShowActiveCard(false);
                      }}
                      className="bg-gradient-to-br from-yellow-500 to-orange-600 p-6 text-white cursor-pointer relative overflow-hidden"
                    >
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          setShowActiveCard(false);
                        }}
                        className="absolute top-4 right-4 p-1 hover:bg-white/20 rounded-full transition-colors z-20"
                      >
                        <X className="h-5 w-5 text-white" />
                      </button>

                      {/* Abstract Background Shapes */}
                      <div className="absolute right-0 top-0 h-40 w-40 bg-white/10 rounded-full -mr-16 -mt-16 blur-2xl" />
                      <div className="absolute left-0 bottom-0 h-32 w-32 bg-orange-700/20 rounded-full -ml-10 -mb-10 blur-xl" />

                      <div className="flex items-center gap-2 mb-4 relative z-10">
                        <span className="animate-pulse h-2 w-2 rounded-full bg-green-400 shadow-[0_0_8px_rgba(74,222,128,1)]" />
                        <p className="text-[10px] font-bold uppercase opacity-90 tracking-widest border border-white/20 px-2 py-0.5 rounded-full bg-white/5">Đang phục vụ</p>
                      </div>

                      <div className="relative z-10 text-center py-2">
                        <h3 className="text-5xl font-black mb-1 drop-shadow-sm tracking-tighter">{activeAppointment.queueNumber}</h3>
                        <p className="text-sm font-medium opacity-90 line-clamp-1">{activeAppointment.procedure}</p>
                      </div>

                      <div className="mt-6 pt-4 border-t border-white/20 flex justify-between items-center relative z-10">
                        <div>
                          <p className="text-[10px] uppercase opacity-75 mb-0.5">Thời gian ước tính</p>
                          <span className="text-sm font-bold flex items-center gap-1">
                            <Activity className="h-3 w-3" /> ~15 phút
                          </span>
                        </div>
                        <div className="bg-white text-orange-600 px-4 py-2 rounded-xl font-bold text-xs shadow-lg hover:bg-orange-50 transition-colors">
                          Xem chi tiết
                        </div>
                      </div>
                    </div>
                  </motion.div>
                </motion.div>
              )}
            </>
          )}
        </AnimatePresence>

        {/* Main Actions Grid - 2x2 Layout */}
        <div className="grid grid-cols-2 gap-4">
          <ActionCard
            title="Đặt lịch hẹn"
            subtitle="Đăng ký trực tuyến"
            icon={Calendar}
            color="bg-blue-500"
            delay={1}
            onClick={() => navigate('booking/step1')}
          />

          <ActionCard
            title="Lịch hẹn"
            subtitle="Xem danh sách"
            icon={List}
            color="bg-green-500"
            delay={2}
            badge={myAppointments.length > 0 ? myAppointments.length.toString() : undefined}
            onClick={() => navigate('appointments')}
          />

          <ActionCard
            title="Hồ sơ"
            subtitle="Tra cứu trạng thái"
            icon={FileText}
            color="bg-orange-500"
            delay={3}
            onClick={() => navigate('documents')}
          />

          <ActionCard
            title="Hướng dẫn"
            subtitle="Quy định & giấy tờ"
            icon={Info}
            color="bg-purple-500"
            delay={4}
            onClick={() => { }}
          />
        </div>

        {/* News Section */}
        <div>
          <div className="flex items-center justify-between mb-4 px-1">
            <h3 className="font-bold text-gray-900 text-lg">Tin tức nổi bật</h3>
            <span className="text-xs font-semibold text-primary cursor-pointer hover:underline">Xem tất cả</span>
          </div>
          <div className="space-y-4">
            {MOCK_NEWS.map((news, idx) => (
              <motion.div
                key={news.id}
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.4 + (idx * 0.1) }}
                className="bg-white rounded-2xl shadow-sm border border-gray-100 p-3 flex gap-4 active:scale-[0.99] transition-transform"
              >
                <img src={news.image} alt="" className="h-20 w-20 rounded-xl object-cover flex-shrink-0 bg-gray-100 shadow-sm" />
                <div className="flex-1 min-w-0 flex flex-col justify-center">
                  <h4 className="font-bold text-sm leading-tight text-gray-900 mb-1.5 line-clamp-2">{news.title}</h4>
                  <p className="text-xs text-gray-500 line-clamp-1 mb-2">{news.summary}</p>
                  <div className="flex items-center gap-2 text-[10px] text-gray-400">
                    <span className="bg-gray-100 px-2 py-0.5 rounded-full font-medium">Tin tức</span>
                    <span>• {news.date}</span>
                  </div>
                </div>
              </motion.div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
