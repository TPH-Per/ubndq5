<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex justify-between items-center">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Quản lý quầy</h1>
        <p class="text-gray-500 mt-1">Quản lý các quầy tiếp nhận hồ sơ</p>
      </div>
      <button
        @click="openCreateModal"
        class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
      >
        <Plus :size="20" />
        Thêm quầy
      </button>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-4">
        <div class="flex items-center gap-3">
          <div class="p-2 bg-blue-100 rounded-lg">
            <Building2 :size="24" class="text-blue-600" />
          </div>
          <div>
            <p class="text-sm text-gray-500">Tổng quầy</p>
            <p class="text-xl font-bold text-gray-900">{{ quays.length }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-4">
        <div class="flex items-center gap-3">
          <div class="p-2 bg-green-100 rounded-lg">
            <CheckCircle :size="24" class="text-green-600" />
          </div>
          <div>
            <p class="text-sm text-gray-500">Đang hoạt động</p>
            <p class="text-xl font-bold text-gray-900">{{ activeQuays }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-4">
        <div class="flex items-center gap-3">
          <div class="p-2 bg-orange-100 rounded-lg">
            <XCircle :size="24" class="text-orange-600" />
          </div>
          <div>
            <p class="text-sm text-gray-500">Đã khóa</p>
            <p class="text-xl font-bold text-gray-900">{{ inactiveQuays }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-4">
        <div class="flex items-center gap-3">
          <div class="p-2 bg-purple-100 rounded-lg">
            <Users :size="24" class="text-purple-600" />
          </div>
          <div>
            <p class="text-sm text-gray-500">Tổng nhân viên</p>
            <p class="text-xl font-bold text-gray-900">{{ totalStaff }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Quay Table -->
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
        <button @click="fetchQuays" class="mt-3 text-blue-600 hover:underline">Thử lại</button>
      </div>

      <!-- Table -->
      <table v-else class="w-full">
        <thead class="bg-gray-50 border-b border-gray-100">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Mã quầy</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tên quầy</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Chuyên môn</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Vị trí</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nhân viên</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Trạng thái</th>
            <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Thao tác</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-for="quay in quays" :key="quay.id" class="hover:bg-gray-50 transition-colors">
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="font-mono text-sm font-medium text-gray-900">{{ quay.maQuay }}</span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="flex items-center gap-3">
                <div class="h-8 w-8 rounded-full bg-blue-100 flex items-center justify-center">
                  <span class="text-blue-600 font-medium text-sm">{{ quay.prefixSo }}</span>
                </div>
                <span class="text-gray-900 font-medium">{{ quay.tenQuay }}</span>
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="px-2 py-1 text-xs font-medium rounded-full bg-purple-100 text-purple-700">
                {{ quay.tenChuyenMon }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-gray-600">
              {{ quay.viTri || '-' }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="text-gray-900 font-medium">{{ quay.soNhanVien }}</span>
              <span class="text-gray-500 text-sm"> người</span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span
                :class="[
                  'px-2 py-1 text-xs font-medium rounded-full',
                  quay.trangThai ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
                ]"
              >
                {{ quay.trangThai ? 'Hoạt động' : 'Đã khóa' }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right">
              <div class="flex items-center justify-end gap-2">
                <button
                  @click="editQuay(quay)"
                  class="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded transition-colors"
                  title="Sửa"
                >
                  <Pencil :size="16" />
                </button>
                <button
                  @click="toggleQuayStatus(quay)"
                  class="p-1.5 text-gray-400 hover:text-orange-600 hover:bg-orange-50 rounded transition-colors"
                  :title="quay.trangThai ? 'Khóa' : 'Mở khóa'"
                >
                  <Lock v-if="quay.trangThai" :size="16" />
                  <Unlock v-else :size="16" />
                </button>
              </div>
            </td>
          </tr>
          
          <!-- Empty State -->
          <tr v-if="quays.length === 0">
            <td colspan="7" class="px-6 py-12 text-center text-gray-500">
              Chưa có quầy nào
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Create Quay Modal -->
    <CreateQuayModal
      v-if="showCreateModal"
      @close="showCreateModal = false"
      @created="onQuayCreated"
    />

    <!-- Edit Quay Modal -->
    <EditQuayModal
      v-if="showEditModal"
      :quay="editingQuay"
      @close="showEditModal = false; editingQuay = null"
      @updated="onQuayUpdated"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { 
  Plus, Building2, CheckCircle, XCircle, Users,
  Pencil, Lock, Unlock, AlertCircle 
} from 'lucide-vue-next';
import { quayApi, type QuayData } from '@/services/api';
import CreateQuayModal from '@/components/admin/CreateQuayModal.vue';
import EditQuayModal from '@/components/admin/EditQuayModal.vue';

// State
const quays = ref<QuayData[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);
const showCreateModal = ref(false);
const showEditModal = ref(false);
const editingQuay = ref<QuayData | null>(null);

// Computed
const activeQuays = computed(() => quays.value.filter(q => q.trangThai).length);
const inactiveQuays = computed(() => quays.value.filter(q => !q.trangThai).length);
const totalStaff = computed(() => quays.value.reduce((sum, q) => sum + (q.soNhanVien || 0), 0));

// Methods
async function fetchQuays() {
  loading.value = true;
  error.value = null;
  
  try {
    const response = await quayApi.getAll();
    if (response.data.success) {
      quays.value = response.data.data;
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

function onQuayCreated(newQuay: QuayData) {
  quays.value.unshift(newQuay);
  showCreateModal.value = false;
}

function editQuay(quay: QuayData) {
  editingQuay.value = quay;
  showEditModal.value = true;
}

function onQuayUpdated(updatedQuay: QuayData) {
  const index = quays.value.findIndex(q => q.id === updatedQuay.id);
  if (index !== -1) {
    quays.value[index] = updatedQuay;
  }
  showEditModal.value = false;
  editingQuay.value = null;
}

async function toggleQuayStatus(quay: QuayData) {
  if (!confirm(`Bạn có chắc muốn ${quay.trangThai ? 'khóa' : 'mở khóa'} quầy ${quay.tenQuay}?`)) {
    return;
  }
  
  try {
    if (quay.trangThai) {
      await quayApi.delete(quay.id);
    } else {
      await quayApi.update(quay.id, { trangThai: true });
    }
    quay.trangThai = !quay.trangThai;
  } catch (err: any) {
    alert(err.response?.data?.message || 'Có lỗi xảy ra');
  }
}

// Lifecycle
onMounted(() => {
  fetchQuays();
});
</script>
