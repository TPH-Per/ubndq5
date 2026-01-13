import React from 'react';
import { Users, Building2, MessageSquare, Activity, TrendingUp } from 'lucide-react';
import { cn } from '../../lib/utils';
import { Button } from '../../components/ui/Button';

export const AdminDashboard = () => {
    const stats = [
        { label: 'Tổng tài khoản', value: '12', icon: Users, color: 'bg-blue-500', trend: '+2 this week' },
        { label: 'Tổng số quầy', value: '8', icon: Building2, color: 'bg-indigo-500', trend: 'Stable' },
        { label: 'Phản ánh mới', value: '5', icon: MessageSquare, color: 'bg-orange-500', trend: '+3 today' },
        { label: 'Lượt phục vụ', value: '128', icon: Activity, color: 'bg-green-500', trend: '+12% vs yesterday' },
    ];

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h1 className="text-2xl font-bold text-gray-900">Tổng quan hệ thống</h1>
                <Button size="sm">Xuất báo cáo</Button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {stats.map((stat, idx) => (
                    <div key={idx} className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 dark:border-gray-800">
                        <div className="flex justify-between items-start mb-4">
                            <div className={cn("p-3 rounded-lg", stat.color)}>
                                <stat.icon className="h-6 w-6 text-white" />
                            </div>
                            <span className="flex items-center text-green-600 text-xs font-medium bg-green-50 px-2 py-1 rounded-full">
                                <TrendingUp className="h-3 w-3 mr-1" /> {stat.trend}
                            </span>
                        </div>
                        <h3 className="text-3xl font-bold text-gray-900 mb-1">{stat.value}</h3>
                        <p className="text-sm text-gray-500">{stat.label}</p>
                    </div>
                ))}
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* Recent Activity Mock */}
                <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
                    <h3 className="font-bold text-lg mb-4">Hoạt động gần đây</h3>
                    <div className="space-y-4">
                        {[1, 2, 3, 4, 5].map((i) => (
                            <div key={i} className="flex items-center gap-4 pb-4 border-b border-gray-50 last:border-0 last:pb-0">
                                <div className="h-10 w-10 rounded-full bg-gray-100 flex items-center justify-center text-gray-500 font-bold text-xs">
                                    AD
                                </div>
                                <div>
                                    <p className="text-sm font-medium text-gray-900">Admin đã cập nhật thông tin <span className="font-bold">Quầy A</span></p>
                                    <p className="text-xs text-gray-400">2 giờ trước</p>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

                {/* System Health / Alerts */}
                <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
                    <h3 className="font-bold text-lg mb-4">Trạng thái hệ thống</h3>
                    <div className="space-y-3">
                        <div className="flex items-center justify-between p-3 bg-green-50 text-green-700 rounded-lg">
                            <span className="text-sm font-medium flex items-center gap-2"><div className="h-2 w-2 rounded-full bg-green-500 animate-pulse" /> Máy chủ hoạt động tốt</span>
                            <span className="text-xs font-bold">99.9% Uptime</span>
                        </div>
                        <div className="flex items-center justify-between p-3 bg-yellow-50 text-yellow-700 rounded-lg">
                            <span className="text-sm font-medium flex items-center gap-2"><div className="h-2 w-2 rounded-full bg-yellow-500" /> Cảnh báo tải cao</span>
                            <span className="text-xs font-bold">11:00 AM</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};
