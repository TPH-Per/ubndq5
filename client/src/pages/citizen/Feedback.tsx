import React, { useState } from 'react';
import { useLocation } from 'react-router-dom';
import { Send, Clock, CheckCircle2, AlertCircle, MessageSquare, FileText, Star } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { cn } from '../../lib/utils';
import { motion, AnimatePresence } from 'framer-motion';

import * as api from '../../services/citizenApi';
import { useSimulation } from '../../context/SimulationContext';

const TYPE_MAP: Record<string, number> = {
  service: 1,
  procedure: 1,
  facility: 1,
  system: 2,
  other: 1
};

const StarRating = ({ rating, onRate, size = 'lg' }: { rating: number; onRate: (r: number) => void; size?: 'sm' | 'lg' }) => {
  const [hovered, setHovered] = useState(0);
  const labels = ['', 'Rất tệ', 'Tệ', 'Bình thường', 'Tốt', 'Rất tốt'];
  const colors = ['', 'text-red-400', 'text-orange-400', 'text-yellow-400', 'text-lime-500', 'text-green-500'];
  const active = hovered || rating;

  return (
    <div className="flex flex-col items-center gap-2">
      <div className="flex gap-1">
        {[1, 2, 3, 4, 5].map((star) => (
          <button
            key={star}
            type="button"
            onMouseEnter={() => setHovered(star)}
            onMouseLeave={() => setHovered(0)}
            onClick={() => onRate(star)}
            className={cn(
              "transition-all duration-200 ease-out",
              size === 'lg' ? 'p-1' : 'p-0.5',
              star <= active ? 'scale-110' : 'scale-100 hover:scale-110'
            )}
          >
            <Star
              className={cn(
                "transition-colors duration-200",
                size === 'lg' ? 'h-8 w-8' : 'h-4 w-4',
                star <= active
                  ? `${colors[active]} fill-current`
                  : 'text-gray-300'
              )}
            />
          </button>
        ))}
      </div>
      {active > 0 && (
        <span className={cn(
          "text-xs font-bold transition-colors",
          colors[active]
        )}>
          {labels[active]}
        </span>
      )}
    </div>
  );
};

