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
          <h3 class="text-lg font-semibold text-gray-900">Thêm quầy mới</h3>
          <button 
            @click="$emit('close')"
            class="p-1 text-gray-400 hover:text-gray-600 transition-colors"
          >
            <X :size="20" />
          </button>
        </div>

        <!-- Form -->
        <form @submit.prevent="handleSubmit" class="px-6 py-4 space-y-4">
          <!-- Mã quầy -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">
              Mã quầy <span class="text-red-500">*</span>
            </label>
            <input
              v-model="form.maQuay"
              type="text"
              placeholder="VD: Q1, Q2, Q3"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
              :class="{ 'border-red-500': errors.maQuay }"
            />
            <p v-if="errors.maQuay" class="mt-1 text-sm text-red-500">{{ errors.maQuay }}</p>
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
              <p class="mt-1 text-xs text-gray-400">Dùng cho số thứ tự (A001, B001...)</p>
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

          <!-- Error message -->
          <div v-if="submitError" class="p-3 bg-red-50 border border-red-200 rounded-lg">
            <p class="text-sm text-red-600">{{ submitError }}</p>
          </div>
        </form>

        <!-- Footer -->
        <div class="flex items-center justify-end gap-3 px-6 py-4 border-t border-gray-100 bg-gray-50 rounded-b-xl">
          <button
            type="button"
            @click="$emit('close')"
            class="px-4 py-2 text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
          >
            Hủy
          </button>
          <button
            @click="handleSubmit"
            :disabled="submitting"
            class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            <Loader2 v-if="submitting" :size="18" class="animate-spin" />
            {{ submitting ? 'Đang tạo...' : 'Tạo quầy' }}
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

// Emits
const emit = defineEmits<{
  close: [];
  created: [quay: QuayData];
}>();

// State
const chuyenMons = ref<ChuyenMonData[]>([]);
const submitting = ref(false);
const submitError = ref<string | null>(null);

const form = reactive({
  maQuay: '',
  tenQuay: '',
  viTri: '',
  prefixSo: '',
  chuyenMonId: null as number | null,
  ghiChu: '',
});

const errors = reactive({
  maQuay: '',
  tenQuay: '',
  prefixSo: '',
  chuyenMonId: '',
});

// Methods
function validate(): boolean {
  let isValid = true;
  
  // Reset errors
  Object.keys(errors).forEach(key => {
    errors[key as keyof typeof errors] = '';
  });

  if (!form.maQuay.trim()) {
    errors.maQuay = 'Vui lòng nhập mã quầy';
    isValid = false;
  }

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
  if (!validate()) return;

  submitting.value = true;
  submitError.value = null;

  try {
    const response = await quayApi.create({
      maQuay: form.maQuay.trim(),
      tenQuay: form.tenQuay.trim(),
      viTri: form.viTri.trim() || undefined,
      prefixSo: form.prefixSo.trim(),
      chuyenMonId: form.chuyenMonId!,
      ghiChu: form.ghiChu.trim() || undefined,
    });

    if (response.data.success) {
      emit('created', response.data.data);
    } else {
      submitError.value = response.data.message;
    }
  } catch (err: any) {
    submitError.value = err.response?.data?.message || 'Có lỗi xảy ra, vui lòng thử lại';
  } finally {
    submitting.value = false;
  }
}

async function fetchChuyenMons() {
  try {
    const response = await chuyenMonApi.getAll();
    if (response.data.success) {
      chuyenMons.value = response.data.data;
    }
  } catch (err) {
    console.error('Failed to fetch chuyen mons:', err);
  }
}

// Lifecycle
onMounted(() => {
  fetchChuyenMons();
});
</script>
