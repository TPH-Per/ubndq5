import React, { useState } from 'react';
import { useSimulation, FileRecord } from '../../context/SimulationContext';
import { FileText, Clock, User, MoreVertical, Search, Filter, Edit, Calendar, CheckCircle, AlertTriangle } from 'lucide-react';
import { cn } from '../../lib/utils';
import { Button } from '../../components/ui/Button';
import { motion, AnimatePresence } from 'framer-motion';
import { format, parseISO } from 'date-fns';

export const FileProcessing = () => {
  const { activeFiles, updateFile } = useSimulation();
  const [editModal, setEditModal] = useState<FileRecord | null>(null);
  const [editForm, setEditForm] = useState<{ status: string; deadline: string }>({
    status: '',
    deadline: ''
  });

  const handleEditClick = (file: FileRecord) => {
    setEditModal(file);
    setEditForm({
      status: file.status,
      deadline: file.deadline
    });
  };

  const handleSave = () => {
    if (editModal) {
      updateFile(editModal.id, {
        status: editForm.status as any,
        deadline: editForm.deadline
      });
      setEditModal(null);
    }
  };

  const StatusBadge = ({ status }: { status: string }) => {
    const styles = {
      processing: 'bg-blue-100 text-blue-700',
      completed: 'bg-green-100 text-green-700',
      need_docs: 'bg-yellow-100 text-yellow-700',
      rejected: 'bg-red-100 text-red-700',
    };
    
    const labels = {
      processing: 'Đang xử lý',
      completed: 'Hoàn thành',
      need_docs: 'Bổ sung HS',
      rejected: 'Từ chối',
    };

    return (
      <span className={cn("px-2.5 py-0.5 rounded-full text-xs font-bold whitespace-nowrap", styles[status as keyof typeof styles])}>
        {labels[status as keyof typeof labels] || status}
      </span>
    );
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Xử lý hồ sơ</h1>
          <p className="text-gray-500 text-sm">Quản lý các hồ sơ đang hoạt động và tiến độ xử lý.</p>
        </div>
        <div className="flex gap-2">
           <Button variant="outline" className="gap-2">
             <Filter className="h-4 w-4" /> Bộ lọc
           </Button>
           <Button className="gap-2">
             <FileText className="h-4 w-4" /> Tạo hồ sơ
           </Button>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
          <p className="text-gray-500 text-xs uppercase font-bold">Đang xử lý</p>
          <h3 className="text-2xl font-bold text-blue-600 mt-1">{activeFiles.filter(f => f.status === 'processing').length}</h3>
        </div>
        <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
          <p className="text-gray-500 text-xs uppercase font-bold">Cần bổ sung</p>
          <h3 className="text-2xl font-bold text-yellow-600 mt-1">{activeFiles.filter(f => f.status === 'need_docs').length}</h3>
        </div>
        <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
          <p className="text-gray-500 text-xs uppercase font-bold">Hoàn thành hôm nay</p>
          <h3 className="text-2xl font-bold text-green-600 mt-1">14</h3>
        </div>
      </div>

      {/* Files Grid */}
      <div className="grid grid-cols-1 gap-4">
        {activeFiles.length > 0 ? (
          activeFiles.map((file) => (
            <div key={file.id} className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm hover:shadow-md transition-shadow flex flex-col md:flex-row items-start md:items-center gap-4">
              <div className="h-12 w-12 bg-blue-50 rounded-lg flex items-center justify-center text-primary shrink-0">
                <FileText className="h-6 w-6" />
              </div>
              
              <div className="flex-1 min-w-0 grid grid-cols-1 md:grid-cols-4 gap-4 w-full">
                <div>
                  <p className="text-xs text-gray-500">Mã hồ sơ</p>
                  <p className="font-bold text-gray-900">{file.fileCode}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-500">Công dân</p>
                  <p className="font-medium text-gray-900">{file.citizenName}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-500">Thủ tục</p>
                  <p className="text-sm text-gray-700 truncate">{file.procedure}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-500">Hạn xử lý</p>
                  <div className="flex items-center gap-1.5 text-sm text-gray-700">
                    <Calendar className="h-3.5 w-3.5 text-gray-400" />
                    {format(parseISO(file.deadline), 'dd/MM/yyyy')}
                  </div>
                </div>
              </div>

              <div className="flex items-center gap-4 w-full md:w-auto justify-between md:justify-end border-t md:border-t-0 border-gray-100 pt-3 md:pt-0 mt-2 md:mt-0">
                <StatusBadge status={file.status} />
                <button 
                  onClick={() => handleEditClick(file)}
                  className="p-2 hover:bg-blue-50 text-gray-400 hover:text-blue-600 rounded-full transition-colors"
                  title="Chỉnh sửa"
                >
                  <Edit className="h-4 w-4" />
                </button>
              </div>
            </div>
          ))
        ) : (
          <div className="text-center py-12 bg-white rounded-xl border border-dashed border-gray-300">
            <p className="text-gray-500">Không có hồ sơ nào đang xử lý.</p>
          </div>
        )}
      </div>

      {/* Edit Modal */}
      <AnimatePresence>
        {editModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
            <motion.div 
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              className="bg-white rounded-2xl shadow-xl w-full max-w-md overflow-hidden"
            >
              <div className="p-6 border-b border-gray-100">
                <div className="flex items-center justify-between">
                  <h3 className="text-lg font-bold text-gray-900">Cập nhật hồ sơ</h3>
                  <button onClick={() => setEditModal(null)} className="text-gray-400 hover:text-gray-600">
                    <span className="text-2xl">&times;</span>
                  </button>
                </div>
                <p className="text-sm text-gray-500 mt-1">Mã HS: {editModal.fileCode}</p>
              </div>
              
              <div className="p-6 space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Trạng thái xử lý</label>
                  <select 
                    className="w-full p-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary bg-white"
                    value={editForm.status}
                    onChange={(e) => setEditForm({ ...editForm, status: e.target.value })}
                  >
                    <option value="processing">Đang xử lý</option>
                    <option value="need_docs">Cần bổ sung giấy tờ</option>
                    <option value="completed">Hoàn thành</option>
                    <option value="rejected">Từ chối / Huỷ</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Hạn xử lý (Deadline)</label>
                  <input 
                    type="date" 
                    className="w-full p-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary"
                    value={editForm.deadline}
                    onChange={(e) => setEditForm({ ...editForm, deadline: e.target.value })}
                  />
                  <p className="text-xs text-gray-500 mt-1">Thay đổi ngày hẹn trả kết quả.</p>
                </div>
              </div>
              
              <div className="bg-gray-50 p-4 flex justify-end gap-3">
                <Button variant="ghost" onClick={() => setEditModal(null)}>Huỷ bỏ</Button>
                <Button onClick={handleSave}>Lưu thay đổi</Button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
};
