<template>
  <div class="fixed inset-0 z-50 overflow-y-auto">
    <!-- Backdrop -->
    <div 
      class="fixed inset-0 bg-black/50 transition-opacity"
      @click="$emit('close')"
    ></div>
    
    <!-- Modal -->
    <div class="flex min-h-full items-center justify-center p-4">
      <div 
        class="relative w-full max-w-lg bg-white rounded-xl shadow-xl transform transition-all"
        @click.stop
      >
        <!-- Header -->
        <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
          <h3 class="text-lg font-semibold text-gray-900">Chỉnh sửa quầy</h3>
          <button 
            @click="$emit('close')"
            class="p-1 text-gray-400 hover:text-gray-600 transition-colors"
          >
            <X :size="20" />
          </button>
        </div>

        <!-- Loading State -->
        <div v-if="loading" class="flex items-center justify-center py-12">
          <Loader2 :size="32" class="animate-spin text-blue-600" />
        </div>

        <!-- Form -->
        <form v-else @submit.prevent="handleSubmit" class="px-6 py-4 space-y-4">
          <!-- Mã quầy (Read-only) -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">
              Mã quầy
            </label>
            <input
              :value="props.quay?.maQuay"
              type="text"
              disabled
              class="w-full px-3 py-2 border border-gray-200 rounded-lg bg-gray-50 text-gray-500 cursor-not-allowed"
            />
            <p class="mt-1 text-xs text-gray-400">Mã quầy không thể thay đổi</p>
          </div>

          <!-- Tên quầy -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">
              Tên quầy <span class="text-red-500">*</span>
            </label>
            <input
              v-model="form.tenQuay"
              type="text"
              placeholder="VD: Quầy Dân số - Hộ tịch"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
              :class="{ 'border-red-500': errors.tenQuay }"
            />
            <p v-if="errors.tenQuay" class="mt-1 text-sm text-red-500">{{ errors.tenQuay }}</p>
          </div>

          <!-- Prefix số & Chuyên môn -->
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">
                Prefix số <span class="text-red-500">*</span>
              </label>
              <input
                v-model="form.prefixSo"
                type="text"
                placeholder="VD: A, B, C"
                maxlength="5"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                :class="{ 'border-red-500': errors.prefixSo }"
              />
              <p v-if="errors.prefixSo" class="mt-1 text-sm text-red-500">{{ errors.prefixSo }}</p>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">
                Chuyên môn <span class="text-red-500">*</span>
              </label>
              <select
                v-model="form.chuyenMonId"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                :class="{ 'border-red-500': errors.chuyenMonId }"
              >
                <option :value="null" disabled>Chọn chuyên môn</option>
                <option v-for="cm in chuyenMons" :key="cm.id" :value="cm.id">
                  {{ cm.tenChuyenMon }}
                </option>
              </select>
              <p v-if="errors.chuyenMonId" class="mt-1 text-sm text-red-500">{{ errors.chuyenMonId }}</p>
            </div>
          </div>

          <!-- Vị trí -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">
              Vị trí
            </label>
            <input
              v-model="form.viTri"
              type="text"
              placeholder="VD: Tầng 1, Phòng A"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
            />
          </div>

          <!-- Ghi chú -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">
              Ghi chú
            </label>
            <textarea
              v-model="form.ghiChu"
              rows="2"
              placeholder="Ghi chú thêm (tùy chọn)"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors resize-none"
            ></textarea>
          </div>

          <!-- Trạng thái -->
          <div class="flex items-center gap-3">
            <label class="relative inline-flex items-center cursor-pointer">
              <input
                type="checkbox"
                v-model="form.trangThai"
                class="sr-only peer"
              />
              <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
            </label>
            <span class="text-sm font-medium text-gray-700">
              {{ form.trangThai ? 'Quầy đang hoạt động' : 'Quầy đã khóa' }}
            </span>
          </div>

          <!-- Error message -->
          <div v-if="submitError" class="p-3 bg-red-50 border border-red-200 rounded-lg">
            <p class="text-sm text-red-600">{{ submitError }}</p>
          </div>

          <!-- Success message -->
          <div v-if="submitSuccess" class="p-3 bg-green-50 border border-green-200 rounded-lg">
            <p class="text-sm text-green-600">{{ submitSuccess }}</p>
          </div>
        </form>

        <!-- Footer -->
        <div class="flex items-center justify-end gap-3 px-6 py-4 border-t border-gray-100 bg-gray-50 rounded-b-xl">
          <button
            type="button"
            @click="$emit('close')"
            class="px-4 py-2 text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
          >
            Đóng
          </button>
          <button
            @click="handleSubmit"
            :disabled="submitting"
            class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            <Loader2 v-if="submitting" :size="18" class="animate-spin" />
            {{ submitting ? 'Đang lưu...' : 'Lưu thay đổi' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { X, Loader2 } from 'lucide-vue-next';
import { quayApi, chuyenMonApi, type ChuyenMonData, type QuayData } from '@/services/api';

// Props
const props = defineProps<{
  quay: QuayData | null;
}>();

// Emits
const emit = defineEmits<{
  close: [];
  updated: [quay: QuayData];
}>();

// State
const loading = ref(true);
const chuyenMons = ref<ChuyenMonData[]>([]);
const submitting = ref(false);
const submitError = ref<string | null>(null);
const submitSuccess = ref<string | null>(null);

const form = reactive({
  tenQuay: '',
  viTri: '',
  prefixSo: '',
  chuyenMonId: null as number | null,
  ghiChu: '',
  trangThai: true,
});

const errors = reactive({
  tenQuay: '',
  prefixSo: '',
  chuyenMonId: '',
});

// Lifecycle
onMounted(async () => {
  try {
    // Load chuyên môn
    const cmResponse = await chuyenMonApi.getAll();
    if (cmResponse.data.success) {
      chuyenMons.value = cmResponse.data.data;
    }

    // Populate form with current quay data
    if (props.quay) {
      form.tenQuay = props.quay.tenQuay || '';
      form.viTri = props.quay.viTri || '';
      form.prefixSo = props.quay.prefixSo || '';
      form.chuyenMonId = props.quay.chuyenMonId || null;
      form.ghiChu = props.quay.ghiChu || '';
      form.trangThai = props.quay.trangThai;
    }
  } catch (err) {
    console.error('Error loading data:', err);
    submitError.value = 'Không thể tải dữ liệu';
  } finally {
    loading.value = false;
  }
});

// Methods
function validate(): boolean {
  let isValid = true;
  
  Object.keys(errors).forEach(key => {
    errors[key as keyof typeof errors] = '';
  });

  if (!form.tenQuay.trim()) {
    errors.tenQuay = 'Vui lòng nhập tên quầy';
    isValid = false;
  }

  if (!form.prefixSo.trim()) {
    errors.prefixSo = 'Vui lòng nhập prefix số';
    isValid = false;
  }

  if (!form.chuyenMonId) {
    errors.chuyenMonId = 'Vui lòng chọn chuyên môn';
    isValid = false;
  }

  return isValid;
}

async function handleSubmit() {
  if (!validate() || !props.quay) return;

  submitting.value = true;
  submitError.value = null;
  submitSuccess.value = null;

  try {
    const response = await quayApi.update(props.quay.id, {
      tenQuay: form.tenQuay.trim(),
      viTri: form.viTri.trim() || undefined,
      prefixSo: form.prefixSo.trim(),
      chuyenMonId: form.chuyenMonId!,
      ghiChu: form.ghiChu.trim() || undefined,
      trangThai: form.trangThai,
    });

    if (response.data.success) {
      submitSuccess.value = 'Cập nhật quầy thành công!';
      emit('updated', response.data.data);
      
      // Auto close after 1.5s
      setTimeout(() => {
        emit('close');
      }, 1500);
    } else {
      submitError.value = response.data.message;
    }
  } catch (err: any) {
    submitError.value = err.response?.data?.message || 'Có lỗi xảy ra, vui lòng thử lại';
  } finally {
    submitting.value = false;
  }
}
</script>
