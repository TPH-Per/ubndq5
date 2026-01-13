<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex justify-between items-center">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Quản lý tài khoản</h1>
        <p class="text-gray-500 mt-1">Quản lý nhân viên và phân quyền hệ thống</p>
      </div>
      <button
        @click="openCreateModal"
        class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
      >
        <Plus :size="20" />
        Thêm tài khoản
      </button>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-4">
        <div class="flex items-center gap-3">
          <div class="p-2 bg-blue-100 rounded-lg">
            <Users :size="24" class="text-blue-600" />
          </div>
          <div>
            <p class="text-sm text-gray-500">Tổng nhân viên</p>
            <p class="text-xl font-bold text-gray-900">{{ users.length }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-4">
        <div class="flex items-center gap-3">
          <div class="p-2 bg-green-100 rounded-lg">
            <UserCheck :size="24" class="text-green-600" />
          </div>
          <div>
            <p class="text-sm text-gray-500">Đang hoạt động</p>
            <p class="text-xl font-bold text-gray-900">{{ activeUsers }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-4">
        <div class="flex items-center gap-3">
          <div class="p-2 bg-purple-100 rounded-lg">
            <Shield :size="24" class="text-purple-600" />
          </div>
          <div>
            <p class="text-sm text-gray-500">Admin</p>
            <p class="text-xl font-bold text-gray-900">{{ adminCount }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-4">
        <div class="flex items-center gap-3">
          <div class="p-2 bg-orange-100 rounded-lg">
            <UserX :size="24" class="text-orange-600" />
          </div>
          <div>
            <p class="text-sm text-gray-500">Đã khóa</p>
            <p class="text-xl font-bold text-gray-900">{{ inactiveUsers }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- User Table -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <!-- Loading State -->
      <div v-if="loading" class="p-8 text-center">
        <div class="inline-block animate-spin rounded-full h-8 w-8 border-4 border-blue-600 border-t-transparent"></div>
        <p class="mt-2 text-gray-500">Đang tải...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="p-8 text-center">
        <AlertCircle :size="48" class="mx-auto text-red-500 mb-3" />
        <p class="text-red-600">{{ error }}</p>
        <button @click="fetchUsers" class="mt-3 text-blue-600 hover:underline">Thử lại</button>
      </div>

      <!-- Table -->
      <table v-else class="w-full">
        <thead class="bg-gray-50 border-b border-gray-100">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Mã NV</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Họ tên</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Vai trò</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Quầy</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Trạng thái</th>
            <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Thao tác</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-for="user in users" :key="user.id" class="hover:bg-gray-50 transition-colors">
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="font-mono text-sm text-gray-900">{{ user.maNhanVien }}</span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="flex items-center gap-3">
                <div class="h-8 w-8 rounded-full bg-blue-100 flex items-center justify-center">
                  <span class="text-blue-600 font-medium text-sm">{{ getInitials(user.hoTen) }}</span>
                </div>
                <span class="text-gray-900 font-medium">{{ user.hoTen }}</span>
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-gray-600">{{ user.email }}</td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span
                :class="[
                  'px-2 py-1 text-xs font-medium rounded-full',
                  user.roleName === 'Admin' ? 'bg-purple-100 text-purple-700' : 'bg-blue-100 text-blue-700'
                ]"
              >
                {{ user.roleDisplayName }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-gray-600">
              {{ user.tenQuay || '-' }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span
                :class="[
                  'px-2 py-1 text-xs font-medium rounded-full',
                  user.trangThai ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
                ]"
              >
                {{ user.trangThai ? 'Hoạt động' : 'Đã khóa' }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right">
              <div class="flex items-center justify-end gap-2">
                <button
                  @click="editUser(user)"
                  class="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded transition-colors"
                  title="Sửa"
                >
                  <Pencil :size="16" />
                </button>
                <button
                  @click="toggleUserStatus(user)"
                  class="p-1.5 text-gray-400 hover:text-orange-600 hover:bg-orange-50 rounded transition-colors"
                  :title="user.trangThai ? 'Khóa' : 'Mở khóa'"
                >
                  <Lock v-if="user.trangThai" :size="16" />
                  <Unlock v-else :size="16" />
                </button>
              </div>
            </td>
          </tr>
          
          <!-- Empty State -->
          <tr v-if="users.length === 0">
            <td colspan="7" class="px-6 py-12 text-center text-gray-500">
              Chưa có tài khoản nào
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Create User Modal -->
    <CreateUserModal
      v-if="showCreateModal"
      @close="showCreateModal = false"
      @created="onUserCreated"
    />

    <!-- Edit User Modal (Admin editing another user) -->
    <EditProfileModal
      v-if="showEditModal"
      :editing-user="editingUser"
      @close="showEditModal = false; editingUser = null"
      @updated="onUserUpdated"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { 
  Plus, Users, UserCheck, UserX, Shield, 
  Pencil, Lock, Unlock, AlertCircle 
} from 'lucide-vue-next';
import { userApi, type UserData } from '@/services/api';
import CreateUserModal from '@/components/admin/CreateUserModal.vue';
import EditProfileModal from '@/components/shared/EditProfileModal.vue';

// State
const users = ref<UserData[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);
const showCreateModal = ref(false);
const showEditModal = ref(false);
const editingUser = ref<UserData | null>(null);

// Computed
const activeUsers = computed(() => users.value.filter(u => u.trangThai).length);
const inactiveUsers = computed(() => users.value.filter(u => !u.trangThai).length);
const adminCount = computed(() => users.value.filter(u => u.roleName === 'Admin').length);

// Methods
function getInitials(name: string): string {
  return name
    .split(' ')
    .map(w => w[0])
    .join('')
    .substring(0, 2)
    .toUpperCase();
}

async function fetchUsers() {
  loading.value = true;
  error.value = null;
  
  try {
    const response = await userApi.getAll();
    if (response.data.success) {
      users.value = response.data.data;
    } else {
      error.value = response.data.message;
    }
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Lỗi kết nối server';
  } finally {
    loading.value = false;
  }
}

function openCreateModal() {
  showCreateModal.value = true;
}

function onUserCreated(newUser: UserData) {
  users.value.unshift(newUser);
  showCreateModal.value = false;
}

function editUser(user: UserData) {
  editingUser.value = user;
  showEditModal.value = true;
}

function onUserUpdated(updatedUser: UserData) {
  // Update user in list
  const index = users.value.findIndex(u => u.id === updatedUser.id);
  if (index !== -1) {
    users.value[index] = updatedUser;
  }
  showEditModal.value = false;
  editingUser.value = null;
}

async function toggleUserStatus(user: UserData) {
  if (!confirm(`Bạn có chắc muốn ${user.trangThai ? 'khóa' : 'mở khóa'} tài khoản ${user.hoTen}?`)) {
    return;
  }
  
  try {
    if (user.trangThai) {
      await userApi.delete(user.id);
    } else {
      await userApi.update(user.id, { trangThai: true });
    }
    user.trangThai = !user.trangThai;
  } catch (err: any) {
    alert(err.response?.data?.message || 'Có lỗi xảy ra');
  }
}

// Lifecycle
onMounted(() => {
  fetchUsers();
});
</script>
