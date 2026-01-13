import React, { useState } from 'react';
import { Plus, Building2, MapPin, Edit2, Trash2, X, AlertCircle } from 'lucide-react';
import { Button } from '../../components/ui/Button';

// Utility to generate unique IDs
const generateId = () => Math.random().toString(36).substr(2, 9);

interface Service {
    id: string;
    name: string;
}

interface Counter {
    id: string;
    name: string;
    location: string;
    services: Service[];
    status: 'Active' | 'Inactive';
    officerName?: string;
}

export const CounterManagement = () => {
    // --- State Management ---
    const [counters, setCounters] = useState<Counter[]>([
        { id: '1', name: 'Quầy A', location: 'Tầng 1 (Cửa chính)', services: [{ id: 's1', name: 'Công thương' }, { id: 's2', name: 'Xây dựng' }], status: 'Active', officerName: 'Nguyễn Văn A' },
        { id: '2', name: 'Quầy B', location: 'Tầng 1 (Bên phải)', services: [{ id: 's3', name: 'Đất đai' }, { id: 's4', name: 'Sao y' }], status: 'Active' },
        { id: '3', name: 'Quầy C', location: 'Tầng 2', services: [{ id: 's5', name: 'Tư pháp' }, { id: 's6', name: 'Hộ tịch' }], status: 'Active' },
    ]);

    const [isModalOpen, setIsModalOpen] = useState(false);
    const [confirmDeleteId, setConfirmDeleteId] = useState<string | null>(null);

    // Initial form state for Add/Edit
    const initialFormState: Counter = {
        id: '',
        name: '',
        location: '',
        services: [],
        status: 'Active',
        officerName: ''
    };
    const [formData, setFormData] = useState<Counter>(initialFormState);
    const [isEditing, setIsEditing] = useState(false);

    // --- Actions ---

    const handleAddNew = () => {
        setFormData({ ...initialFormState, id: generateId() });
        setIsEditing(false);
        setIsModalOpen(true);
    };

    const handleEdit = (counter: Counter) => {
        setFormData({ ...counter }); // Clone object to avoid direct mutation
        setIsEditing(true);
        setIsModalOpen(true);
    };

    const handleDelete = (id: string) => {
        setCounters(prev => prev.filter(c => c.id !== id));
        setConfirmDeleteId(null);
    };

    const handleSave = (e: React.FormEvent) => {
        e.preventDefault();

        if (!formData.name || !formData.location) {
            alert("Vui lòng nhập tên quầy và vị trí!");
            return;
        }

        if (isEditing) {
            setCounters(prev => prev.map(c => c.id === formData.id ? formData : c));
        } else {
            setCounters(prev => [...prev, formData]);
        }
        setIsModalOpen(false);
    };

    // Service Management inside Modal
    const [newServiceName, setNewServiceName] = useState('');
    const handleAddService = () => {
        if (!newServiceName.trim()) return;
        const newService = { id: generateId(), name: newServiceName };
        setFormData(prev => ({ ...prev, services: [...prev.services, newService] }));
        setNewServiceName('');
    };

    const handleRemoveService = (serviceId: string) => {
        setFormData(prev => ({
            ...prev,
            services: prev.services.filter(s => s.id !== serviceId)
        }));
    };

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex justify-between items-center bg-white p-6 rounded-xl shadow-sm border border-gray-100">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Quản lý Quầy & Dịch vụ</h1>
                    <p className="text-sm text-gray-500 mt-1">Cấu hình các điểm tiếp nhận hồ sơ và dịch vụ tương ứng</p>
                </div>
                <Button onClick={handleAddNew} className="gap-2 bg-indigo-600 hover:bg-indigo-700 shadow-lg shadow-indigo-200">
                    <Plus className="h-4 w-4" /> Thêm quầy mới
                </Button>
            </div>

            {/* List */}
            {counters.length === 0 ? (
                <div className="text-center py-20 bg-gray-50 rounded-xl border-2 border-dashed border-gray-200">
                    <Building2 className="h-12 w-12 text-gray-300 mx-auto mb-4" />
                    <p className="text-gray-500 font-medium">Chưa có quầy nào được tạo.</p>
                    <p className="text-sm text-gray-400">Nhấn "Thêm quầy mới" để bắt đầu.</p>
                </div>
            ) : (
                <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
                    {counters.map((counter) => (
                        <div key={counter.id} className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden group hover:shadow-lg transition-all duration-300">
                            {/* Card Header */}
                            <div className="p-6 border-b border-gray-100 bg-gradient-to-br from-white to-gray-50">
                                <div className="flex justify-between items-start mb-4">
                                    <div className="h-12 w-12 bg-indigo-50 text-indigo-600 rounded-xl flex items-center justify-center shadow-inner">
                                        <Building2 className="h-6 w-6" />
                                    </div>
                                    <div className="flex gap-1 opacity-100 lg:opacity-0 lg:group-hover:opacity-100 transition-opacity">
                                        <button
                                            onClick={() => handleEdit(counter)}
                                            className="p-2 text-gray-400 hover:text-indigo-600 rounded-lg hover:bg-white hover:shadow-sm transition-all"
                                            title="Chỉnh sửa"
                                        >
                                            <Edit2 className="h-4 w-4" />
                                        </button>
                                        <button
                                            onClick={() => setConfirmDeleteId(counter.id)}
                                            className="p-2 text-gray-400 hover:text-red-600 rounded-lg hover:bg-white hover:shadow-sm transition-all"
                                            title="Xóa"
                                        >
                                            <Trash2 className="h-4 w-4" />
                                        </button>
                                    </div>
                                </div>

                                <h3 className="text-xl font-bold text-gray-900 mb-1">{counter.name}</h3>
                                <div className="space-y-1">
                                    <div className="flex items-center text-sm text-gray-500">
                                        <MapPin className="h-4 w-4 mr-1.5 text-gray-400" /> {counter.location}
                                    </div>
                                    {counter.officerName && (
                                        <p className="text-xs text-indigo-600 font-medium bg-indigo-50 px-2 py-1 rounded inline-block">
                                            Cán bộ: {counter.officerName}
                                        </p>
                                    )}
                                </div>
                            </div>

                            {/* Services List */}
                            <div className="p-6">
                                <h4 className="text-xs font-bold text-gray-400 mb-3 uppercase tracking-wider flex justify-between items-center">
                                    Dịch vụ ({counter.services.length})
                                </h4>
                                <div className="flex flex-wrap gap-2">
                                    {counter.services.length > 0 ? counter.services.map((service) => (
                                        <span key={service.id} className="bg-gray-50 border border-gray-200 text-gray-600 px-3 py-1.5 rounded-lg text-xs font-medium">
                                            {service.name}
                                        </span>
                                    )) : (
                                        <span className="text-xs text-gray-400 italic">Chưa có dịch vụ gán.</span>
                                    )}
                                </div>
                            </div>

                            {/* Footer Status */}
                            <div className={`h-1.5 w-full ${counter.status === 'Active' ? 'bg-green-500' : 'bg-gray-300'}`} />
                        </div>
                    ))}
                </div>
            )}

            {/* Modal Form */}
            {isModalOpen && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4 animate-in fade-in duration-200">
                    <div className="bg-white rounded-2xl w-full max-w-lg shadow-2xl overflow-hidden animate-in zoom-in-95 duration-200">
                        <div className="px-6 py-4 border-b border-gray-100 flex justify-between items-center bg-gray-50">
                            <h3 className="font-bold text-lg text-gray-900">
                                {isEditing ? 'Chỉnh sửa Quầy' : 'Thêm Quầy Mới'}
                            </h3>
                            <button onClick={() => setIsModalOpen(false)} className="text-gray-400 hover:text-gray-600 transition-colors">
                                <X className="h-5 w-5" />
                            </button>
                        </div>

                        <form onSubmit={handleSave} className="p-6 space-y-5">
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">Tên quầy <span className="text-red-500">*</span></label>
                                    <input
                                        type="text"
                                        value={formData.name}
                                        onChange={e => setFormData({ ...formData, name: e.target.value })}
                                        className="w-full p-2.5 bg-white border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 block"
                                        placeholder="Ví dụ: Quầy A"
                                        required
                                    />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">Trạng thái</label>
                                    <select
                                        value={formData.status}
                                        onChange={e => setFormData({ ...formData, status: e.target.value as any })}
                                        className="w-full p-2.5 bg-white border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 block"
                                    >
                                        <option value="Active">Đang hoạt động</option>
                                        <option value="Inactive">Tạm đóng</option>
                                    </select>
                                </div>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Vị trí <span className="text-red-500">*</span></label>
                                <input
                                    type="text"
                                    value={formData.location}
                                    onChange={e => setFormData({ ...formData, location: e.target.value })}
                                    className="w-full p-2.5 bg-white border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 block"
                                    placeholder="Ví dụ: Tầng 1 - Cửa chính"
                                    required
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Cán bộ phụ trách (Tùy chọn)</label>
                                <input
                                    type="text"
                                    value={formData.officerName || ''}
                                    onChange={e => setFormData({ ...formData, officerName: e.target.value })}
                                    className="w-full p-2.5 bg-white border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 block"
                                    placeholder="Nhập tên cán bộ..."
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Dịch vụ cung cấp</label>
                                <div className="bg-gray-50 p-4 rounded-xl border border-gray-200 space-y-3">
                                    <div className="flex gap-2">
                                        <input
                                            type="text"
                                            value={newServiceName}
                                            onChange={e => setNewServiceName(e.target.value)}
                                            onKeyDown={e => e.key === 'Enter' && (e.preventDefault(), handleAddService())}
                                            className="flex-1 p-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:border-indigo-500"
                                            placeholder="Tên dịch vụ mới..."
                                        />
                                        <button
                                            type="button"
                                            onClick={handleAddService}
                                            className="px-3 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 transition-colors"
                                        >
                                            Thêm
                                        </button>
                                    </div>

                                    <div className="flex flex-wrap gap-2 max-h-32 overflow-y-auto">
                                        {formData.services.map(s => (
                                            <span key={s.id} className="inline-flex items-center gap-1 bg-white border border-gray-200 px-2 pl-3 py-1 rounded-full text-xs text-gray-700 shadow-sm">
                                                {s.name}
                                                <button
                                                    type="button"
                                                    onClick={() => handleRemoveService(s.id)}
                                                    className="p-0.5 hover:bg-red-50 hover:text-red-500 rounded-full transition-colors ml-1"
                                                >
                                                    <X className="h-3 w-3" />
                                                </button>
                                            </span>
                                        ))}
                                    </div>
                                </div>
                            </div>

                            <div className="pt-4 flex justify-end gap-3 border-t border-gray-100">
                                <Button
                                    type="button"
                                    variant="outline"
                                    onClick={() => setIsModalOpen(false)}
                                >
                                    Hủy bỏ
                                </Button>
                                <Button className="bg-indigo-600 hover:bg-indigo-700">
                                    {isEditing ? 'Lưu thay đổi' : 'Tạo mới'}
                                </Button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Confirm Delete Dialog */}
            {confirmDeleteId && (
                <div className="fixed inset-0 z-[60] flex items-center justify-center bg-black/50 backdrop-blur-sm p-4 animate-in fade-in duration-200">
                    <div className="bg-white rounded-xl p-6 w-full max-w-sm shadow-2xl animate-in zoom-in-95 duration-200">
                        <div className="flex flex-col items-center text-center">
                            <div className="h-12 w-12 bg-red-100 rounded-full flex items-center justify-center mb-4">
                                <AlertCircle className="h-6 w-6 text-red-600" />
                            </div>
                            <h3 className="text-lg font-bold text-gray-900 mb-2">Xác nhận xóa</h3>
                            <p className="text-gray-500 text-sm mb-6">
                                Bạn có chắc chắn muốn xóa quầy này không? Hành động này không thể hoàn tác.
                            </p>
                            <div className="flex gap-3 w-full">
                                <Button
                                    fullWidth
                                    variant="outline"
                                    onClick={() => setConfirmDeleteId(null)}
                                >
                                    Hủy
                                </Button>
                                <Button
                                    fullWidth
                                    className="bg-red-600 hover:bg-red-700 text-white border-none"
                                    onClick={() => handleDelete(confirmDeleteId)}
                                >
                                    Xóa ngay
                                </Button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};
