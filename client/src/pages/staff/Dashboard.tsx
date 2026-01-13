import React from 'react';
import { useSimulation } from '../../context/SimulationContext';
import { 
  Users, Clock, CheckCircle, TrendingUp, MoreHorizontal, 
  Phone, XCircle, FileText, ArrowRight 
} from 'lucide-react';
import { cn } from '../../lib/utils';
import { Button } from '../../components/ui/Button';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

const chartData = [
  { name: '8am', visitors: 10 },
  { name: '9am', visitors: 25 },
  { name: '10am', visitors: 45 },
  { name: '11am', visitors: 30 },
  { name: '12pm', visitors: 15 },
  { name: '1pm', visitors: 35 },
  { name: '2pm', visitors: 50 },
];

export const StaffDashboard = () => {
  const { 
    waitingList, 
    currentServing, 
    stats, 
    callNext, 
    completeCurrent, 
    cancelAppointment 
  } = useSimulation();

  const StatCard = ({ title, value, sub, icon: Icon, color, trend }: any) => (
    <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
      <div className="flex justify-between items-start mb-4">
        <div className={cn("p-3 rounded-lg", color)}>
          <Icon className="h-6 w-6 text-white" />
        </div>
        {trend && (
          <span className="flex items-center text-green-600 text-xs font-medium bg-green-50 px-2 py-1 rounded-full">
            <TrendingUp className="h-3 w-3 mr-1" /> {trend}
          </span>
        )}
      </div>
      <h3 className="text-3xl font-bold text-gray-900 mb-1">{value}</h3>
      <p className="text-sm text-gray-500">{title}</p>
      {sub && <p className="text-xs text-gray-400 mt-2">{sub}</p>}
    </div>
  );

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-900">Tổng quan hệ thống</h1>
        <div className="flex gap-2">
          <Button variant="outline" size="sm">Tải báo cáo</Button>
          <Button size="sm">Quản lý hàng chờ</Button>
        </div>
      </div>

      {/* Stats Row */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard 
          title="Đang chờ" 
          value={stats.waiting} 
          sub="Người đang chờ"
          icon={Users} 
          color="bg-orange-500"
        />
        <StatCard 
          title="Đang phục vụ" 
          value={currentServing ? 1 : 0} 
          sub="Quầy hoạt động"
          icon={Clock} 
          color="bg-green-500"
        />
        <StatCard 
          title="Hoàn thành hôm nay" 
          value={stats.completed} 
          sub="Lượt đã xử lý"
          icon={CheckCircle} 
          color="bg-blue-500"
        />
        <StatCard 
          title="Thời gian chờ TB" 
          value={`${stats.avgWait} phút`} 
          sub="Mục tiêu: < 20 phút"
          icon={TrendingUp} 
          color="bg-purple-500"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Main Queue Control */}
        <div className="lg:col-span-2 space-y-6">
          {/* Now Serving */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
            <div className="p-6 border-b border-gray-100 flex justify-between items-center">
              <h3 className="font-bold text-lg">Phiên hiện tại</h3>
              <span className="bg-green-100 text-green-700 px-3 py-1 rounded-full text-xs font-bold animate-pulse">TRỰC TIẾP</span>
            </div>
            
            {currentServing ? (
              <div className="p-6 grid grid-cols-1 md:grid-cols-2 gap-8">
                <div className="text-center md:text-left">
                  <div className="inline-block bg-green-50 text-green-700 text-6xl font-bold px-8 py-4 rounded-2xl mb-4">
                    {currentServing.queueNumber}
                  </div>
                  <h4 className="font-bold text-xl">{currentServing.citizenName}</h4>
                  <p className="text-gray-500">{currentServing.procedure}</p>
                  <div className="flex items-center gap-2 mt-2 text-sm text-gray-400 justify-center md:justify-start">
                    <Clock className="h-4 w-4" /> Vừa bắt đầu
                  </div>
                </div>
                <div className="flex flex-col justify-center gap-3">
                  <Button 
                    variant="success" 
                    size="lg" 
                    className="w-full justify-between group"
                    onClick={completeCurrent}
                  >
                    Hoàn thành <ArrowRight className="h-5 w-5 group-hover:translate-x-1 transition-transform" />
                  </Button>
                  <div className="grid grid-cols-2 gap-3">
                    <Button variant="outline" className="w-full border-yellow-500 text-yellow-600 hover:bg-yellow-50">Bổ sung HS</Button>
                    <Button variant="outline" className="w-full border-gray-300 text-gray-500 hover:bg-gray-50">Vắng mặt</Button>
                  </div>
                </div>
              </div>
            ) : (
              <div className="p-12 text-center text-gray-400">
                <p>Không có phiên hoạt động. Gọi số tiếp theo để bắt đầu.</p>
              </div>
            )}
          </div>

          {/* Waiting List Table */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-100">
            <div className="p-6 border-b border-gray-100 flex justify-between items-center">
              <h3 className="font-bold text-lg">Danh sách chờ</h3>
              <Button variant="ghost" size="sm" className="text-primary">Xem tất cả</Button>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full text-sm text-left">
                <thead className="bg-gray-50 text-gray-500 font-medium">
                  <tr>
                    <th className="px-6 py-4">Số phiếu</th>
                    <th className="px-6 py-4">Công dân</th>
                    <th className="px-6 py-4">Thủ tục</th>
                    <th className="px-6 py-4">Trạng thái</th>
                    <th className="px-6 py-4">Thao tác</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {waitingList.length > 0 ? waitingList.map((item) => (
                    <tr key={item.id} className="hover:bg-gray-50 transition-colors">
                      <td className="px-6 py-4 font-bold text-gray-900">{item.queueNumber}</td>
                      <td className="px-6 py-4">{item.citizenName}</td>
                      <td className="px-6 py-4">
                        <span className="bg-blue-50 text-blue-700 px-2 py-1 rounded text-xs">
                          {item.procedure}
                        </span>
                      </td>
                      <td className="px-6 py-4">
                        <span className={cn(
                          "px-2 py-1 rounded-full text-xs font-medium",
                          item.status === 'ready' ? "bg-green-100 text-green-700" : "bg-gray-100 text-gray-600"
                        )}>
                          {item.status === 'ready' ? 'SẴN SÀNG' : 'ĐANG CHỜ'}
                        </span>
                      </td>
                      <td className="px-6 py-4">
                        <div className="flex gap-2">
                          <button className="p-1.5 hover:bg-blue-50 text-blue-600 rounded"><Phone className="h-4 w-4" /></button>
                          <button 
                            onClick={() => cancelAppointment(item.id)}
                            className="p-1.5 hover:bg-red-50 text-red-600 rounded"
                          >
                            <XCircle className="h-4 w-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  )) : (
                    <tr>
                      <td colSpan={5} className="px-6 py-8 text-center text-gray-400">
                        Hàng chờ trống
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* Right Sidebar - Activity & Next */}
        <div className="space-y-6">
          {/* Next Up */}
          <div className="bg-gradient-to-br from-[#003366] to-[#004080] rounded-xl p-6 text-white shadow-lg">
            <h3 className="text-sm font-medium opacity-80 mb-4">TIẾP THEO</h3>
            {waitingList.length > 0 ? (
              <>
                <div className="flex items-center justify-between mb-6">
                  <div>
                    <h2 className="text-4xl font-bold">{waitingList[0].queueNumber}</h2>
                    <p className="mt-1 opacity-90">{waitingList[0].citizenName}</p>
                  </div>
                  <div className="h-12 w-12 bg-white/10 rounded-full flex items-center justify-center">
                    <Users className="h-6 w-6" />
                  </div>
                </div>
                <Button 
                  onClick={callNext}
                  className="w-full bg-white text-[#003366] hover:bg-gray-100 font-bold"
                >
                  Gọi số tiếp theo
                </Button>
              </>
            ) : (
              <div className="text-center py-4 opacity-50">
                Không có ai đang chờ
              </div>
            )}
          </div>

          {/* Chart */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
            <h3 className="font-bold text-gray-900 mb-4">Lưu lượng phục vụ</h3>
            <div className="h-48">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={chartData}>
                  <defs>
                    <linearGradient id="colorVisitors" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#0068FF" stopOpacity={0.1}/>
                      <stop offset="95%" stopColor="#0068FF" stopOpacity={0}/>
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E5E7EB" />
                  <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{fontSize: 12, fill: '#9CA3AF'}} />
                  <YAxis axisLine={false} tickLine={false} tick={{fontSize: 12, fill: '#9CA3AF'}} />
                  <Tooltip />
                  <Area type="monotone" dataKey="visitors" stroke="#0068FF" strokeWidth={2} fillOpacity={1} fill="url(#colorVisitors)" />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
