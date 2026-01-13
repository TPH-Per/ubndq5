import React from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { Home, Calendar, FileText, User, Bell, ChevronLeft } from 'lucide-react';
import { cn } from '../lib/utils';

export const CitizenLayout = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const isHome = location.pathname === '/citizen';
  const isBooking = location.pathname.includes('/citizen/booking');

  const navItems = [
    { icon: Home, label: 'Trang chủ', path: '/citizen' },
    { icon: Calendar, label: 'Lịch hẹn', path: '/citizen/appointments' },
    { icon: FileText, label: 'Hồ sơ', path: '/citizen/documents' },
    { icon: User, label: 'Cá nhân', path: '/citizen/profile' },
  ];

  // Helper to determine header title based on current route
  const getPageTitle = () => {
    const path = location.pathname;
    if (path.includes('/citizen/booking')) return 'Đặt lịch làm việc';
    if (path.includes('/citizen/feedback')) return 'Góp ý & Phản ánh';
    if (path.includes('/citizen/profile')) return 'Thông tin cá nhân';
    if (path.includes('/citizen/appointments')) return 'Lịch hẹn của tôi';
    if (path.includes('/citizen/documents')) return 'Hồ sơ của tôi';
    if (path.includes('/citizen/queue')) return 'Theo dõi hàng chờ';
    return 'Chi tiết dịch vụ';
  };

  return (
    <div className="h-[100dvh] w-full bg-gray-100 flex justify-center items-center overflow-hidden">
      {/* Mobile Container Simulation */}
      <div className="w-full max-w-md bg-white h-full shadow-2xl relative flex flex-col overflow-hidden transform">

        {/* Header - Hidden on Home */}
        {!isHome && (
          <header className="bg-primary text-white p-4 sticky top-0 z-50 shadow-md shrink-0">
            <div className="flex items-center justify-between">
              <div className="flex items-center w-full">
                <button onClick={() => navigate(-1)} className="mr-3 p-1 hover:bg-white/10 rounded-full transition-colors">
                  <ChevronLeft className="h-6 w-6" />
                </button>
                <h1 className="font-bold text-lg flex-1 text-center mr-8 truncate">
                  {getPageTitle()}
                </h1>
              </div>
            </div>
          </header>
        )}

        {/* Main Content */}
        <main className="flex-1 overflow-y-auto pb-20 bg-gray-50 scroll-smooth">
          <Outlet />
        </main>

        {/* Bottom Navigation */}
        {!isBooking && (
          <nav className="bg-white border-t border-gray-200 absolute bottom-0 w-full z-40 pb-safe">
            <div className="flex justify-around items-center h-16">
              {navItems.map((item) => {
                const isActive = location.pathname === item.path;
                return (
                  <button
                    key={item.label}
                    onClick={() => navigate(item.path)}
                    className={cn(
                      "flex flex-col items-center justify-center w-full h-full space-y-1",
                      isActive ? "text-primary" : "text-gray-400"
                    )}
                  >
                    <item.icon className={cn("h-6 w-6", isActive && "fill-current")} />
                    <span className="text-[10px] font-medium">{item.label}</span>
                  </button>
                );
              })}
            </div>
          </nav>
        )}
      </div>
    </div>
  );
};
