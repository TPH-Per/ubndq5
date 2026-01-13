import React, { useState } from 'react';
import { MessageSquare, History, FileText, Download, Star } from 'lucide-react';
import { cn } from '../../lib/utils';
import { Button } from '../../components/ui/Button';

export const AdminReports = () => {
    const [activeTab, setActiveTab] = useState<'feedback' | 'history'>('feedback');

    const feedbacks = [
        { id: 1, user: 'Lê Văn C', date: '2025-10-24', rating: 5, content: 'Dịch vụ rất nhanh, cán bộ nhiệt tình.', status: 'New' },
        { id: 2, user: 'Nguyễn Thị B', date: '2025-10-23', rating: 3, content: 'Chờ hơi lâu vào buổi sáng.', status: 'Read' },
    ];

    const history = [
        { id: 'TXN-001', date: '2025-10-25 08:30', service: 'Đăng ký kết hôn', citizen: 'Phạm Văn D', counter: 'Quầy C', officer: 'Nguyễn Văn Bộ' },
        { id: 'TXN-002', date: '2025-10-25 09:15', service: 'Sao y công chứng', citizen: 'Trần Thị E', counter: 'Quầy B', officer: 'Trần Thị Mai' },
    ];

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h1 className="text-2xl font-bold text-gray-900">Báo cáo & Dữ liệu</h1>
                <Button variant="outline" className="gap-2">
                    <Download className="h-4 w-4" /> Xuất dữ liệu
                </Button>
            </div>

            {/* Tabs */}
            <div className="flex border-b border-gray-200">
                <button
                    onClick={() => setActiveTab('feedback')}
                    className={cn(
                        "px-6 py-3 text-sm font-medium border-b-2 transition-colors flex items-center gap-2",
                        activeTab === 'feedback'
                            ? "border-indigo-600 text-indigo-600"
                            : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
                    )}
                >
                    <MessageSquare className="h-4 w-4" />
                    Phản ánh - Góp ý
                </button>
                <button
                    onClick={() => setActiveTab('history')}
                    className={cn(
                        "px-6 py-3 text-sm font-medium border-b-2 transition-colors flex items-center gap-2",
                        activeTab === 'history'
                            ? "border-indigo-600 text-indigo-600"
                            : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
                    )}
                >
                    <History className="h-4 w-4" />
                    Lịch sử dịch vụ
                </button>
            </div>

            {/* Content */}
            <div className="bg-white rounded-xl shadow-sm border border-gray-100 min-h-[400px]">
                {activeTab === 'feedback' ? (
                    <div className="divide-y divide-gray-100">
                        {feedbacks.map((item) => (
                            <div key={item.id} className="p-6 hover:bg-gray-50 transition-colors">
                                <div className="flex justify-between items-start mb-2">
                                    <div className="flex items-center gap-2">
                                        <span className="font-bold text-gray-900">{item.user}</span>
                                        <span className="text-xs text-gray-500">• {item.date}</span>
                                        {item.status === 'New' && <span className="text-[10px] bg-red-100 text-red-600 px-1.5 py-0.5 rounded font-bold">MỚI</span>}
                                    </div>
                                    <div className="flex">
                                        {[...Array(5)].map((_, i) => (
                                            <Star key={i} className={cn("h-4 w-4", i < item.rating ? "text-yellow-400 fill-current" : "text-gray-200")} />
                                        ))}
                                    </div>
                                </div>
                                <p className="text-gray-600 text-sm">{item.content}</p>
                            </div>
                        ))}
                    </div>
                ) : (
                    <table className="w-full text-sm text-left">
                        <thead className="bg-gray-50 text-gray-500 font-medium">
                            <tr>
                                <th className="px-6 py-4">Mã GD</th>
                                <th className="px-6 py-4">Thời gian</th>
                                <th className="px-6 py-4">Dịch vụ</th>
                                <th className="px-6 py-4">Công dân</th>
                                <th className="px-6 py-4">Quầy xử lý</th>
                                <th className="px-6 py-4">Cán bộ</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-100">
                            {history.map((record) => (
                                <tr key={record.id} className="hover:bg-gray-50">
                                    <td className="px-6 py-4 font-mono text-xs font-bold text-indigo-600">{record.id}</td>
                                    <td className="px-6 py-4">{record.date}</td>
                                    <td className="px-6 py-4 font-medium">{record.service}</td>
                                    <td className="px-6 py-4">{record.citizen}</td>
                                    <td className="px-6 py-4">{record.counter}</td>
                                    <td className="px-6 py-4 text-gray-500">{record.officer}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    );
};
