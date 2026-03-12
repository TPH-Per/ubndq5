<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Quản lý thủ tục</h1>
        <p class="text-sm text-gray-500 mt-1">
          Chỉ hiển thị thủ tục thuộc chuyên môn quầy của bạn
        </p>
      </div>
      <div class="flex items-center gap-3">
        <span v-if="chuyenMonName" class="inline-flex items-center gap-2 px-3 py-1.5 bg-blue-50 text-blue-700 rounded-full text-xs font-bold border border-blue-200">
          <Layers class="h-3.5 w-3.5" />
          {{ chuyenMonName }}
        </span>
        <button
          @click="fetchProcedures"
          class="inline-flex items-center gap-2 px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
        >
          <RefreshCw class="h-4 w-4" :class="{ 'animate-spin': loading }" />
          Tải lại
        </button>
        <button
          @click="openCreateModal"
          class="inline-flex items-center gap-2 px-4 py-2.5 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 shadow-sm transition-colors"
        >
          <Plus class="h-4 w-4" />
          Thêm thủ tục
        </button>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <div class="bg-white rounded-xl border border-gray-200 p-5 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="p-2.5 bg-blue-50 rounded-lg">
            <ClipboardList class="h-5 w-5 text-blue-600" />
          </div>
          <div>
            <p class="text-xs text-gray-500 font-medium">Tổng thủ tục</p>
            <p class="text-2xl font-bold text-gray-800 mt-1">{{ procedures.length }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-xl border border-gray-200 p-5 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="p-2.5 bg-green-50 rounded-lg">
            <CheckCircle class="h-5 w-5 text-green-600" />
          </div>
          <div>
            <p class="text-xs text-gray-500 font-medium">Đang hoạt động</p>
            <p class="text-2xl font-bold text-gray-800 mt-1">{{ activeProcedures }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-xl border border-gray-200 p-5 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="p-2.5 bg-amber-50 rounded-lg">
            <FileText class="h-5 w-5 text-amber-600" />
          </div>
          <div>
            <p class="text-xs text-gray-500 font-medium">Tổng hồ sơ</p>
            <p class="text-2xl font-bold text-gray-800 mt-1">{{ totalHoSo }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Procedures Table -->
    <div class="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead class="bg-gray-50 border-b border-gray-200">
            <tr>
              <th class="text-left px-5 py-3.5 text-xs font-bold text-gray-500 uppercase tracking-wider">Mã</th>
              <th class="text-left px-5 py-3.5 text-xs font-bold text-gray-500 uppercase tracking-wider">Tên thủ tục</th>
              <th class="text-left px-5 py-3.5 text-xs font-bold text-gray-500 uppercase tracking-wider">Thời gian XL</th>
              <th class="text-left px-5 py-3.5 text-xs font-bold text-gray-500 uppercase tracking-wider">Giấy tờ yêu cầu</th>
              <th class="text-left px-5 py-3.5 text-xs font-bold text-gray-500 uppercase tracking-wider">Hồ sơ</th>
              <th class="text-left px-5 py-3.5 text-xs font-bold text-gray-500 uppercase tracking-wider">Trạng thái</th>
              <th class="text-right px-5 py-3.5 text-xs font-bold text-gray-500 uppercase tracking-wider">Thao tác</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr v-for="tt in procedures" :key="tt.id" class="hover:bg-blue-50/30 transition-colors duration-200 group">
              <td class="px-5 py-4">
                <span class="font-mono text-xs bg-gray-100 px-2 py-1 rounded text-gray-700">
                  {{ tt.maThuTuc }}
                </span>
              </td>
              <td class="px-5 py-4">
                <span class="text-gray-900 font-medium">{{ tt.tenThuTuc }}</span>
                <p v-if="tt.moTa" class="text-xs text-gray-400 mt-0.5 line-clamp-1">{{ tt.moTa }}</p>
              </td>
              <td class="px-5 py-4">
                <span class="text-gray-700">{{ tt.thoiGianXuLy }} ngày</span>
              </td>
              <td class="px-5 py-4">
                <span class="text-gray-600 text-xs line-clamp-2">{{ tt.giayToYeuCau || '—' }}</span>
              </td>
              <td class="px-5 py-4">
                <span class="inline-flex items-center gap-1 px-2 py-0.5 bg-blue-50 text-blue-700 rounded-full text-xs font-bold">
                  {{ tt.soHoSo }}
                </span>
              </td>
              <td class="px-5 py-4">
                <span :class="tt.trangThai
                  ? 'bg-green-100 text-green-700 border-green-200'
                  : 'bg-red-100 text-red-700 border-red-200'"
                  class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-bold border"
                >
                  <span :class="tt.trangThai ? 'bg-green-500' : 'bg-red-500'" class="h-1.5 w-1.5 rounded-full"></span>
                  {{ tt.trangThai ? 'Hoạt động' : 'Đã khóa' }}
                </span>
              </td>
              <td class="px-5 py-4 text-right">
                <div class="flex items-center justify-end gap-2">
                  <button
                    @click="openEditModal(tt)"
                    class="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-blue-600 bg-blue-50 rounded-lg hover:bg-blue-100 transition-colors"
                  >
                    <Pencil class="h-3.5 w-3.5" />
                    Sửa
                  </button>
                  <button
                    @click="handleToggleStatus(tt)"
                    :class="tt.trangThai
                      ? 'text-red-600 bg-red-50 hover:bg-red-100'
                      : 'text-green-600 bg-green-50 hover:bg-green-100'"
                    class="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-lg transition-colors"
                  >
                    <component :is="tt.trangThai ? Ban : CheckCircle" class="h-3.5 w-3.5" />
                    {{ tt.trangThai ? 'Khóa' : 'Mở' }}
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="procedures.length === 0">
              <td colspan="7" class="px-5 py-12 text-center text-gray-400">
                <ClipboardList class="h-10 w-10 mx-auto mb-2 text-gray-300" />
                <p v-if="loading">Đang tải dữ liệu...</p>
                <p v-else>Không có thủ tục nào thuộc chuyên môn quầy</p>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Create Modal -->
    <Teleport to="body">
      <div v-if="showCreateModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="fixed inset-0 bg-black/50 backdrop-blur-sm" @click="showCreateModal = false"></div>
        <div class="relative bg-white rounded-2xl shadow-2xl w-full max-w-lg z-10 max-h-[90vh] overflow-y-auto">
          <div class="p-6 border-b border-gray-200">
            <div class="flex items-center justify-between">
              <h3 class="text-lg font-bold text-gray-900">Thêm thủ tục mới</h3>
              <button @click="showCreateModal = false" class="p-1.5 hover:bg-gray-100 rounded-lg transition-colors">
                <X class="h-5 w-5 text-gray-400" />
              </button>
            </div>
            <p v-if="chuyenMonName" class="text-xs text-gray-500 mt-1">
              Chuyên môn: <span class="font-medium text-blue-600">{{ chuyenMonName }}</span> (tự động gán)
            </p>
          </div>

          <form @submit.prevent="handleCreate" class="p-6 space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Mã thủ tục <span class="text-red-500">*</span></label>
                <input
                  v-model="createForm.maThuTuc"
                  type="text"
                  required
                  placeholder="VD: TT-HT-001"
                  class="w-full px-3 py-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Thời gian XL (ngày)</label>
                <input
                  v-model.number="createForm.thoiGianXuLy"
                  type="number"
                  min="1"
                  class="w-full px-3 py-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none"
                />
              </div>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Tên thủ tục <span class="text-red-500">*</span></label>
              <input
                v-model="createForm.tenThuTuc"
                type="text"
                required
                placeholder="VD: Đăng ký khai sinh"
                class="w-full px-3 py-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Mô tả</label>
              <textarea
                v-model="createForm.moTa"
                rows="2"
                placeholder="Mô tả ngắn gọn về thủ tục..."
                class="w-full px-3 py-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none resize-none"
              ></textarea>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Giấy tờ yêu cầu</label>
              <textarea
                v-model="createForm.giayToYeuCau"
                rows="3"
                placeholder="Liệt kê các giấy tờ cần thiết..."
                class="w-full px-3 py-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none resize-none"
              ></textarea>
            </div>

            <div class="flex justify-end gap-3 pt-2">
              <button
                type="button"
                @click="showCreateModal = false"
                class="px-4 py-2.5 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
              >
                Hủy
              </button>
              <button
                type="submit"
                :disabled="saving"
                class="px-6 py-2.5 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors"
              >
                {{ saving ? 'Đang tạo...' : 'Tạo thủ tục' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>

    <!-- Edit Modal -->
    <Teleport to="body">
      <div v-if="showEditModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="fixed inset-0 bg-black/50 backdrop-blur-sm" @click="showEditModal = false"></div>
        <div class="relative bg-white rounded-2xl shadow-2xl w-full max-w-lg z-10 max-h-[90vh] overflow-y-auto">
          <div class="p-6 border-b border-gray-200">
            <div class="flex items-center justify-between">
              <h3 class="text-lg font-bold text-gray-900">Chỉnh sửa thủ tục</h3>
              <button @click="showEditModal = false" class="p-1.5 hover:bg-gray-100 rounded-lg transition-colors">
                <X class="h-5 w-5 text-gray-400" />
              </button>
            </div>
            <p class="text-xs text-gray-500 mt-1">Mã: {{ editForm.maThuTuc }}</p>
          </div>

          <form @submit.prevent="handleUpdate" class="p-6 space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Tên thủ tục <span class="text-red-500">*</span></label>
              <input
                v-model="editForm.tenThuTuc"
                type="text"
                required
                class="w-full px-3 py-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Mô tả</label>
              <textarea
                v-model="editForm.moTa"
                rows="3"
                class="w-full px-3 py-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none resize-none"
              ></textarea>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Thời gian XL (ngày)</label>
                <input
                  v-model.number="editForm.thoiGianXuLy"
                  type="number"
                  min="1"
                  class="w-full px-3 py-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Thứ tự hiển thị</label>
                <input
                  v-model.number="editForm.thuTu"
                  type="number"
                  min="0"
                  class="w-full px-3 py-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none"
                />
              </div>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Giấy tờ yêu cầu</label>
              <textarea
                v-model="editForm.giayToYeuCau"
                rows="3"
                placeholder="Liệt kê các giấy tờ cần thiết..."
                class="w-full px-3 py-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none resize-none"
              ></textarea>
            </div>

            <div class="flex justify-end gap-3 pt-2">
              <button
                type="button"
                @click="showEditModal = false"
                class="px-4 py-2.5 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
              >
                Hủy
              </button>
              <button
                type="submit"
                :disabled="saving"
                class="px-6 py-2.5 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors"
              >
                {{ saving ? 'Đang lưu...' : 'Lưu thay đổi' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Layers, RefreshCw, ClipboardList, CheckCircle, FileText, Pencil, X, Plus, Ban } from 'lucide-vue-next'
import { staffLoaiThuTucApi, type LoaiThuTucData } from '@/services/api'
import { useToast } from 'vue-toastification'
import Swal from 'sweetalert2'

const toast = useToast()

const procedures = ref<LoaiThuTucData[]>([])
const loading = ref(false)
const saving = ref(false)
const showCreateModal = ref(false)
const showEditModal = ref(false)
const chuyenMonName = ref('')

const createForm = ref({
  maThuTuc: '',
  tenThuTuc: '',
  moTa: '',
  thoiGianXuLy: 15,
  giayToYeuCau: '',
  thuTu: 0,
})

const editForm = ref({
  id: 0,
  maThuTuc: '',
  tenThuTuc: '',
  moTa: '',
  thoiGianXuLy: 15,
  giayToYeuCau: '',
  thuTu: 0,
})

const activeProcedures = computed(() => procedures.value.filter(p => p.trangThai).length)
const totalHoSo = computed(() => procedures.value.reduce((sum, p) => sum + (p.soHoSo || 0), 0))

async function fetchProcedures() {
  loading.value = true
  try {
    const response = await staffLoaiThuTucApi.getMyProcedures()
    if (response.data?.data) {
      procedures.value = response.data.data
      if (procedures.value.length > 0) {
        chuyenMonName.value = procedures.value[0].tenChuyenMon || ''
      }
    }
  } catch (error: unknown) {
    const err = error as { response?: { data?: { message?: string } } }
    const msg = err?.response?.data?.message || 'Không thể tải danh sách thủ tục'
    toast.error(msg)
  } finally {
    loading.value = false
  }
}

function openCreateModal() {
  createForm.value = {
    maThuTuc: '',
    tenThuTuc: '',
    moTa: '',
    thoiGianXuLy: 15,
    giayToYeuCau: '',
    thuTu: 0,
  }
  showCreateModal.value = true
}

async function handleCreate() {
  if (!createForm.value.maThuTuc.trim() || !createForm.value.tenThuTuc.trim()) {
    toast.warning('Mã thủ tục và tên thủ tục là bắt buộc')
    return
  }

  saving.value = true
  try {
    await staffLoaiThuTucApi.create({
      maThuTuc: createForm.value.maThuTuc,
      tenThuTuc: createForm.value.tenThuTuc,
      moTa: createForm.value.moTa || undefined,
      thoiGianXuLy: createForm.value.thoiGianXuLy,
      giayToYeuCau: createForm.value.giayToYeuCau || undefined,
      thuTu: createForm.value.thuTu,
    })

    toast.success('Tạo thủ tục thành công')
    showCreateModal.value = false
    await fetchProcedures()
  } catch (error: unknown) {
    const err = error as { response?: { data?: { message?: string } } }
    toast.error(err?.response?.data?.message || 'Tạo thủ tục thất bại')
  } finally {
    saving.value = false
  }
}

function openEditModal(tt: LoaiThuTucData) {
  editForm.value = {
    id: tt.id,
    maThuTuc: tt.maThuTuc,
    tenThuTuc: tt.tenThuTuc,
    moTa: tt.moTa || '',
    thoiGianXuLy: tt.thoiGianXuLy,
    giayToYeuCau: tt.giayToYeuCau || '',
    thuTu: tt.thuTu || 0,
  }
  showEditModal.value = true
}

async function handleUpdate() {
  if (!editForm.value.tenThuTuc.trim()) {
    toast.warning('Tên thủ tục không được để trống')
    return
  }

  saving.value = true
  try {
    await staffLoaiThuTucApi.update(editForm.value.id, {
      tenThuTuc: editForm.value.tenThuTuc,
      moTa: editForm.value.moTa || undefined,
      thoiGianXuLy: editForm.value.thoiGianXuLy,
      giayToYeuCau: editForm.value.giayToYeuCau || undefined,
      thuTu: editForm.value.thuTu,
    })

    toast.success('Cập nhật thủ tục thành công')
    showEditModal.value = false
    await fetchProcedures()
  } catch (error: unknown) {
    const err = error as { response?: { data?: { message?: string } } }
    toast.error(err?.response?.data?.message || 'Cập nhật thất bại')
  } finally {
    saving.value = false
  }
}

async function handleToggleStatus(tt: LoaiThuTucData) {
  const action = tt.trangThai ? 'khóa' : 'mở khóa'

  const result = await Swal.fire({
    title: `${tt.trangThai ? 'Khóa' : 'Mở khóa'} thủ tục?`,
    text: `Bạn có chắc muốn ${action} thủ tục "${tt.tenThuTuc}"?`,
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: tt.trangThai ? '#ef4444' : '#22c55e',
    cancelButtonColor: '#6b7280',
    confirmButtonText: `Có, ${action}`,
    cancelButtonText: 'Không',
  })

  if (!result.isConfirmed) return

  try {
    if (tt.trangThai) {
      // Khóa = soft delete
      await staffLoaiThuTucApi.delete(tt.id)
    } else {
      // Mở khóa = update trangThai = true
      await staffLoaiThuTucApi.update(tt.id, { trangThai: true })
    }
    toast.success(`Đã ${action} thủ tục "${tt.tenThuTuc}"`)
    await fetchProcedures()
  } catch (error: unknown) {
    const err = error as { response?: { data?: { message?: string } } }
    toast.error(err?.response?.data?.message || `${action} thất bại`)
  }
}

onMounted(() => {
  fetchProcedures()
})
</script>
