import React from 'react';
import { Outlet, NavLink } from 'react-router-dom';
import { 
  LayoutDashboard, 
  ListOrdered, 
  FolderOpen, 
  Settings, 
  Search, 
  Bell, 
  LogOut,
  Building2
} from 'lucide-react';
import { cn } from '../lib/utils';

export const StaffLayout = () => {
  const sidebarItems = [
    { icon: LayoutDashboard, label: 'Tổng quan', path: '/staff/dashboard' },
    { icon: ListOrdered, label: 'Quản lý hàng chờ', path: '/staff/queue' },
    { icon: FolderOpen, label: 'Xử lý hồ sơ', path: '/staff/documents' },
    { icon: Settings, label: 'Cài đặt', path: '/staff/settings' },
  ];

  const staffInfo = {
    name: 'Nguyễn Văn Bộ',
    id: 'NV8829',
    role: 'Cán bộ Một cửa',
    avatar: 'NB',
    counter: 'Quầy A - Tầng 1'
  };

  return (
    <div className="min-h-screen bg-gray-100 flex">
      {/* Sidebar */}
      <aside className="w-72 bg-[#003366] text-white fixed h-full z-20 hidden md:flex flex-col shadow-xl">
        {/* Logo Area */}
        <div className="p-6 pb-4">
          <div className="flex items-center gap-3 mb-6">
            <div className="h-10 w-10 bg-white rounded-xl flex items-center justify-center text-[#003366] font-bold shadow-lg overflow-hidden p-1">
              <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/Emblem_of_Vietnam.svg/2048px-Emblem_of_Vietnam.svg.png" alt="Logo" className="h-full w-full object-contain" />
            </div>
            <div>
              <h1 className="font-bold text-base uppercase tracking-wide">Phú Thọ</h1>
              <p className="text-xs text-blue-200">Hệ thống Một cửa</p>
            </div>
          </div>

          {/* Counter Info Widget in Sidebar */}
          <div className="bg-white/10 rounded-xl p-4 border border-white/10 backdrop-blur-sm shadow-inner">
            <div className="flex items-start gap-3">
              <div className="p-2 bg-blue-500/20 rounded-lg shrink-0">
                <Building2 className="h-5 w-5 text-blue-300" />
              </div>
              <div>
                <p className="text-[10px] text-blue-200 font-bold uppercase tracking-wider mb-1">Đang làm việc tại</p>
                <p className="font-bold text-white text-lg leading-tight">{staffInfo.counter}</p>
                <div className="flex items-center gap-1.5 mt-2">
                  <span className="relative flex h-2 w-2">
                    <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
                    <span className="relative inline-flex rounded-full h-2 w-2 bg-green-500"></span>
                  </span>
                  <span className="text-[10px] text-green-300 font-medium">Đang hoạt động</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Navigation */}
        <nav className="flex-1 px-4 space-y-1.5 overflow-y-auto py-4">
          <p className="px-4 text-[10px] font-bold text-blue-300/50 uppercase tracking-wider mb-2">Menu chính</p>
          {sidebarItems.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) => cn(
                "flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-all duration-200 group",
                isActive 
                  ? "bg-white text-[#003366] shadow-md translate-x-1" 
                  : "text-blue-100 hover:bg-white/10 hover:text-white hover:translate-x-1"
              )}
            >
              {({ isActive }) => (
                <>
                  <item.icon className={cn("h-5 w-5 transition-colors", isActive ? "text-[#003366]" : "text-blue-300 group-hover:text-white")} />
                  {item.label}
                </>
              )}
            </NavLink>
          ))}
        </nav>

        {/* User Profile at Bottom of Sidebar */}
        <div className="p-4 border-t border-white/10 bg-[#002b55]">
          <div className="flex items-center gap-3 mb-4 bg-black/20 p-3 rounded-xl border border-white/5">
            <div className="h-10 w-10 bg-gradient-to-br from-blue-400 to-blue-600 rounded-full flex items-center justify-center text-white font-bold border-2 border-white/20 shadow-inner">
              {staffInfo.avatar}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-bold text-white truncate">{staffInfo.name}</p>
              <p className="text-xs text-blue-300 truncate">{staffInfo.role}</p>
            </div>
          </div>
          <button className="flex items-center justify-center gap-2 w-full px-4 py-2 bg-white/5 hover:bg-white/10 border border-white/10 rounded-lg text-xs font-medium text-blue-200 hover:text-white transition-colors group">
            <LogOut className="h-4 w-4 group-hover:text-red-300 transition-colors" />
            Đăng xuất
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <div className="flex-1 md:ml-72 flex flex-col min-h-screen transition-all duration-300">
        {/* Top Header */}
        <header className="bg-white border-b border-gray-200 h-16 px-6 flex items-center justify-between sticky top-0 z-10 shadow-sm">
          <div className="flex items-center gap-4 flex-1">
            <div className="relative w-96 max-w-full">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
              <input 
                type="text" 
                placeholder="Tìm kiếm hồ sơ, công dân..."
                className="w-full pl-10 pr-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
              />
            </div>
          </div>

          <div className="flex items-center gap-4">
            <button className="relative p-2 text-gray-500 hover:bg-gray-100 rounded-full transition-colors">
              <Bell className="h-5 w-5" />
              <span className="absolute top-1.5 right-1.5 h-2 w-2 bg-red-500 rounded-full border-2 border-white"></span>
            </button>
          </div>
        </header>

        <main className="flex-1 p-6 overflow-y-auto bg-gray-50/50">
          <Outlet />
        </main>
      </div>
    </div>
  );
};
