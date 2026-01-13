import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Shield, Lock, User } from 'lucide-react';
import { Button } from '../../components/ui/Button';

export const AdminLogin = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);

    const handleLogin = (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        // Simulate API call
        setTimeout(() => {
            setLoading(false);
            navigate('/admin/dashboard');
        }, 1000);
    };

    return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
            <div className="w-full max-w-md bg-white rounded-2xl shadow-xl overflow-hidden">
                <div className="bg-slate-900 p-8 text-center">
                    <div className="h-16 w-16 bg-white/10 rounded-2xl flex items-center justify-center mx-auto mb-4 backdrop-blur-sm">
                        <Shield className="h-8 w-8 text-white" />
                    </div>
                    <h1 className="text-2xl font-bold text-white mb-1">Quản Trị Hệ Thống</h1>
                    <p className="text-slate-400 text-sm">Đăng nhập để tiếp tục</p>
                </div>

                <form onSubmit={handleLogin} className="p-8 space-y-6">
                    <div className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1.5 ml-1">Tài khoản</label>
                            <div className="relative">
                                <User className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
                                <input
                                    type="text"
                                    defaultValue="admin"
                                    className="w-full pl-10 pr-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all font-medium"
                                    placeholder="Nhập tài khoản quản trị"
                                />
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1.5 ml-1">Mật khẩu</label>
                            <div className="relative">
                                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
                                <input
                                    type="password"
                                    defaultValue="password"
                                    className="w-full pl-10 pr-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all font-medium"
                                    placeholder="••••••••"
                                />
                            </div>
                        </div>
                    </div>

                    <Button
                        fullWidth
                        size="lg"
                        className="bg-slate-900 hover:bg-slate-800 h-12 text-base shadow-lg shadow-slate-900/20"
                        disabled={loading}
                    >
                        {loading ? 'Đang xác thực...' : 'Đăng nhập'}
                    </Button>

                    <div className="text-center">
                        <a href="/" className="text-sm text-slate-500 hover:text-slate-700 font-medium">
                            ← Quay lại trang chủ
                        </a>
                    </div>
                </form>
            </div>
        </div>
    );
};
