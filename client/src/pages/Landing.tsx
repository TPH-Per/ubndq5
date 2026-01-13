import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Smartphone, Monitor, ArrowRight, Shield } from 'lucide-react';

export const Landing = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-50 flex items-center justify-center p-4">
      <div className="max-w-4xl w-full">
        <div className="text-center mb-12">
          <h1 className="text-4xl md:text-5xl font-bold text-gray-900 mb-4">
            Hệ thống Một cửa Điện tử Phú Thọ
          </h1>
          <p className="text-xl text-gray-600">
            Chọn giao diện để trải nghiệm bản demo
          </p>
        </div>

        <div className="grid md:grid-cols-2 gap-8">
          {/* Citizen App Card */}
          <div
            onClick={() => navigate('/citizen')}
            className="bg-white rounded-2xl p-8 shadow-xl hover:shadow-2xl transition-all cursor-pointer group border-2 border-transparent hover:border-primary/20"
          >
            <div className="h-16 w-16 bg-blue-100 rounded-2xl flex items-center justify-center mb-6 text-primary group-hover:scale-110 transition-transform">
              <Smartphone className="h-8 w-8" />
            </div>
            <h2 className="text-2xl font-bold text-gray-900 mb-3">Ứng dụng Công dân (Mini App)</h2>
            <p className="text-gray-500 mb-6">
              Giao diện di động cho người dân đặt lịch hẹn, theo dõi hàng chờ trực tuyến và quản lý hồ sơ cá nhân.
            </p>
            <div className="flex items-center text-primary font-semibold">
              Mở ứng dụng <ArrowRight className="ml-2 h-5 w-5 group-hover:translate-x-1 transition-transform" />
            </div>
          </div>

          {/* Admin Portal Card */}
          <div
            onClick={() => navigate('/admin/dashboard')}
            className="bg-white rounded-2xl p-8 shadow-xl hover:shadow-2xl transition-all cursor-pointer group border-2 border-transparent hover:border-slate-800/20"
          >
            <div className="h-16 w-16 bg-slate-100 rounded-2xl flex items-center justify-center mb-6 text-slate-800 group-hover:scale-110 transition-transform">
              <Shield className="h-8 w-8" />
            </div>
            <h2 className="text-2xl font-bold text-gray-900 mb-3">Cổng Quản Trị Viên</h2>
            <p className="text-gray-500 mb-6">
              Dành cho Admin quản lý tài khoản, cấu hình hệ thống quầy và xem báo cáo tổng hợp.
            </p>
            <div className="flex items-center text-slate-800 font-semibold">
              Truy cập Admin <ArrowRight className="ml-2 h-5 w-5 group-hover:translate-x-1 transition-transform" />
            </div>
          </div>

          {/* Staff Portal Card */}
          <div
            onClick={() => navigate('/staff/dashboard')}
            className="bg-white rounded-2xl p-8 shadow-xl hover:shadow-2xl transition-all cursor-pointer group border-2 border-transparent hover:border-[#003366]/20"
          >
            <div className="h-16 w-16 bg-indigo-100 rounded-2xl flex items-center justify-center mb-6 text-[#003366] group-hover:scale-110 transition-transform">
              <Monitor className="h-8 w-8" />
            </div>
            <h2 className="text-2xl font-bold text-gray-900 mb-3">Cổng Quản trị Cán bộ</h2>
            <p className="text-gray-500 mb-6">
              Giao diện dành cho cán bộ để quản lý hàng chờ, xử lý hồ sơ và xem báo cáo thống kê hiệu quả.
            </p>
            <div className="flex items-center text-[#003366] font-semibold">
              Truy cập cổng <ArrowRight className="ml-2 h-5 w-5 group-hover:translate-x-1 transition-transform" />
            </div>
          </div>
        </div>

        <div className="mt-12 text-center text-sm text-gray-400">
          Demo Application • Built with React & Tailwind
        </div>
      </div>
    </div>
  );
};
