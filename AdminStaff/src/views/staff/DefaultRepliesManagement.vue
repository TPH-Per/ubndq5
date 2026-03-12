<template>
  <div class="space-y-6 animate-in fade-in zoom-in duration-300">

    <!-- Header -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Phản hồi mặc định</h1>
        <p class="text-gray-500 mt-1 text-sm">Quản lý các mẫu câu trả lời nhanh khi tiếp nhận phản ánh của công dân</p>
      </div>
      <div class="flex gap-2 flex-wrap">
        <button
          @click="openResetConfirm"
          class="flex items-center gap-2 px-4 py-2 border border-gray-200 text-gray-600 rounded-xl hover:bg-gray-50 transition-colors text-sm font-medium"
        >
          <RotateCcw :size="15" />
          Khôi phục mặc định
        </button>
        <button
          @click="openAddModal"
          class="group flex items-center gap-2 px-5 py-2.5 bg-gradient-to-r from-blue-600 to-indigo-600 text-white rounded-xl shadow-lg shadow-blue-500/25 hover:shadow-blue-500/40 hover:-translate-y-0.5 transition-all duration-300 font-medium text-sm"
        >
          <Plus :size="18" class="group-hover:rotate-90 transition-transform duration-300" />
          Thêm mẫu mới
        </button>
      </div>
    </div>

    <!-- Stats row -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
      <div
        v-for="cat in categoryStats"
        :key="cat.name"
        class="bg-white rounded-xl p-4 border border-gray-100 shadow-sm"
      >
        <p class="text-xs text-gray-500 font-medium">{{ cat.name }}</p>
        <p class="text-2xl font-bold text-gray-800 mt-1">{{ cat.count }}</p>
      </div>
    </div>

    <!-- List -->
    <div class="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
      <!-- Filter bar -->
      <div class="p-4 border-b flex flex-wrap gap-2 items-center">
        <button
          @click="activeCategory = null"
          :class="[
            'px-3 py-1.5 rounded-full text-xs font-medium transition-colors',
            activeCategory === null
              ? 'bg-blue-600 text-white'
              : 'bg-gray-100 text-gray-600 hover:bg-gray-200',
          ]"
        >
          Tất cả ({{ replies.length }})
        </button>
        <button
          v-for="cat in categories"
          :key="cat"
          @click="activeCategory = cat"
          :class="[
            'px-3 py-1.5 rounded-full text-xs font-medium transition-colors',
            activeCategory === cat
              ? 'bg-blue-600 text-white'
              : 'bg-gray-100 text-gray-600 hover:bg-gray-200',
          ]"
        >
          {{ cat }}
        </button>
      </div>

      <!-- Empty state -->
      <div v-if="filtered.length === 0" class="py-16 text-center text-gray-400">
        <MessageSquare :size="40" class="mx-auto mb-3 opacity-30" />
        <p class="font-medium">Chưa có mẫu phản hồi nào</p>
        <p class="text-sm mt-1">Nhấn "Thêm mẫu mới" để bắt đầu</p>
      </div>

      <!-- Cards grid -->
      <div v-else class="p-4 grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
        <div
          v-for="reply in filtered"
          :key="reply.id"
          class="group border border-gray-100 rounded-xl p-4 hover:border-blue-200 hover:shadow-md transition-all duration-200 bg-gray-50/50 hover:bg-white flex flex-col gap-3"
        >
          <!-- Card header -->
          <div class="flex items-start justify-between gap-2">
            <div class="flex-1 min-w-0">
              <span class="inline-block text-xs font-medium px-2 py-0.5 rounded-full bg-blue-50 text-blue-600 border border-blue-100 mb-1.5">
                {{ reply.category }}
              </span>
              <h3 class="font-semibold text-gray-900 text-sm truncate">{{ reply.title }}</h3>
            </div>
            <div class="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity shrink-0">
              <button
                @click="openEditModal(reply)"
                class="p-1.5 rounded-lg text-gray-400 hover:text-blue-600 hover:bg-blue-50 transition-colors"
                title="Chỉnh sửa"
              >
                <Pencil :size="14" />
              </button>
              <button
                @click="openDeleteConfirm(reply)"
                class="p-1.5 rounded-lg text-gray-400 hover:text-red-600 hover:bg-red-50 transition-colors"
                title="Xóa"
              >
                <Trash2 :size="14" />
              </button>
            </div>
          </div>

          <!-- Content preview -->
          <p class="text-sm text-gray-600 leading-relaxed line-clamp-3">{{ reply.content }}</p>

          <!-- Copy button -->
          <button
            @click="copyToClipboard(reply.content)"
            class="mt-auto flex items-center gap-1.5 text-xs text-gray-400 hover:text-blue-600 transition-colors self-start"
          >
            <Copy :size="12" />
            Sao chép
          </button>
        </div>
      </div>
    </div>

    <!-- ===== ADD / EDIT MODAL ===== -->
    <div
      v-if="showModal"
      class="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4 animate-in fade-in duration-200"
      @click.self="closeModal"
    >
      <div class="bg-white rounded-2xl w-full max-w-lg shadow-2xl animate-in zoom-in-95 duration-200">
        <div class="p-5 border-b flex items-center justify-between">
          <h2 class="font-bold text-lg">
            {{ editingReply ? 'Chỉnh sửa mẫu phản hồi' : 'Thêm mẫu phản hồi mới' }}
          </h2>
          <button @click="closeModal" class="p-2 rounded-full hover:bg-gray-100 transition-colors">
            <X :size="18" />
          </button>
        </div>

        <form @submit.prevent="handleSave" class="p-5 space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1.5">Tiêu đề mẫu <span class="text-red-500">*</span></label>
            <input
              v-model="form.title"
              type="text"
              required
              placeholder="VD: Tiếp nhận & xác nhận"
              class="w-full px-3 py-2 rounded-lg border border-gray-200 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1.5">Danh mục <span class="text-red-500">*</span></label>
            <div class="flex gap-2">
              <select
                v-model="form.category"
                class="flex-1 px-3 py-2 rounded-lg border border-gray-200 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
              >
                <option value="">-- Chọn danh mục --</option>
                <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
                <option value="__new__">+ Tạo danh mục mới...</option>
              </select>
              <input
                v-if="form.category === '__new__'"
                v-model="newCategory"
                type="text"
                placeholder="Tên danh mục mới"
                class="flex-1 px-3 py-2 rounded-lg border border-gray-200 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1.5">Nội dung phản hồi <span class="text-red-500">*</span></label>
            <textarea
              v-model="form.content"
              rows="5"
              required
              placeholder="Nhập nội dung mẫu phản hồi..."
              class="w-full px-3 py-2 rounded-lg border border-gray-200 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
            ></textarea>
            <p class="text-xs text-gray-400 mt-1 text-right">{{ form.content.length }} ký tự</p>
          </div>

          <div class="flex justify-end gap-3 pt-2">
            <button type="button" @click="closeModal" class="px-4 py-2 text-sm border rounded-xl hover:bg-gray-50 transition-colors">
              Hủy
            </button>
            <button
              type="submit"
              :disabled="!isFormValid"
              class="px-5 py-2 text-sm bg-blue-600 text-white rounded-xl hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors font-medium"
            >
              {{ editingReply ? 'Lưu thay đổi' : 'Thêm mẫu' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- ===== DELETE CONFIRM ===== -->
    <div
      v-if="deleteTarget"
      class="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4 animate-in fade-in duration-200"
      @click.self="deleteTarget = null"
    >
      <div class="bg-white rounded-2xl w-full max-w-sm shadow-2xl animate-in zoom-in-95 duration-200 p-6">
        <div class="flex items-center gap-3 mb-4">
          <div class="p-2 bg-red-50 rounded-xl">
            <Trash2 :size="20" class="text-red-600" />
          </div>
          <h2 class="font-bold text-lg">Xóa mẫu phản hồi</h2>
        </div>
        <p class="text-sm text-gray-600 mb-1">Bạn có chắc muốn xóa mẫu:</p>
        <p class="font-semibold text-gray-900 mb-5">{{ deleteTarget.title }}</p>
        <div class="flex justify-end gap-3">
          <button @click="deleteTarget = null" class="px-4 py-2 text-sm border rounded-xl hover:bg-gray-50 transition-colors">
            Hủy
          </button>
          <button
            @click="confirmDelete"
            class="px-5 py-2 text-sm bg-red-600 text-white rounded-xl hover:bg-red-700 transition-colors font-medium"
          >
            Xóa
          </button>
        </div>
      </div>
    </div>

    <!-- ===== RESET CONFIRM ===== -->
    <div
      v-if="showResetConfirm"
      class="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4 animate-in fade-in duration-200"
      @click.self="showResetConfirm = false"
    >
      <div class="bg-white rounded-2xl w-full max-w-sm shadow-2xl animate-in zoom-in-95 duration-200 p-6">
        <div class="flex items-center gap-3 mb-4">
          <div class="p-2 bg-orange-50 rounded-xl">
            <RotateCcw :size="20" class="text-orange-600" />
          </div>
          <h2 class="font-bold text-lg">Khôi phục mặc định</h2>
        </div>
        <p class="text-sm text-gray-600 mb-5">
          Thao tác này sẽ <strong>xóa toàn bộ</strong> các mẫu hiện tại và khôi phục về danh sách mặc định ban đầu.
        </p>
        <div class="flex justify-end gap-3">
          <button @click="showResetConfirm = false" class="px-4 py-2 text-sm border rounded-xl hover:bg-gray-50 transition-colors">
            Hủy
          </button>
          <button
            @click="confirmReset"
            class="px-5 py-2 text-sm bg-orange-600 text-white rounded-xl hover:bg-orange-700 transition-colors font-medium"
          >
            Khôi phục
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Plus, Pencil, Trash2, X, Copy, MessageSquare, RotateCcw } from 'lucide-vue-next'
import { toast } from 'vue-sonner'
import { useDefaultReplies, type DefaultReply } from '@/lib/useDefaultReplies'

const { replies, add, update, remove, resetToDefaults } = useDefaultReplies()

// ── Category filter ──
const activeCategory = ref<string | null>(null)

const categories = computed(() => {
  const cats = new Set(replies.value.map((r) => r.category))
  return Array.from(cats).sort()
})

const filtered = computed(() =>
  activeCategory.value
    ? replies.value.filter((r) => r.category === activeCategory.value)
    : replies.value
)

const categoryStats = computed(() => {
  return categories.value.map((cat) => ({
    name: cat,
    count: replies.value.filter((r) => r.category === cat).length,
  }))
})

// ── Add / Edit modal ──
const showModal = ref(false)
const editingReply = ref<DefaultReply | null>(null)
const form = ref({ title: '', content: '', category: '' })
const newCategory = ref('')

const isFormValid = computed(() => {
  const cat = form.value.category === '__new__' ? newCategory.value.trim() : form.value.category
  return form.value.title.trim() && form.value.content.trim() && cat
})

function openAddModal() {
  editingReply.value = null
  form.value = { title: '', content: '', category: '' }
  newCategory.value = ''
  showModal.value = true
}

function openEditModal(reply: DefaultReply) {
  editingReply.value = reply
  form.value = { title: reply.title, content: reply.content, category: reply.category }
  newCategory.value = ''
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

function handleSave() {
  const cat = form.value.category === '__new__' ? newCategory.value.trim() : form.value.category
  if (!cat) return

  if (editingReply.value) {
    update({ ...editingReply.value, ...form.value, category: cat })
    toast.success('Đã cập nhật mẫu phản hồi')
  } else {
    add({ title: form.value.title, content: form.value.content, category: cat })
    toast.success('Đã thêm mẫu phản hồi mới')
  }
  closeModal()
}

// ── Delete ──
const deleteTarget = ref<DefaultReply | null>(null)

function openDeleteConfirm(reply: DefaultReply) {
  deleteTarget.value = reply
}

function confirmDelete() {
  if (!deleteTarget.value) return
  remove(deleteTarget.value.id)
  toast.success('Đã xóa mẫu phản hồi')
  deleteTarget.value = null
}

// ── Reset ──
const showResetConfirm = ref(false)

function openResetConfirm() {
  showResetConfirm.value = true
}

function confirmReset() {
  resetToDefaults()
  activeCategory.value = null
  showResetConfirm.value = false
  toast.success('Đã khôi phục về danh sách mặc định')
}

// ── Copy ──
function copyToClipboard(text: string) {
  navigator.clipboard.writeText(text).then(() => {
    toast.success('Đã sao chép nội dung')
  })
}
</script>
