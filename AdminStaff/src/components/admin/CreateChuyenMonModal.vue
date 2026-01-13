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
          <h3 class="text-lg font-semibold text-gray-900">Thêm chuyên môn mới</h3>
          <button 
            @click="$emit('close')"
            class="p-1 text-gray-400 hover:text-gray-600 transition-colors"
          >
            <X :size="20" />
          </button>
        </div>

        <!-- Form -->
        <form @submit.prevent="handleSubmit" class="px-6 py-4 space-y-4">
          <!-- Mã chuyên môn -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">
              Mã chuyên môn <span class="text-red-500">*</span>
            </label>
            <input
              v-model="form.maChuyenMon"
              type="text"
              placeholder="VD: CM01, DT-HT"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
              :class="{ 'border-red-500': errors.maChuyenMon }"
            />
            <p v-if="errors.maChuyenMon" class="mt-1 text-sm text-red-500">{{ errors.maChuyenMon }}</p>
          </div>

          <!-- Tên chuyên môn -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">
              Tên chuyên môn <span class="text-red-500">*</span>
            </label>
            <input
              v-model="form.tenChuyenMon"
              type="text"
              placeholder="VD: Dân số - Hộ tịch"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
              :class="{ 'border-red-500': errors.tenChuyenMon }"
            />
            <p v-if="errors.tenChuyenMon" class="mt-1 text-sm text-red-500">{{ errors.tenChuyenMon }}</p>
          </div>

          <!-- Mô tả -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">
              Mô tả
            </label>
            <textarea
              v-model="form.moTa"
              rows="3"
              placeholder="Mô tả về chuyên môn (tùy chọn)"
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
            {{ submitting ? 'Đang tạo...' : 'Tạo chuyên môn' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { X, Loader2 } from 'lucide-vue-next';
import { chuyenMonApi, type ChuyenMonData } from '@/services/api';

// Emits
const emit = defineEmits<{
  close: [];
  created: [chuyenMon: ChuyenMonData];
}>();

// State
const submitting = ref(false);
const submitError = ref<string | null>(null);

const form = reactive({
  maChuyenMon: '',
  tenChuyenMon: '',
  moTa: '',
});

const errors = reactive({
  maChuyenMon: '',
  tenChuyenMon: '',
});

// Methods
function validate(): boolean {
  let isValid = true;
  
  errors.maChuyenMon = '';
  errors.tenChuyenMon = '';

  if (!form.maChuyenMon.trim()) {
    errors.maChuyenMon = 'Vui lòng nhập mã chuyên môn';
    isValid = false;
  }

  if (!form.tenChuyenMon.trim()) {
    errors.tenChuyenMon = 'Vui lòng nhập tên chuyên môn';
    isValid = false;
  }

  return isValid;
}

async function handleSubmit() {
  if (!validate()) return;

  submitting.value = true;
  submitError.value = null;

  try {
    const response = await chuyenMonApi.create({
      maChuyenMon: form.maChuyenMon.trim(),
      tenChuyenMon: form.tenChuyenMon.trim(),
      moTa: form.moTa.trim() || undefined,
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
</script>
