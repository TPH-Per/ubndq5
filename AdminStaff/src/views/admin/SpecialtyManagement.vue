<template>
  <div class="space-y-8 p-6 animate-fade-in-up">
    <!-- Header -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h1 class="text-3xl font-bold bg-gradient-to-r from-blue-700 to-indigo-700 bg-clip-text text-transparent">
          Quản lý chuyên môn
        </h1>
        <p class="text-gray-500 mt-2 font-medium">Hệ thống quản lý các lĩnh vực chuyên môn và thủ tục hành chính</p>
      </div>
      <button
        @click="openCreateModal"
        class="group flex items-center gap-2 px-5 py-2.5 bg-gradient-to-r from-blue-600 to-indigo-600 text-white rounded-xl shadow-lg shadow-blue-500/30 hover:shadow-blue-500/40 hover:-translate-y-0.5 transition-all duration-300 font-medium"
      >
        <Plus :size="20" class="group-hover:rotate-90 transition-transform duration-300" />
        Thêm chuyên môn
      </button>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
      <div class="bg-white rounded-2xl p-5 border border-gray-100 shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all duration-300 group">
        <div class="flex items-center gap-4">
          <div class="p-3 bg-blue-50 text-blue-600 rounded-xl group-hover:scale-110 transition-transform duration-300">
            <Layers :size="24" />
          </div>
          <div>
            <p class="text-sm font-medium text-gray-500">Tổng chuyên môn</p>
            <p class="text-2xl font-bold text-gray-800 mt-1">{{ chuyenMons.length }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-2xl p-5 border border-gray-100 shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all duration-300 group">
        <div class="flex items-center gap-4">
          <div class="p-3 bg-green-50 text-green-600 rounded-xl group-hover:scale-110 transition-transform duration-300">
            <CheckCircle :size="24" />
          </div>
          <div>
            <p class="text-sm font-medium text-gray-500">Đang hoạt động</p>
            <p class="text-2xl font-bold text-gray-800 mt-1">{{ activeChuyenMons }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-2xl p-5 border border-gray-100 shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all duration-300 group">
        <div class="flex items-center gap-4">
          <div class="p-3 bg-purple-50 text-purple-600 rounded-xl group-hover:scale-110 transition-transform duration-300">
            <Building2 :size="24" />
          </div>
          <div>
            <p class="text-sm font-medium text-gray-500">Tổng quầy</p>
            <p class="text-2xl font-bold text-gray-800 mt-1">{{ totalQuays }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-2xl p-5 border border-gray-100 shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all duration-300 group">
        <div class="flex items-center gap-4">
          <div class="p-3 bg-orange-50 text-orange-600 rounded-xl group-hover:scale-110 transition-transform duration-300">
            <FileText :size="24" />
          </div>
          <div>
            <p class="text-sm font-medium text-gray-500">Tổng thủ tục</p>
            <p class="text-2xl font-bold text-gray-800 mt-1">{{ totalThuTucs }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- ChuyenMon Table -->
    <div class="bg-white rounded-2xl shadow-xl shadow-gray-200/50 border border-gray-100 overflow-hidden">
      <!-- Loading State -->
      <div v-if="loading" class="p-12 text-center">
        <div class="inline-block animate-spin rounded-full h-10 w-10 border-4 border-blue-600 border-t-transparent"></div>
        <p class="mt-4 text-gray-500 font-medium">Đang tải dữ liệu...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="p-12 center">
        <div class="w-16 h-16 bg-red-50 rounded-full flex items-center justify-center mx-auto mb-4">
          <AlertCircle :size="32" class="text-red-500" />
        </div>
        <p class="text-red-800 font-medium text-lg mb-2">Đã xảy ra lỗi</p>
        <p class="text-gray-500 mb-6">{{ error }}</p>
        <button 
          @click="fetchChuyenMons" 
          class="px-4 py-2 bg-white border border-gray-300 rounded-lg text-gray-700 font-medium hover:bg-gray-50 transition-colors"
        >
          Thử lại
        </button>
      </div>

      <!-- Table -->
      <div v-else class="overflow-x-auto">
        <table class="w-full">
          <thead class="bg-gray-50/50 border-b border-gray-100">
            <tr>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Mã</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Tên chuyên môn</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Mô tả</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Thống kê</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Trạng thái</th>
              <th class="px-6 py-4 text-right text-xs font-semibold text-gray-500 uppercase tracking-wider">Thao tác</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-50">
            <tr v-for="cm in chuyenMons" :key="cm.id" class="hover:bg-blue-50/30 transition-colors duration-200 group">
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="font-mono text-sm font-semibold text-blue-600 bg-blue-50 px-2 py-1 rounded-md">
                  {{ cm.maChuyenMon }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="text-gray-900 font-medium">{{ cm.tenChuyenMon }}</span>
              </td>
              <td class="px-6 py-4">
                <span class="text-gray-500 text-sm line-clamp-2 max-w-xs">{{ cm.moTa || '-' }}</span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex gap-2">
                  <span class="px-2.5 py-1 text-xs font-medium rounded-full bg-purple-50 text-purple-700 border border-purple-100">
                    {{ cm.soQuay }} quầy
                  </span>
                  <span class="px-2.5 py-1 text-xs font-medium rounded-full bg-orange-50 text-orange-700 border border-orange-100">
                    {{ cm.soThuTuc }} thủ tục
                  </span>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span
                  :class="[
                    'px-3 py-1 text-xs font-medium rounded-full inline-flex items-center gap-1.5',
                    cm.trangThai 
                      ? 'bg-green-50 text-green-700 border border-green-200' 
                      : 'bg-red-50 text-red-700 border border-red-200'
                  ]"
                >
                  <span class="w-1.5 h-1.5 rounded-full" :class="cm.trangThai ? 'bg-green-500' : 'bg-red-500'"></span>
                  {{ cm.trangThai ? 'Hoạt động' : 'Đã khóa' }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-right">
                <div class="flex items-center justify-end gap-2 opacity-100 sm:opacity-0 sm:group-hover:opacity-100 transition-opacity">
                  <button
                    @click="editChuyenMon(cm)"
                    class="p-2 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-all duration-200"
                    title="Chỉnh sửa"
                  >
                    <Pencil :size="18" />
                  </button>
                  <button
                    @click="toggleChuyenMonStatus(cm)"
                    :class="[
                      'p-2 rounded-lg transition-all duration-200',
                      cm.trangThai 
                        ? 'text-gray-400 hover:text-red-600 hover:bg-red-50' 
                        : 'text-gray-400 hover:text-green-600 hover:bg-green-50'
                    ]"
                    :title="cm.trangThai ? 'Khóa chuyên môn' : 'Mở khóa chuyên môn'"
                  >
                    <Lock v-if="cm.trangThai" :size="18" />
                    <Unlock v-else :size="18" />
                  </button>
                </div>
              </td>
            </tr>
            
            <!-- Empty State -->
            <tr v-if="chuyenMons.length === 0">
              <td colspan="6" class="px-6 py-24 text-center">
                <div class="w-24 h-24 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4">
                  <Layers :size="48" class="text-gray-300" />
                </div>
                <p class="text-gray-500 font-medium text-lg">Chưa có chuyên môn nào</p>
                <p class="text-gray-400 text-sm mt-1">Bắt đầu bằng cách thêm chuyên môn mới</p>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
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
import { useToast } from "vue-toastification";
import Swal from 'sweetalert2';

// State
const chuyenMons = ref<ChuyenMonData[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);
const showCreateModal = ref(false);
const showEditModal = ref(false);
const editingChuyenMon = ref<ChuyenMonData | null>(null);
const toast = useToast();

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
      toast.error(error.value || 'Lỗi tải dữ liệu');
    }
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Lỗi kết nối server';
    toast.error(error.value || 'Lỗi kết nối server');
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
  toast.success('Thêm chuyên môn thành công');
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
  toast.success('Cập nhật chuyên môn thành công');
}

async function toggleChuyenMonStatus(cm: ChuyenMonData) {
  const result = await Swal.fire({
    title: 'Xác nhận',
    text: `Bạn có chắc muốn ${cm.trangThai ? 'khóa' : 'mở khóa'} chuyên môn ${cm.tenChuyenMon}?`,
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#3085d6',
    cancelButtonColor: '#d33',
    confirmButtonText: 'Đồng ý',
    cancelButtonText: 'Hủy'
  });

  if (!result.isConfirmed) return;
  
  try {
    if (cm.trangThai) {
      await chuyenMonApi.delete(cm.id);
    } else {
      await chuyenMonApi.update(cm.id, { trangThai: true });
    }
    cm.trangThai = !cm.trangThai;
    toast.success(`Đã ${cm.trangThai ? 'mở khóa' : 'khóa'} chuyên môn ${cm.tenChuyenMon}`);
  } catch (err: any) {
    toast.error(err.response?.data?.message || 'Có lỗi xảy ra');
  }
}

// Lifecycle
onMounted(() => {
  fetchChuyenMons();
});
</script>