export const Feedback = () => {
  const { citizenId, citizenName, zaloId } = useSimulation();
  const location = useLocation();
  const locationState = (location.state as { applicationId?: number; procedureName?: string; fromAppointment?: boolean } | null);

  const [activeTab, setActiveTab] = useState<'send' | 'history'>('send');
  const [form, setForm] = useState({ title: '', content: '', category: 'service', rating: 0 });
  const linkedApplicationId = locationState?.applicationId;
  const linkedProcedureName = locationState?.procedureName;
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [feedbacks, setFeedbacks] = useState<any[]>([]);
  const [isLoadingHistory, setIsLoadingHistory] = useState(false);

  // Fetch history when tab changes to 'history'
  React.useEffect(() => {
    if (activeTab === 'history' && citizenId) {
      const fetchHistory = async () => {
        setIsLoadingHistory(true);
        try {
          const data = await api.getMyFeedbacks(citizenId, zaloId ?? undefined);
          setFeedbacks(data);
        } catch (error) {
          console.error("Error fetching feedback:", error);
        } finally {
          setIsLoadingHistory(false);
        }
      };
      fetchHistory();
    }
  }, [activeTab, citizenId]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!citizenId) return;

    if (form.rating === 0) {
      alert('Vui lòng chọn số sao đánh giá');
      return;
    }

    setIsSubmitting(true);
    try {
      await api.submitFeedback({
        type: TYPE_MAP[form.category] || 1,
        title: form.title,
        content: form.content || undefined,
        citizenCccd: citizenId,
        citizenName: citizenName,
        applicationId: linkedApplicationId,
        zaloId: zaloId,
        rating: form.rating,
      });

      setIsSubmitted(true);
      setForm({ title: '', content: '', category: 'service', rating: 0 });
      setTimeout(() => setIsSubmitted(false), 3000);
    } catch (error) {
      console.error("Error submitting feedback:", error);
      alert("Không thể gửi góp ý lúc này. Vui lòng thử lại sau.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const StatusBadge = ({ status }: { status: string | number }) => {
    const isReplied = status === 'RESOLVED' || status === 2;
    return isReplied ? (
      <span className="flex items-center gap-1 text-[10px] font-bold bg-green-100 text-green-700 px-2 py-1 rounded-full">
        <CheckCircle2 className="h-3 w-3" /> Đã trả lời
      </span>
    ) : (
      <span className="flex items-center gap-1 text-[10px] font-bold bg-yellow-100 text-yellow-700 px-2 py-1 rounded-full">
        <Clock className="h-3 w-3" /> Đang xử lý
      </span>
    );
  };

  const MiniStars = ({ count }: { count: number }) => (
    <div className="flex gap-0.5">
      {[1, 2, 3, 4, 5].map((s) => (
        <Star
          key={s}
          className={cn(
            "h-3 w-3",
            s <= count ? "text-yellow-400 fill-current" : "text-gray-300"
          )}
        />
      ))}
    </div>
  );

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
            <Send className="h-4 w-4" /> Gửi đánh giá
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
              {linkedApplicationId && (
                <div className="bg-blue-50 border border-blue-200 rounded-xl p-3 flex items-center gap-3">
                  <FileText className="h-5 w-5 text-blue-600 shrink-0" />
                  <div>
                    <p className="text-xs font-bold text-blue-800">Gửi đánh giá cho hồ sơ</p>
                    <p className="text-xs text-blue-700">{linkedProcedureName}</p>
                  </div>
                </div>
              )}
              {isSubmitted ? (
                <div className="bg-green-50 border border-green-200 rounded-2xl p-8 text-center">
                  <div className="h-16 w-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                    <CheckCircle2 className="h-8 w-8 text-green-600" />
                  </div>
                  <h3 className="text-lg font-bold text-green-800">Gửi đánh giá thành công!</h3>
                  <p className="text-sm text-green-600 mt-2">
                    Cảm ơn bạn đã đóng góp ý kiến. Chúng tôi sẽ phản hồi sớm nhất có thể.
                  </p>
                  <Button
                    variant="outline"
                    className="mt-6 border-green-200 text-green-700 hover:bg-green-100"
                    onClick={() => setIsSubmitted(false)}
                  >
                    Gửi đánh giá khác
                  </Button>
                </div>
              ) : (
                <form onSubmit={handleSubmit} className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 space-y-5">
                  {/* Star Rating */}
                  <div className="text-center">
                    <label className="block text-sm font-medium text-gray-700 mb-3">Đánh giá của bạn <span className="text-red-500">*</span></label>
                    <StarRating
                      rating={form.rating}
                      onRate={(r) => setForm({ ...form, rating: r })}
                    />
                  </div>

                  <div className="h-px bg-gray-100"></div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Chủ đề</label>
                    <select
                      className="w-full p-3 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all"
                      value={form.category}
                      onChange={(e) => setForm({ ...form, category: e.target.value })}
                    >
                      <option value="service">Thái độ phục vụ</option>
                      <option value="procedure">Quy trình thủ tục</option>
                      <option value="facility">Cơ sở vật chất</option>
                      <option value="system">Lỗi hệ thống / Ứng dụng</option>
                      <option value="other">Khác</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Tiêu đề <span className="text-gray-400 text-xs font-normal">(không bắt buộc)</span>
                    </label>
                    <input
                      type="text"
                      placeholder="Nhập tiêu đề ngắn gọn..."
                      value={form.title}
                      onChange={(e) => setForm({ ...form, title: e.target.value })}
                      className="w-full p-3 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Nội dung chi tiết <span className="text-gray-400 text-xs font-normal">(không bắt buộc)</span>
                    </label>
                    <textarea
                      rows={4}
                      placeholder="Mô tả chi tiết vấn đề hoặc ý kiến của bạn..."
                      value={form.content}
                      onChange={(e) => setForm({ ...form, content: e.target.value })}
                      className="w-full p-3 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all resize-none"
                    />
                  </div>

                  <div className="pt-2">
                    <Button type="submit" fullWidth size="lg" className="shadow-lg shadow-blue-200" disabled={isSubmitting}>
                      {isSubmitting ? 'Đang gửi...' : 'Gửi đánh giá'}
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
              {isLoadingHistory ? (
                <div className="text-center py-8 text-gray-400">Đang tải lịch sử góp ý...</div>
              ) : feedbacks.length > 0 ? (
                feedbacks.map((item) => (
                  <div key={item.id} className="bg-white p-4 rounded-xl shadow-sm border border-gray-100">
                    <div className="flex justify-between items-start mb-2">
                      <h4 className="font-bold text-gray-900 text-sm line-clamp-1 flex-1 mr-2">{item.title}</h4>
                      <StatusBadge status={item.status} />
                    </div>

                    <div className="flex items-center gap-3 mb-2">
                      {item.rating && <MiniStars count={item.rating} />}
                      {item.applicationCode && (
                        <div className="flex items-center gap-1.5">
                          <FileText className="h-3 w-3 text-gray-400" />
                          <span className="text-[10px] text-gray-500">Hồ sơ: {item.applicationCode}</span>
                        </div>
                      )}
                    </div>

                    {item.content && (
                      <p className="text-xs text-gray-500 mb-3 line-clamp-2">{item.content}</p>
                    )}

                    {item.replies && item.replies.length > 0 && (
                      <div className="space-y-2 mb-3">
                        {item.replies.map((reply: api.FeedbackReply) => {
                          const rd = new Date(reply.createdAt)
                          const rdate = `${rd.getDate().toString().padStart(2, '0')}/${(rd.getMonth() + 1).toString().padStart(2, '0')}/${rd.getFullYear()}`
                          return (
                            <div key={reply.id} className="bg-blue-50 p-3 rounded-lg border-l-2 border-primary">
                              <p className="text-[10px] text-primary font-bold mb-1">
                                <MessageSquare className="h-3 w-3 inline mr-1" />
                                {reply.staffName} &mdash; {rdate}
                              </p>
                              <p className="text-xs text-gray-700">{reply.content}</p>
                            </div>
                          )
                        })}
                      </div>
                    )}

                    <div className="flex justify-between items-center pt-2 border-t border-gray-50">
                      <span className="text-[10px] text-gray-400">
                        {(() => {
                          const id = new Date(item.createdAt);
                          return `${id.getDate().toString().padStart(2, '0')}/${(id.getMonth() + 1).toString().padStart(2, '0')}/${id.getFullYear()}`;
                        })()}
                      </span>
                    </div>
                  </div>
                ))
              ) : (
                <div className="text-center py-8 text-gray-400">Bạn chưa có lịch sử đánh giá nào.</div>
              )}
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
};
