import React, { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { MapPin, Phone, Clock, FileText, ChevronDown, ChevronUp, Info } from 'lucide-react';
import * as api from '../../services/citizenApi';
import { cn } from '../../lib/utils';

interface FaqItem {
  question: string;
  answer: string;
}

const FAQ_ITEMS: FaqItem[] = [
  {
    question: 'Tôi có thể đặt lịch hẹn trước bao lâu?',
    answer: 'Bạn có thể đặt lịch hẹn trước ít nhất 2 tiếng so với giờ hẹn. Hệ thống tự động ẩn các khung giờ đã qua hoặc quá gần.',
  },
  {
    question: 'Tôi có thể hủy lịch hẹn không?',
    answer: 'Có. Bạn có thể hủy lịch hẹn trước giờ hẹn ít nhất 2 tiếng. Sau thời gian này, hệ thống sẽ tự động đánh dấu "Không đến" và bạn cần đặt lại lịch mới.',
  },
  {
    question: 'Mỗi lần được đặt bao nhiêu lịch hẹn?',
    answer: 'Mỗi tài khoản Zalo chỉ được có 1 lịch hẹn đang chờ xử lý tại một thời điểm. Vui lòng hoàn thành hoặc hủy lịch cũ trước khi đặt mới.',
  },
  {
    question: 'Tôi cần mang theo giấy tờ gì?',
    answer: 'Tùy theo thủ tục bạn đăng ký, hệ thống sẽ hiển thị danh sách giấy tờ bắt buộc ở bước cuối cùng khi đặt lịch. Vui lòng mang theo bản chính và bản photo khi đến làm việc.',
  },
];

export const Guide = () => {
  const [procedures, setProcedures] = useState<{ name: string; requiredDocuments: string[] }[]>([]);
  const [expandedFaq, setExpandedFaq] = useState<number | null>(null);

  useEffect(() => {
    const fetchProcedures = async () => {
      try {
        const specialties = await api.getSpecialties();
        const allProcedures: { name: string; requiredDocuments: string[] }[] = [];
        for (const spec of specialties) {
          const procs = await api.getProcedures(spec.id);
          for (const p of procs) {
            if (p.requiredDocuments && p.requiredDocuments.length > 0) {
              allProcedures.push({ name: p.name, requiredDocuments: p.requiredDocuments });
            }
          }
        }
        setProcedures(allProcedures);
      } catch (err) {
        console.error('Failed to fetch procedures for guide:', err);
      }
    };
    fetchProcedures();
  }, []);

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      className="p-4 space-y-4 pb-8"
    >
      {/* Office Info */}
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-5 space-y-4">
        <h3 className="font-bold text-gray-900 text-base flex items-center gap-2">
          <Info className="h-5 w-5 text-primary" />
          Thông tin liên hệ
        </h3>
        <div className="space-y-3 text-sm">
          <div className="flex items-start gap-3">
            <MapPin className="h-4 w-4 text-gray-400 mt-0.5 shrink-0" />
            <span className="text-gray-700">UBND Phường Chợ Lớn, Quận 5, TP. Hồ Chí Minh</span>
          </div>
          <div className="flex items-start gap-3">
            <Phone className="h-4 w-4 text-gray-400 mt-0.5 shrink-0" />
            <span className="text-gray-700">(028) 3855 1234</span>
          </div>
          <div className="flex items-start gap-3">
            <Clock className="h-4 w-4 text-gray-400 mt-0.5 shrink-0" />
            <div className="text-gray-700">
              <p>Thứ 2 - Thứ 6: 7:30 - 11:30, 13:30 - 17:00</p>
              <p className="text-xs text-gray-500 mt-1">Nghỉ thứ 7, Chủ nhật và các ngày lễ</p>
            </div>
          </div>
        </div>
      </div>

      {/* Required Documents per Procedure */}
      {procedures.length > 0 && (
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-5 space-y-3">
          <h3 className="font-bold text-gray-900 text-base flex items-center gap-2">
            <FileText className="h-5 w-5 text-primary" />
            Giấy tờ theo thủ tục
          </h3>
          <div className="space-y-3">
            {procedures.map((proc, idx) => (
              <div key={idx} className="bg-gray-50 rounded-xl p-3">
                <p className="font-semibold text-sm text-gray-800 mb-2">{proc.name}</p>
                <ul className="space-y-1">
                  {proc.requiredDocuments.map((doc, i) => (
                    <li key={i} className="flex items-start gap-2 text-xs text-gray-600">
                      <span className="mt-0.5 h-3 w-3 rounded border border-gray-300 shrink-0" />
                      {doc}
                    </li>
                  ))}
                </ul>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* FAQ */}
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-5 space-y-3">
        <h3 className="font-bold text-gray-900 text-base">Câu hỏi thường gặp</h3>
        <div className="space-y-2">
          {FAQ_ITEMS.map((item, idx) => {
            const isOpen = expandedFaq === idx;
            return (
              <div key={idx} className="border border-gray-100 rounded-xl overflow-hidden">
                <button
                  onClick={() => setExpandedFaq(isOpen ? null : idx)}
                  className="w-full flex items-center justify-between p-3 text-left"
                >
                  <span className="text-sm font-medium text-gray-800 pr-2">{item.question}</span>
                  {isOpen ? (
                    <ChevronUp className="h-4 w-4 text-gray-400 shrink-0" />
                  ) : (
                    <ChevronDown className="h-4 w-4 text-gray-400 shrink-0" />
                  )}
                </button>
                {isOpen && (
                  <motion.div
                    initial={{ height: 0, opacity: 0 }}
                    animate={{ height: 'auto', opacity: 1 }}
                    className="px-3 pb-3"
                  >
                    <p className="text-xs text-gray-600 leading-relaxed">{item.answer}</p>
                  </motion.div>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </motion.div>
  );
};
