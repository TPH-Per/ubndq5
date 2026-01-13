import React, { useState } from 'react';
import { Send, MessageSquare, Clock, CheckCircle2, AlertCircle, ChevronRight } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { cn } from '../../lib/utils';
import { motion, AnimatePresence } from 'framer-motion';

// Mock Data for History
const MOCK_FEEDBACK_HISTORY = [
  {
    id: 1,
    title: 'Thái độ phục vụ tại quầy A',
    content: 'Nhân viên tiếp nhận hồ sơ rất nhiệt tình và hướng dẫn chi tiết.',
    date: '20/12/2025',
    status: 'replied', // replied, pending
    reply: 'Cảm ơn bạn đã dành lời khen cho đội ngũ cán bộ. Chúng tôi sẽ tiếp tục phát huy.'
  },
  {
    id: 2,
    title: 'Hệ thống lấy số bị lỗi',
    content: 'Tôi không thể lấy số thứ tự trên app vào sáng nay.',
    date: '15/12/2025',
    status: 'pending',
    reply: null
  },
  {
    id: 3,
    title: 'Góp ý về cơ sở vật chất',
    content: 'Khu vực chờ cần thêm quạt mát.',
    date: '10/11/2025',
    status: 'replied',
    reply: 'Đã ghi nhận và sẽ bổ sung trong tuần tới.'
  }
];

export const Feedback = () => {
  const [activeTab, setActiveTab] = useState<'send' | 'history'>('send');
  const [form, setForm] = useState({ title: '', content: '', category: 'service' });
  const [isSubmitted, setIsSubmitted] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // Simulate API call
    setTimeout(() => {
      setIsSubmitted(true);
      setForm({ title: '', content: '', category: 'service' });
      // Reset success message after 3 seconds
      setTimeout(() => setIsSubmitted(false), 3000);
    }, 1000);
  };

  const StatusBadge = ({ status }: { status: string }) => {
    return status === 'replied' ? (
      <span className="flex items-center gap-1 text-[10px] font-bold bg-green-100 text-green-700 px-2 py-1 rounded-full">
        <CheckCircle2 className="h-3 w-3" /> Đã trả lời
      </span>
    ) : (
      <span className="flex items-center gap-1 text-[10px] font-bold bg-yellow-100 text-yellow-700 px-2 py-1 rounded-full">
        <Clock className="h-3 w-3" /> Đang xử lý
      </span>
    );
  };

  return (
    <div className="min-h-full bg-gray-50 flex flex-col">
      {/* Tabs */}
      <div className="bg-white p-2 sticky top-0 z-10 shadow-sm">
        <div className="flex bg-gray-100 p-1 rounded-xl">
          <button
            onClick={() => setActiveTab('send')}
            className={cn(
              "flex-1 py-2.5 text-sm font-medium rounded-lg transition-all flex items-center justify-center gap-2",
              activeTab === 'send' ? "bg-white text-primary shadow-sm" : "text-gray-500 hover:text-gray-700"
            )}
          >
            <Send className="h-4 w-4" /> Gửi góp ý
          </button>
          <button
            onClick={() => setActiveTab('history')}
            className={cn(
              "flex-1 py-2.5 text-sm font-medium rounded-lg transition-all flex items-center justify-center gap-2",
              activeTab === 'history' ? "bg-white text-primary shadow-sm" : "text-gray-500 hover:text-gray-700"
            )}
          >
            <Clock className="h-4 w-4" /> Lịch sử
          </button>
        </div>
      </div>

      <div className="p-4 flex-1">
        <AnimatePresence mode="wait">
          {activeTab === 'send' ? (
            <motion.div
              key="send"
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: 20 }}
              className="space-y-6"
            >
              {isSubmitted ? (
                <div className="bg-green-50 border border-green-200 rounded-2xl p-8 text-center">
                  <div className="h-16 w-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                    <CheckCircle2 className="h-8 w-8 text-green-600" />
                  </div>
                  <h3 className="text-lg font-bold text-green-800">Gửi góp ý thành công!</h3>
                  <p className="text-sm text-green-600 mt-2">
                    Cảm ơn bạn đã đóng góp ý kiến. Chúng tôi sẽ phản hồi sớm nhất có thể.
                  </p>
                  <Button 
                    variant="outline" 
                    className="mt-6 border-green-200 text-green-700 hover:bg-green-100"
                    onClick={() => setIsSubmitted(false)}
                  >
                    Gửi góp ý khác
                  </Button>
                </div>
              ) : (
                <form onSubmit={handleSubmit} className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Chủ đề góp ý</label>
                    <select 
                      className="w-full p-3 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all"
                      value={form.category}
                      onChange={(e) => setForm({...form, category: e.target.value})}
                    >
                      <option value="service">Thái độ phục vụ</option>
                      <option value="procedure">Quy trình thủ tục</option>
                      <option value="facility">Cơ sở vật chất</option>
                      <option value="system">Lỗi hệ thống / Ứng dụng</option>
                      <option value="other">Khác</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Tiêu đề</label>
                    <input 
                      type="text" 
                      placeholder="Nhập tiêu đề ngắn gọn..."
                      required
                      value={form.title}
                      onChange={(e) => setForm({...form, title: e.target.value})}
                      className="w-full p-3 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Nội dung chi tiết</label>
                    <textarea 
                      rows={5}
                      placeholder="Mô tả chi tiết vấn đề hoặc ý kiến của bạn..."
                      required
                      value={form.content}
                      onChange={(e) => setForm({...form, content: e.target.value})}
                      className="w-full p-3 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all resize-none"
                    />
                  </div>

                  <div className="pt-2">
                    <Button type="submit" fullWidth size="lg" className="shadow-lg shadow-blue-200">
                      Gửi phản ánh
                    </Button>
                  </div>
                </form>
              )}

              <div className="bg-blue-50 p-4 rounded-xl flex gap-3 items-start">
                <AlertCircle className="h-5 w-5 text-blue-600 shrink-0 mt-0.5" />
                <div className="text-xs text-blue-800">
                  <p className="font-bold mb-1">Lưu ý:</p>
                  <p>Mọi ý kiến đóng góp của công dân đều được bảo mật và là cơ sở để chúng tôi nâng cao chất lượng phục vụ.</p>
                </div>
              </div>
            </motion.div>
          ) : (
            <motion.div
              key="history"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -20 }}
              className="space-y-4"
            >
              {MOCK_FEEDBACK_HISTORY.map((item) => (
                <div key={item.id} className="bg-white p-4 rounded-xl shadow-sm border border-gray-100">
                  <div className="flex justify-between items-start mb-2">
                    <h4 className="font-bold text-gray-900 text-sm line-clamp-1 flex-1 mr-2">{item.title}</h4>
                    <StatusBadge status={item.status} />
                  </div>
                  <p className="text-xs text-gray-500 mb-3 line-clamp-2">{item.content}</p>
                  
                  {item.reply && (
                    <div className="bg-gray-50 p-3 rounded-lg mb-3 border-l-2 border-primary">
                      <p className="text-[10px] text-gray-400 font-bold uppercase mb-1">Phản hồi từ ban quản lý</p>
                      <p className="text-xs text-gray-700">{item.reply}</p>
                    </div>
                  )}

                  <div className="flex justify-between items-center pt-2 border-t border-gray-50">
                    <span className="text-[10px] text-gray-400">{item.date}</span>
                    <button className="text-xs font-medium text-primary flex items-center">
                      Xem chi tiết <ChevronRight className="h-3 w-3 ml-0.5" />
                    </button>
                  </div>
                </div>
              ))}
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
};
