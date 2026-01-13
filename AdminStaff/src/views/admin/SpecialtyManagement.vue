<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex justify-between items-center">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Quản lý chuyên môn</h1>
        <p class="text-gray-500 mt-1">Quản lý các lĩnh vực chuyên môn và thủ tục</p>
      </div>
      <button
        @click="openCreateModal"
        class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
      >
        <Plus :size="20" />
        Thêm chuyên môn
      </button>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-4">
        <div class="flex items-center gap-3">
          <div class="p-2 bg-blue-100 rounded-lg">
            <Layers :size="24" class="text-blue-600" />
          </div>
          <div>
            <p class="text-sm text-gray-500">Tổng chuyên môn</p>
            <p class="text-xl font-bold text-gray-900">{{ chuyenMons.length }}</p>
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
            <p class="text-xl font-bold text-gray-900">{{ activeChuyenMons }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-4">
        <div class="flex items-center gap-3">
          <div class="p-2 bg-purple-100 rounded-lg">
            <Building2 :size="24" class="text-purple-600" />
          </div>
          <div>
            <p class="text-sm text-gray-500">Tổng quầy</p>
            <p class="text-xl font-bold text-gray-900">{{ totalQuays }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-4">
        <div class="flex items-center gap-3">
          <div class="p-2 bg-orange-100 rounded-lg">
            <FileText :size="24" class="text-orange-600" />
          </div>
          <div>
            <p class="text-sm text-gray-500">Tổng thủ tục</p>
            <p class="text-xl font-bold text-gray-900">{{ totalThuTucs }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- ChuyenMon Table -->
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
        <button @click="fetchChuyenMons" class="mt-3 text-blue-600 hover:underline">Thử lại</button>
      </div>

      <!-- Table -->
      <table v-else class="w-full">
        <thead class="bg-gray-50 border-b border-gray-100">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Mã</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tên chuyên môn</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Mô tả</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Số quầy</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Số thủ tục</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Trạng thái</th>
            <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Thao tác</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-for="cm in chuyenMons" :key="cm.id" class="hover:bg-gray-50 transition-colors">
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="font-mono text-sm font-medium text-gray-900">{{ cm.maChuyenMon }}</span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="text-gray-900 font-medium">{{ cm.tenChuyenMon }}</span>
            </td>
            <td class="px-6 py-4">
              <span class="text-gray-600 text-sm line-clamp-2">{{ cm.moTa || '-' }}</span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="px-2 py-1 text-xs font-medium rounded-full bg-purple-100 text-purple-700">
                {{ cm.soQuay }} quầy
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="px-2 py-1 text-xs font-medium rounded-full bg-orange-100 text-orange-700">
                {{ cm.soThuTuc }} thủ tục
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span
                :class="[
                  'px-2 py-1 text-xs font-medium rounded-full',
                  cm.trangThai ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
                ]"
              >
                {{ cm.trangThai ? 'Hoạt động' : 'Đã khóa' }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right">
              <div class="flex items-center justify-end gap-2">
                <button
                  @click="editChuyenMon(cm)"
                  class="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded transition-colors"
                  title="Sửa"
                >
                  <Pencil :size="16" />
                </button>
                <button
                  @click="toggleChuyenMonStatus(cm)"
                  class="p-1.5 text-gray-400 hover:text-orange-600 hover:bg-orange-50 rounded transition-colors"
                  :title="cm.trangThai ? 'Khóa' : 'Mở khóa'"
                >
                  <Lock v-if="cm.trangThai" :size="16" />
                  <Unlock v-else :size="16" />
                </button>
              </div>
            </td>
          </tr>
          
          <!-- Empty State -->
          <tr v-if="chuyenMons.length === 0">
            <td colspan="7" class="px-6 py-12 text-center text-gray-500">
              Chưa có chuyên môn nào
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Create Modal -->
    <CreateChuyenMonModal
      v-if="showCreateModal"
      @close="showCreateModal = false"
      @created="onChuyenMonCreated"
    />

    <!-- Edit Modal -->
    <EditChuyenMonModal
      v-if="showEditModal"
      :chuyen-mon="editingChuyenMon"
      @close="showEditModal = false; editingChuyenMon = null"
      @updated="onChuyenMonUpdated"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { 
  Plus, Layers, CheckCircle, Building2, FileText,
  Pencil, Lock, Unlock, AlertCircle 
} from 'lucide-vue-next';
import { chuyenMonApi, type ChuyenMonData } from '@/services/api';
import CreateChuyenMonModal from '@/components/admin/CreateChuyenMonModal.vue';
import EditChuyenMonModal from '@/components/admin/EditChuyenMonModal.vue';

// State
const chuyenMons = ref<ChuyenMonData[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);
const showCreateModal = ref(false);
const showEditModal = ref(false);
const editingChuyenMon = ref<ChuyenMonData | null>(null);

// Computed
const activeChuyenMons = computed(() => chuyenMons.value.filter(cm => cm.trangThai).length);
const totalQuays = computed(() => chuyenMons.value.reduce((sum, cm) => sum + (cm.soQuay || 0), 0));
const totalThuTucs = computed(() => chuyenMons.value.reduce((sum, cm) => sum + (cm.soThuTuc || 0), 0));

// Methods
async function fetchChuyenMons() {
  loading.value = true;
  error.value = null;
  
  try {
    const response = await chuyenMonApi.getAll();
    if (response.data.success) {
      chuyenMons.value = response.data.data;
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

function onChuyenMonCreated(newChuyenMon: ChuyenMonData) {
  chuyenMons.value.unshift(newChuyenMon);
  showCreateModal.value = false;
}

function editChuyenMon(cm: ChuyenMonData) {
  editingChuyenMon.value = cm;
  showEditModal.value = true;
}

function onChuyenMonUpdated(updatedChuyenMon: ChuyenMonData) {
  const index = chuyenMons.value.findIndex(cm => cm.id === updatedChuyenMon.id);
  if (index !== -1) {
    chuyenMons.value[index] = updatedChuyenMon;
  }
  showEditModal.value = false;
  editingChuyenMon.value = null;
}

async function toggleChuyenMonStatus(cm: ChuyenMonData) {
  if (!confirm(`Bạn có chắc muốn ${cm.trangThai ? 'khóa' : 'mở khóa'} chuyên môn ${cm.tenChuyenMon}?`)) {
    return;
  }
  
  try {
    if (cm.trangThai) {
      await chuyenMonApi.delete(cm.id);
    } else {
      await chuyenMonApi.update(cm.id, { trangThai: true });
    }
    cm.trangThai = !cm.trangThai;
  } catch (err: any) {
    alert(err.response?.data?.message || 'Có lỗi xảy ra');
  }
}

// Lifecycle
onMounted(() => {
  fetchChuyenMons();
});
</script>
