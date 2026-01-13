import React, { useState } from 'react';
import { Plus, Search, Edit2, Trash2, Shield, X, Lock, Unlock, AlertCircle } from 'lucide-react';
import { Button } from '../../components/ui/Button';

// Types
interface Account {
    id: number;
    name: string;
    username: string;
    role: 'Quản trị viên' | 'Cán bộ quầy';
    department: string;
    status: 'Active' | 'Locked';
    email: string;
}

export const AccountManagement = () => {
    // --- State ---
    const [accounts, setAccounts] = useState<Account[]>([
        { id: 1, name: 'Nguyễn Văn Bộ', username: 'canbo_a', role: 'Cán bộ quầy', department: 'Bộ phận Tiếp nhận', status: 'Active', email: 'nva@example.com' },
        { id: 2, name: 'Trần Thị Mai', username: 'canbo_b', role: 'Cán bộ quầy', department: 'Bộ phận Trả kết quả', status: 'Active', email: 'ttm@example.com' },
        { id: 3, name: 'Lê Văn Quản', username: 'admin', role: 'Quản trị viên', department: 'Ban Quản lý', status: 'Active', email: 'admin@example.com' },
    ]);

    const [searchTerm, setSearchTerm] = useState('');
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [confirmDeleteId, setConfirmDeleteId] = useState<number | null>(null);

    // Form State
    const initialFormState: Account = {
        id: 0,
        name: '',
        username: '',
        role: 'Cán bộ quầy',
        department: '',
        status: 'Active',
        email: ''
    };
    const [formData, setFormData] = useState<Account>(initialFormState);
    const [isEditing, setIsEditing] = useState(false);

    // --- Actions ---

    const handleAddNew = () => {
        setFormData({ ...initialFormState, id: Date.now() }); // Simple ID gen
        setIsEditing(false);
        setIsModalOpen(true);
    };

    const handleEdit = (account: Account) => {
        setFormData({ ...account });
        setIsEditing(true);
        setIsModalOpen(true);
    };

    const handleDelete = (id: number) => {
        setAccounts(prev => prev.filter(acc => acc.id !== id));
        setConfirmDeleteId(null);
    };

    const handleSave = (e: React.FormEvent) => {
        e.preventDefault();
        if (!formData.name || !formData.username) {
            alert("Vui lòng điền đầy đủ thông tin!");
            return;
        }

        if (isEditing) {
            setAccounts(prev => prev.map(acc => acc.id === formData.id ? formData : acc));
        } else {
            setAccounts(prev => [...prev, formData]);
        }
        setIsModalOpen(false);
    };

    const toggleStatus = (id: number) => {
        setAccounts(prev => prev.map(acc => {
            if (acc.id === id) {
                return { ...acc, status: acc.status === 'Active' ? 'Locked' : 'Active' };
            }
            return acc;
        }));
    };

    // Filter Logic
    const filteredAccounts = accounts.filter(acc =>
        acc.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        acc.username.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <div className="space-y-6">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Quản lý tài khoản</h1>
                    <p className="text-sm text-gray-500">Quản lý danh sách người dùng và phân quyền</p>
                </div>
                <Button onClick={handleAddNew} className="gap-2 bg-indigo-600 hover:bg-indigo-700 shadow-lg shadow-indigo-200">
                    <Plus className="h-4 w-4" /> Thêm tài khoản
                </Button>
            </div>

            {/* Filters */}
            <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex flex-col md:flex-row gap-4">
                <div className="relative flex-1 max-w-md">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
                    <input
                        type="text"
                        placeholder="Tìm kiếm theo tên, tài khoản..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20 transition-all"
                    />
                </div>
                <div className="flex gap-2">
                    <select className="px-4 py-2 border border-gray-200 rounded-lg text-sm bg-white focus:outline-none focus:border-indigo-500">
                        <option>Tất cả vai trò</option>
                        <option>Quản trị viên</option>
                        <option>Cán bộ quầy</option>
                    </select>
                    <select className="px-4 py-2 border border-gray-200 rounded-lg text-sm bg-white focus:outline-none focus:border-indigo-500">
                        <option>Tất cả trạng thái</option>
                        <option>Hoạt động</option>
                        <option>Đã khóa</option>
                    </select>
                </div>
            </div>

            {/* Table */}
            <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="w-full text-sm text-left">
                        <thead className="bg-gray-50 text-gray-500 font-medium border-b border-gray-100">
                            <tr>
                                <th className="px-6 py-4">Họ và tên</th>
                                <th className="px-6 py-4">Thông tin đăng nhập</th>
                                <th className="px-6 py-4">Vai trò</th>
                                <th className="px-6 py-4">Phòng ban</th>
                                <th className="px-6 py-4">Trạng thái</th>
                                <th className="px-6 py-4 text-right">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-100">
                            {filteredAccounts.length > 0 ? filteredAccounts.map((user) => (
                                <tr key={user.id} className="hover:bg-gray-50 transition-colors group">
                                    <td className="px-6 py-4">
                                        <p className="font-bold text-gray-900">{user.name}</p>
                                        <p className="text-xs text-gray-400">{user.email}</p>
                                    </td>
                                    <td className="px-6 py-4 font-mono text-gray-600">{user.username}</td>
                                    <td className="px-6 py-4">
                                        <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium ${user.role === 'Quản trị viên' ? 'bg-purple-100 text-purple-700' : 'bg-blue-50 text-blue-700'
                                            }`}>
                                            {user.role === 'Quản trị viên' && <Shield className="h-3 w-3" />}
                                            {user.role}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 text-gray-500">{user.department}</td>
                                    <td className="px-6 py-4">
                                        <button
                                            onClick={() => toggleStatus(user.id)}
                                            className={`flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium border transition-all ${user.status === 'Active'
                                                ? 'bg-green-50 text-green-700 border-green-200 hover:bg-green-100'
                                                : 'bg-gray-50 text-gray-500 border-gray-200 hover:bg-gray-100'
                                                }`}
                                        >
                                            {user.status === 'Active' ? <Unlock className="h-3 w-3" /> : <Lock className="h-3 w-3" />}
                                            {user.status === 'Active' ? 'Hoạt động' : 'Đã khóa'}
                                        </button>
                                    </td>
                                    <td className="px-6 py-4 text-right">
                                        <div className="flex items-center justify-end gap-2 opacity-100 lg:opacity-0 lg:group-hover:opacity-100 transition-opacity">
                                            <button
                                                onClick={() => handleEdit(user)}
                                                className="p-2 text-gray-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors"
                                                title="Chỉnh sửa"
                                            >
                                                <Edit2 className="h-4 w-4" />
                                            </button>
                                            <button
                                                onClick={() => setConfirmDeleteId(user.id)}
                                                className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                                                title="Xóa tài khoản"
                                            >
                                                <Trash2 className="h-4 w-4" />
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            )) : (
                                <tr>
                                    <td colSpan={6} className="px-6 py-12 text-center text-gray-400">
                                        Không tìm thấy tài khoản nào phù hợp.
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Modal Form */}
            {isModalOpen && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4 animate-in fade-in duration-200">
                    <div className="bg-white rounded-2xl w-full max-w-lg shadow-2xl overflow-hidden animate-in zoom-in-95 duration-200">
                        <div className="px-6 py-4 border-b border-gray-100 flex justify-between items-center bg-gray-50">
                            <h3 className="font-bold text-lg text-gray-900">
                                {isEditing ? 'Chỉnh sửa tài khoản' : 'Thêm tài khoản mới'}
                            </h3>
                            <button onClick={() => setIsModalOpen(false)} className="text-gray-400 hover:text-gray-600 transition-colors">
                                <X className="h-5 w-5" />
                            </button>
                        </div>

                        <form onSubmit={handleSave} className="p-6 space-y-5">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Họ và tên <span className="text-red-500">*</span></label>
                                <input
                                    type="text"
                                    value={formData.name}
                                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                    required
                                    className="w-full p-2.5 bg-white border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 block"
                                    placeholder="Nhập họ và tên..."
                                />
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">Tên đăng nhập <span className="text-red-500">*</span></label>
                                    <input
                                        type="text"
                                        value={formData.username}
                                        onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                                        required
                                        className="w-full p-2.5 bg-white border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 block"
                                        placeholder="username"
                                    />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                                    <input
                                        type="email"
                                        value={formData.email}
                                        onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                                        className="w-full p-2.5 bg-white border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 block"
                                        placeholder="email@example.com"
                                    />
                                </div>
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">Vai trò</label>
                                    <select
                                        value={formData.role}
                                        onChange={(e) => setFormData({ ...formData, role: e.target.value as any })}
                                        className="w-full p-2.5 bg-white border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 block"
                                    >
                                        <option>Cán bộ quầy</option>
                                        <option>Quản trị viên</option>
                                    </select>
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">Phòng ban</label>
                                    <input
                                        type="text"
                                        value={formData.department}
                                        onChange={(e) => setFormData({ ...formData, department: e.target.value })}
                                        className="w-full p-2.5 bg-white border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 block"
                                        placeholder="Nhập phòng ban..."
                                    />
                                </div>
                            </div>

                            {!isEditing && (
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">Mật khẩu khởi tạo</label>
                                    <input
                                        type="password"
                                        disabled
                                        value="MacDinh@123"
                                        className="w-full p-2.5 bg-gray-100 border border-gray-300 rounded-lg text-sm text-gray-500"
                                    />
                                    <p className="text-xs text-gray-400 mt-1">Mật khẩu mặc định sẽ là MacDinh@123</p>
                                </div>
                            )}

                            <div className="flex justify-end gap-3 pt-4 border-t border-gray-100">
                                <Button type="button" variant="outline" onClick={() => setIsModalOpen(false)}>Hủy</Button>
                                <Button className="bg-indigo-600 hover:bg-indigo-700">Lưu thông tin</Button>
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
                            <h3 className="text-lg font-bold text-gray-900 mb-2">Xóa tài khoản?</h3>
                            <p className="text-gray-500 text-sm mb-6">
                                Hành động này sẽ xóa vĩnh viễn tài khoản người dùng khỏi hệ thống.
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
