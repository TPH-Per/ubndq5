<template>
  <div class="space-y-6 animate-in fade-in zoom-in duration-300">

    <!-- Header -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Quản lý hồ sơ tổng hợp</h1>
        <p class="text-gray-500 mt-1 text-sm">Xem và theo dõi hồ sơ của tất cả các quầy theo ngày</p>
      </div>
      <button
        @click="loadAll"
        :class="{ 'animate-spin': loadingDash }"
        class="p-2 rounded-xl border border-gray-200 hover:bg-gray-50 transition-colors"
        title="Làm mới"
      >
        <RefreshCw :size="18" class="text-gray-500" />
      </button>
    </div>

    <!-- Summary stat cards -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
      <div class="bg-white rounded-2xl p-5 border border-gray-100 shadow-sm hover:shadow-md transition-all group">
        <div class="flex items-center gap-3">
          <div class="p-2.5 bg-blue-50 rounded-xl group-hover:scale-110 transition-transform">
            <Calendar :size="22" class="text-blue-600" />
          </div>
          <div>
            <p class="text-xs text-gray-500 font-medium">Hôm nay</p>
            <p class="text-2xl font-bold text-gray-800">{{ dash?.tongHomNay ?? '–' }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-2xl p-5 border border-gray-100 shadow-sm hover:shadow-md transition-all group">
        <div class="flex items-center gap-3">
          <div class="p-2.5 bg-indigo-50 rounded-xl group-hover:scale-110 transition-transform">
            <CalendarDays :size="22" class="text-indigo-600" />
          </div>
          <div>
            <p class="text-xs text-gray-500 font-medium">Ngày mai</p>
            <p class="text-2xl font-bold text-gray-800">{{ dash?.tongNgayMai ?? '–' }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-2xl p-5 border border-gray-100 shadow-sm hover:shadow-md transition-all group">
        <div class="flex items-center gap-3">
          <div class="p-2.5 bg-emerald-50 rounded-xl group-hover:scale-110 transition-transform">
            <Archive :size="22" class="text-emerald-600" />
          </div>
          <div>
            <p class="text-xs text-gray-500 font-medium">Tổng hệ thống</p>
            <p class="text-2xl font-bold text-gray-800">{{ dash?.tongTatCa ?? '–' }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-2xl p-5 border border-gray-100 shadow-sm hover:shadow-md transition-all group">
        <div class="flex items-center gap-3">
          <div class="p-2.5 bg-amber-50 rounded-xl group-hover:scale-110 transition-transform">
            <Building2 :size="22" class="text-amber-600" />
          </div>
          <div>
            <p class="text-xs text-gray-500 font-medium">Số quầy ngày lọc</p>
            <p class="text-2xl font-bold text-gray-800">{{ dash?.perCounter?.length ?? '–' }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Per-counter mini cards for selected date -->
    <div v-if="dash?.perCounter?.length" class="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
      <h2 class="text-sm font-semibold text-gray-600 mb-3 flex items-center gap-2">
        <BarChart3 :size="15" />
        Phân bổ theo quầy — {{ formatDate(filterDate) }}
      </h2>
      <div class="flex flex-wrap gap-3">
        <button
          v-for="c in dash.perCounter"
          :key="c.quayId"
          @click="filterQuayId = filterQuayId === c.quayId ? undefined : c.quayId; loadList()"
          :class="[
            'flex items-center gap-2 px-3 py-2 rounded-xl border text-sm font-medium transition-all',
            filterQuayId === c.quayId
              ? 'border-blue-500 bg-blue-50 text-blue-700'
              : 'border-gray-200 hover:border-blue-300 text-gray-700 hover:bg-gray-50'
          ]"
        >
          <span class="font-bold text-lg leading-none">{{ c.soLuong }}</span>
          <span class="text-xs">{{ c.tenQuay }}</span>
        </button>
      </div>
    </div>

    <!-- Filter Bar -->
    <div class="bg-white rounded-2xl border border-gray-100 shadow-sm">
      <div class="p-4 border-b flex flex-wrap gap-3 items-center">
        <!-- Date quick buttons -->
        <div class="flex gap-1.5 bg-gray-100 p-1 rounded-xl">
          <button
            @click="setDate('today')"
            :class="['px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors', filterDate === todayStr ? 'bg-white shadow text-blue-700' : 'text-gray-500 hover:text-gray-700']"
          >
            Hôm nay
          </button>
          <button
            @click="setDate('tomorrow')"
            :class="['px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors', filterDate === tomorrowStr ? 'bg-white shadow text-indigo-700' : 'text-gray-500 hover:text-gray-700']"
          >
            Ngày mai
          </button>
          <button
            @click="setDate('')"
            :class="['px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors', !filterDate ? 'bg-white shadow text-gray-700' : 'text-gray-500 hover:text-gray-700']"
          >
            Tất cả
          </button>
        </div>

        <!-- Custom date -->
        <input
          type="date"
          v-model="filterDate"
          @change="loadAll"
          class="px-3 py-1.5 rounded-xl border border-gray-200 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
        />

        <!-- Counter filter dropdown -->
        <select
          v-model="filterQuayId"
          @change="loadList()"
          class="px-3 py-1.5 rounded-xl border border-gray-200 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
        >
          <option :value="undefined">Tất cả quầy</option>
          <option v-for="c in allCounters" :key="c.id" :value="c.id">
            {{ c.tenQuay }}
          </option>
        </select>

        <!-- Status filter -->
        <select
          v-model="filterTrangThai"
          @change="loadList()"
          class="px-3 py-1.5 rounded-xl border border-gray-200 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
        >
          <option :value="undefined">Tất cả trạng thái</option>
          <option :value="1">Chờ tiếp nhận</option>
          <option :value="2">Chờ xử lý</option>
          <option :value="3">Đang xử lý</option>
          <option :value="4">Hoàn thành</option>
          <option :value="0">Đã hủy</option>
          <option :value="6">Bổ sung</option>
        </select>

        <span class="ml-auto text-xs text-gray-400 font-medium">
          {{ totalElements }} hồ sơ
        </span>
      </div>

      <!-- Table -->
      <div class="overflow-x-auto">
        <!-- Loading -->
        <div v-if="loadingList" class="p-12 text-center">
          <div class="inline-block animate-spin rounded-full h-8 w-8 border-4 border-blue-600 border-t-transparent"></div>
          <p class="mt-3 text-sm text-gray-400">Đang tải dữ liệu...</p>
        </div>

        <!-- Empty -->
        <div v-else-if="!list.length" class="p-12 text-center text-gray-400">
          <FileSearch :size="40" class="mx-auto mb-3 opacity-30" />
          <p class="font-medium">Không có hồ sơ nào</p>
          <p class="text-sm mt-1">Thử thay đổi bộ lọc</p>
        </div>

        <!-- Table content -->
        <table v-else class="w-full text-sm">
          <thead class="bg-gray-50 border-b">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wide">Mã hồ sơ</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wide">Công dân</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wide">Thủ tục</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wide">Quầy</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wide">Ngày nộp</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wide">Trạng thái</th>
              <th class="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-50">
            <tr
              v-for="hs in list"
              :key="hs.id"
              class="hover:bg-gray-50/80 transition-colors group"
            >
              <td class="px-4 py-3">
                <span class="font-mono text-xs font-semibold text-blue-700 bg-blue-50 px-2 py-1 rounded">
                  {{ hs.maHoSo }}
                </span>
              </td>
              <td class="px-4 py-3">
                <p class="font-medium text-gray-900">{{ hs.hoTenCongDan }}</p>
                <p class="text-xs text-gray-400">{{ hs.cccd }}</p>
              </td>
              <td class="px-4 py-3 max-w-[180px]">
                <p class="truncate text-gray-700" :title="hs.tenThuTuc">{{ hs.tenThuTuc }}</p>
              </td>
              <td class="px-4 py-3">
                <span class="text-xs px-2 py-1 rounded-full border font-medium"
                  :class="hs.tenQuay ? 'bg-purple-50 text-purple-700 border-purple-100' : 'bg-gray-50 text-gray-400 border-gray-200'"
                >
                  {{ hs.tenQuay || 'Chưa phân' }}
                </span>
              </td>
              <td class="px-4 py-3 text-xs text-gray-500 whitespace-nowrap">
                {{ formatDateTime(hs.ngayNop) }}
              </td>
              <td class="px-4 py-3">
                <span :class="statusClass(hs.trangThai)" class="text-xs px-2 py-1 rounded-full font-semibold">
                  {{ hs.trangThaiText }}
                </span>
              </td>
              <td class="px-4 py-3 text-right">
                <button
                  @click="openDetail(hs)"
                  class="opacity-0 group-hover:opacity-100 px-3 py-1.5 text-xs font-medium text-blue-600 bg-blue-50 hover:bg-blue-100 rounded-lg transition-all border border-blue-200"
                >
                  Chi tiết
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="p-4 border-t flex items-center justify-between gap-3 flex-wrap">
        <p class="text-xs text-gray-400">
          Trang {{ currentPage + 1 }} / {{ totalPages }} &nbsp;·&nbsp; {{ totalElements }} hồ sơ
        </p>
        <div class="flex gap-1.5">
          <button
            @click="goPage(currentPage - 1)"
            :disabled="currentPage === 0"
            class="px-3 py-1.5 text-xs rounded-lg border disabled:opacity-40 disabled:cursor-not-allowed hover:bg-gray-50 transition-colors"
          >
            ← Trước
          </button>
          <button
            v-for="p in visiblePages"
            :key="p"
            @click="goPage(p)"
            :class="[
              'px-3 py-1.5 text-xs rounded-lg border transition-colors',
              p === currentPage ? 'bg-blue-600 text-white border-blue-600' : 'hover:bg-gray-50'
            ]"
          >
            {{ p + 1 }}
          </button>
          <button
            @click="goPage(currentPage + 1)"
            :disabled="currentPage >= totalPages - 1"
            class="px-3 py-1.5 text-xs rounded-lg border disabled:opacity-40 disabled:cursor-not-allowed hover:bg-gray-50 transition-colors"
          >
            Sau →
          </button>
        </div>
      </div>
    </div>

    <!-- ===== DETAIL MODAL ===== -->
    <div
      v-if="selectedHoSo"
      class="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex items-center justify-center p-4 animate-in fade-in duration-200"
      @click.self="selectedHoSo = null"
    >
      <div class="bg-white rounded-2xl w-full max-w-2xl max-h-[90vh] flex flex-col shadow-2xl animate-in zoom-in-95 duration-200">
        <!-- Header -->
        <div class="p-5 border-b flex items-center justify-between bg-gray-50/50">
          <div>
            <h2 class="font-bold text-lg">Chi tiết hồ sơ</h2>
            <p class="text-sm text-gray-500 font-mono">{{ selectedHoSo.maHoSo }}</p>
          </div>
          <button @click="selectedHoSo = null" class="p-2 rounded-full hover:bg-gray-200 transition-colors">
            <X :size="18" />
          </button>
        </div>

        <div class="p-5 overflow-y-auto flex-1 space-y-5">
          <!-- Citizen info -->
          <div class="flex items-start gap-4 p-4 bg-blue-50/50 rounded-xl border border-blue-100">
            <div class="w-11 h-11 rounded-full bg-blue-100 flex items-center justify-center text-blue-700 font-bold text-lg border-2 border-white shadow-sm">
              {{ selectedHoSo.hoTenCongDan?.charAt(0) }}
            </div>
            <div class="flex-1 min-w-0">
              <p class="font-bold text-gray-900">{{ selectedHoSo.hoTenCongDan }}</p>
              <p class="text-sm text-gray-500">CCCD: {{ selectedHoSo.cccd }}</p>
              <p v-if="selectedHoSo.soDienThoai" class="text-sm text-gray-500">SĐT: {{ selectedHoSo.soDienThoai }}</p>
            </div>
            <span :class="statusClass(selectedHoSo.trangThai)" class="text-xs px-2.5 py-1 rounded-full font-semibold shrink-0">
              {{ selectedHoSo.trangThaiText }}
            </span>
          </div>

          <!-- Hồ sơ info -->
          <div class="grid grid-cols-2 gap-3 text-sm">
            <div class="bg-gray-50 rounded-xl p-3">
              <p class="text-xs text-gray-400 mb-1">Thủ tục</p>
              <p class="font-semibold text-gray-900">{{ selectedHoSo.tenThuTuc }}</p>
            </div>
            <div class="bg-gray-50 rounded-xl p-3">
              <p class="text-xs text-gray-400 mb-1">Quầy xử lý</p>
              <p class="font-semibold text-gray-900">{{ selectedHoSo.tenQuay || 'Chưa phân' }}</p>
            </div>
            <div class="bg-gray-50 rounded-xl p-3">
              <p class="text-xs text-gray-400 mb-1">Ngày nộp</p>
              <p class="font-semibold text-gray-900">{{ formatDateTime(selectedHoSo.ngayNop) }}</p>
            </div>
            <div class="bg-gray-50 rounded-xl p-3">
              <p class="text-xs text-gray-400 mb-1">Hạn xử lý</p>
              <p :class="isOverdue(selectedHoSo.hanXuLy) ? 'text-red-600 font-bold' : 'font-semibold text-gray-900'">
                {{ selectedHoSo.hanXuLy || '—' }}
              </p>
            </div>
            <div class="bg-gray-50 rounded-xl p-3">
              <p class="text-xs text-gray-400 mb-1">Nguồn gốc</p>
              <p class="font-semibold text-gray-900">{{ selectedHoSo.nguonGoc }}</p>
            </div>
            <div class="bg-gray-50 rounded-xl p-3">
              <p class="text-xs text-gray-400 mb-1">Ưu tiên</p>
              <p class="font-semibold text-gray-900">{{ priorityLabel(selectedHoSo.doUuTien) }}</p>
            </div>
          </div>

          <!-- History -->
          <div v-if="selectedHoSo.lichSuXuLy?.length" class="space-y-2">
            <h3 class="text-sm font-semibold text-gray-600 flex items-center gap-2">
              <History :size="14" /> Lịch sử xử lý
            </h3>
            <div class="space-y-2 max-h-52 overflow-y-auto pr-1">
              <div
                v-for="(h, i) in selectedHoSo.lichSuXuLy"
                :key="i"
                class="flex gap-3 text-sm"
              >
                <div class="w-1.5 h-1.5 rounded-full bg-blue-400 mt-1.5 shrink-0"></div>
                <div class="flex-1 pb-2 border-b border-gray-50 last:border-0">
                  <div class="flex justify-between items-start gap-2">
                    <span class="font-medium text-gray-800">{{ h.hanhDong }}</span>
                    <span class="text-xs text-gray-400 whitespace-nowrap">{{ formatDateTime(h.thoiGian) }}</span>
                  </div>
                  <p v-if="h.nguoiXuLy" class="text-xs text-gray-500">{{ h.nguoiXuLy }}</p>
                  <p v-if="h.noiDung" class="text-xs text-gray-500 mt-0.5">{{ h.noiDung }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { adminHoSoApi, quayApi, type HoSoData, type HoSoDetailData, type AdminHoSoDashboardData, type QuayData } from '@/services/api'
import { RefreshCw, Calendar, CalendarDays, Archive, Building2, BarChart3, FileSearch, X, History } from 'lucide-vue-next'
import { toast } from 'vue-sonner'

// ── state ──
const dash = ref<AdminHoSoDashboardData | null>(null)
const allCounters = ref<QuayData[]>([])
const list = ref<HoSoData[]>([])
const loadingDash = ref(false)
const loadingList = ref(false)
const totalElements = ref(0)
const totalPages = ref(0)
const currentPage = ref(0)
const selectedHoSo = ref<HoSoDetailData | null>(null)

// ── filter state ──
const todayStr    = new Date().toISOString().slice(0, 10)
const tomorrowStr = new Date(Date.now() + 86400000).toISOString().slice(0, 10)
const filterDate      = ref(todayStr)
const filterQuayId    = ref<number | undefined>(undefined)
const filterTrangThai = ref<number | undefined>(undefined)

// ── dashboard ──
async function loadDash() {
  loadingDash.value = true
  try {
    const res = await adminHoSoApi.getDashboard(filterDate.value || undefined)
    if (res.data.success) dash.value = res.data.data
  } catch {
    toast.error('Không thể tải thống kê')
  } finally {
    loadingDash.value = false
  }
}

// ── list ──
async function loadList(reset = false) {
  if (reset) currentPage.value = 0
  loadingList.value = true
  try {
    const params: Record<string, unknown> = {
      page: currentPage.value,
      size: 20,
    }
    if (filterDate.value)      params.date      = filterDate.value
    if (filterQuayId.value)    params.quayId    = filterQuayId.value
    if (filterTrangThai.value !== undefined) params.trangThai = filterTrangThai.value

    const res = await adminHoSoApi.getList(params as Parameters<typeof adminHoSoApi.getList>[0])
    if (res.data.success) {
      const page = res.data.data
      list.value          = page.content
      totalElements.value = page.totalElements
      totalPages.value    = page.totalPages
      currentPage.value   = page.number
    }
  } catch {
    toast.error('Không thể tải danh sách hồ sơ')
  } finally {
    loadingList.value = false
  }
}

function loadAll() {
  loadDash()
  loadList(true)
}

function setDate(d: string) {
  filterDate.value = d === 'today' ? todayStr : d === 'tomorrow' ? tomorrowStr : ''
  filterQuayId.value = undefined
  loadAll()
}

function goPage(p: number) {
  if (p < 0 || p >= totalPages.value) return
  currentPage.value = p
  loadList()
}

const visiblePages = computed(() => {
  const total = totalPages.value
  const cur   = currentPage.value
  const pages: number[] = []
  for (let i = Math.max(0, cur - 2); i <= Math.min(total - 1, cur + 2); i++) {
    pages.push(i)
  }
  return pages
})

async function openDetail(hs: HoSoData) {
  try {
    const res = await adminHoSoApi.getById(hs.id)
    if (res.data.success) selectedHoSo.value = res.data.data as HoSoDetailData
    else selectedHoSo.value = hs as HoSoDetailData
  } catch {
    selectedHoSo.value = hs as HoSoDetailData
  }
}

// ── helpers ──
function formatDate(dateStr: string) {
  if (!dateStr) return ''
  try {
    const d = new Date(dateStr)
    return `${d.getDate().toString().padStart(2,'0')}/${(d.getMonth()+1).toString().padStart(2,'0')}/${d.getFullYear()}`
  } catch { return dateStr }
}

function formatDateTime(dateStr: string | null | undefined) {
  if (!dateStr) return '—'
  try {
    const d = new Date(dateStr)
    return `${d.getHours().toString().padStart(2,'0')}:${d.getMinutes().toString().padStart(2,'0')}:${d.getSeconds().toString().padStart(2,'0')} ${d.getDate().toString().padStart(2,'0')}/${(d.getMonth()+1).toString().padStart(2,'0')}/${d.getFullYear()}`
  } catch { return dateStr }
}

function isOverdue(dateStr: string | null | undefined) {
  if (!dateStr) return false
  return new Date(dateStr) < new Date()
}

function statusClass(trangThai: number) {
  const map: Record<number, string> = {
    0: 'bg-red-50 text-red-700',
    1: 'bg-sky-50 text-sky-700',
    2: 'bg-yellow-50 text-yellow-700',
    3: 'bg-blue-50 text-blue-700',
    4: 'bg-green-50 text-green-700',
    5: 'bg-purple-50 text-purple-700',
    6: 'bg-orange-50 text-orange-700',
  }
  return map[trangThai] ?? 'bg-gray-50 text-gray-500'
}

function priorityLabel(p: number) {
  return p === 2 ? 'Khẩn cấp' : p === 1 ? 'Ưu tiên' : 'Bình thường'
}

onMounted(async () => {
  try {
    const res = await quayApi.getAll()
    if (res.data.success) allCounters.value = res.data.data
  } catch { /* ignore */ }
  loadAll()
})
</script>
