import React from 'react';
import { Outlet, NavLink } from 'react-router-dom';
import {
    LayoutDashboard,
    Users,
    Building2,
    FileBarChart,
    LogOut,
    Search,
    Bell,
    Settings
} from 'lucide-react';
import { cn } from '../lib/utils';

export const AdminLayout = () => {
    const sidebarItems = [
        { icon: LayoutDashboard, label: 'Tổng quan', path: '/admin/dashboard' },
        { icon: Users, label: 'Quản lý tài khoản', path: '/admin/accounts' },
        { icon: Building2, label: 'Quản lý quầy', path: '/admin/counters' },
        { icon: FileBarChart, label: 'Báo cáo & Góp ý', path: '/admin/reports' },
        { icon: Settings, label: 'Cài đặt hệ thống', path: '/admin/settings' },
    ];

    const adminInfo = {
        name: 'Administrator',
        role: 'Quản trị viên',
        avatar: 'AD'
    };

    return (
        <div className="min-h-screen bg-gray-100 flex">
            {/* Sidebar - Dark Purple/Slate Theme for Admin */}
            <aside className="w-72 bg-slate-900 text-white fixed h-full z-20 hidden md:flex flex-col shadow-xl">
                {/* Logo Area */}
                <div className="p-6 pb-4">
                    <div className="flex items-center gap-3 mb-6">
                        <div className="h-10 w-10 bg-white rounded-xl flex items-center justify-center text-slate-900 font-bold shadow-lg overflow-hidden p-1">
                            <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/Emblem_of_Vietnam.svg/2048px-Emblem_of_Vietnam.svg.png" alt="Logo" className="h-full w-full object-contain" />
                        </div>
                        <div>
                            <h1 className="font-bold text-base uppercase tracking-wide">Phú Thọ</h1>
                            <p className="text-xs text-slate-400">Trang Quản Trị</p>
                        </div>
                    </div>
                </div>

                {/* Navigation */}
                <nav className="flex-1 px-4 space-y-1.5 overflow-y-auto py-4">
                    <p className="px-4 text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-2">QUẢN TRỊ</p>
                    {sidebarItems.map((item) => (
                        <NavLink
                            key={item.path}
                            to={item.path}
                            className={({ isActive }) => cn(
                                "flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-all duration-200 group",
                                isActive
                                    ? "bg-indigo-600 text-white shadow-md translate-x-1"
                                    : "text-slate-400 hover:bg-white/5 hover:text-white hover:translate-x-1"
                            )}
                        >
                            {({ isActive }) => (
                                <>
                                    <item.icon className={cn("h-5 w-5 transition-colors", isActive ? "text-white" : "text-slate-500 group-hover:text-white")} />
                                    {item.label}
                                </>
                            )}
                        </NavLink>
                    ))}
                </nav>

                {/* User Profile at Bottom of Sidebar */}
                <div className="p-4 border-t border-white/5 bg-slate-950">
                    <div className="flex items-center gap-3 mb-4 bg-white/5 p-3 rounded-xl border border-white/5">
                        <div className="h-10 w-10 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-full flex items-center justify-center text-white font-bold border-2 border-white/20 shadow-inner">
                            {adminInfo.avatar}
                        </div>
                        <div className="flex-1 min-w-0">
                            <p className="text-sm font-bold text-white truncate">{adminInfo.name}</p>
                            <p className="text-xs text-slate-400 truncate">{adminInfo.role}</p>
                        </div>
                    </div>
                    <button className="flex items-center justify-center gap-2 w-full px-4 py-2 bg-white/5 hover:bg-white/10 border border-white/5 rounded-lg text-xs font-medium text-slate-400 hover:text-white transition-colors group">
                        <LogOut className="h-4 w-4 group-hover:text-red-400 transition-colors" />
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
                                placeholder="Tìm kiếm..."
                                className="w-full pl-10 pr-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all"
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
