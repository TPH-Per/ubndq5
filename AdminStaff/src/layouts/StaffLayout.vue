<template>
  <div class="min-h-screen bg-gray-100 flex">
    <!-- Sidebar -->
    <aside class="w-72 bg-[#003366] text-white fixed h-full z-20 hidden md:flex flex-col shadow-xl">
      <!-- Logo Area -->
      <div class="p-6 pb-4">
        <div class="flex items-center gap-3 mb-6">
          <div class="h-10 w-10 bg-white rounded-xl flex items-center justify-center text-[#003366] font-bold shadow-lg overflow-hidden p-1">
            <img 
              src="https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/Emblem_of_Vietnam.svg/2048px-Emblem_of_Vietnam.svg.png" 
              alt="Logo" 
              class="h-full w-full object-contain" 
            />
          </div>
          <div>
            <h1 class="font-bold text-base uppercase tracking-wide">Phú Thọ</h1>
            <p class="text-xs text-blue-200">Hệ thống Một cửa</p>
          </div>
        </div>

        <!-- Counter Info Widget -->
        <div class="bg-white/10 rounded-xl p-4 border border-white/10 backdrop-blur-sm shadow-inner">
          <div class="flex items-start gap-3">
            <div class="p-2 bg-blue-500/20 rounded-lg shrink-0">
              <Building2 class="h-5 w-5 text-blue-300" />
            </div>
            <div>
              <p class="text-[10px] text-blue-200 font-bold uppercase tracking-wider mb-1">Đang làm việc tại</p>
              <p class="font-bold text-white text-lg leading-tight">{{ staffInfo.counter }}</p>
              <div class="flex items-center gap-1.5 mt-2">
                <span class="relative flex h-2 w-2">
                  <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
                  <span class="relative inline-flex rounded-full h-2 w-2 bg-green-500"></span>
                </span>
                <span class="text-[10px] text-green-300 font-medium">Đang hoạt động</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Navigation -->
      <nav class="flex-1 px-4 space-y-1.5 overflow-y-auto py-4">
        <p class="px-4 text-[10px] font-bold text-blue-300/50 uppercase tracking-wider mb-2">Menu chính</p>
        <RouterLink
          v-for="item in sidebarItems"
          :key="item.path"
          :to="item.path"
          class="nav-link flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-all duration-200 group"
          :class="[
            $route.path === item.path 
              ? 'bg-white text-[#003366] shadow-md translate-x-1' 
              : 'text-blue-100 hover:bg-white/10 hover:text-white hover:translate-x-1'
          ]"
        >
          <component 
            :is="item.icon" 
            class="h-5 w-5 transition-colors"
            :class="$route.path === item.path ? 'text-[#003366]' : 'text-blue-300 group-hover:text-white'"
          />
          {{ item.label }}
        </RouterLink>
      </nav>

      <!-- User Profile -->
      <div class="p-4 border-t border-white/10 bg-[#002b55]">
        <div class="flex items-center gap-3 mb-4 bg-black/20 p-3 rounded-xl border border-white/5">
          <div class="h-10 w-10 bg-gradient-to-br from-blue-400 to-blue-600 rounded-full flex items-center justify-center text-white font-bold border-2 border-white/20 shadow-inner">
            {{ staffInfo.avatar }}
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-sm font-bold text-white truncate">{{ staffInfo.name }}</p>
            <p class="text-xs text-blue-300 truncate">{{ staffInfo.role }}</p>
          </div>
        </div>
        <div class="flex gap-2">
          <button
            @click="showEditProfileModal = true"
            class="flex-1 flex items-center justify-center gap-2 px-4 py-2 bg-white/5 hover:bg-white/10 border border-white/10 rounded-lg text-xs font-medium text-blue-200 hover:text-white transition-colors group"
          >
            <UserCog class="h-4 w-4 group-hover:text-blue-300 transition-colors" />
            Sửa profile
          </button>
          <button
            @click="handleLogout"
            class="flex-1 flex items-center justify-center gap-2 px-4 py-2 bg-white/5 hover:bg-white/10 border border-white/10 rounded-lg text-xs font-medium text-blue-200 hover:text-white transition-colors group"
          >
            <LogOut class="h-4 w-4 group-hover:text-red-300 transition-colors" />
            Đăng xuất
          </button>
        </div>
      </div>
    </aside>

    <!-- Main Content -->
    <div class="flex-1 md:ml-72 flex flex-col min-h-screen transition-all duration-300">
      <!-- Top Header -->
      <header class="bg-white border-b border-gray-200 h-16 px-6 flex items-center justify-between sticky top-0 z-10 shadow-sm">
        <div class="flex items-center gap-4 flex-1">
          <div class="relative w-96 max-w-full">
            <Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
            <input 
              type="text" 
              placeholder="Tìm kiếm hồ sơ, công dân..."
              class="w-full pl-10 pr-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all"
            />
          </div>
        </div>

        <div class="flex items-center gap-4">
          <button class="relative p-2 text-gray-500 hover:bg-gray-100 rounded-full transition-colors">
            <Bell class="h-5 w-5" />
            <span class="absolute top-1.5 right-1.5 h-2 w-2 bg-red-500 rounded-full border-2 border-white"></span>
          </button>
        </div>
      </header>

      <!-- Page Content -->
      <main class="flex-1 p-6 overflow-y-auto bg-gray-50/50">
        <RouterView />
      </main>
    </div>

    <!-- Edit Profile Modal -->
    <EditProfileModal
      v-if="showEditProfileModal"
      @close="showEditProfileModal = false"
      @updated="handleProfileUpdated"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import {
  LayoutDashboard, 
  ListOrdered, 
  FolderOpen, 
  Settings, 
  Search, 
  Bell, 
  LogOut,
  Building2,
  UserCog
} from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import EditProfileModal from '@/components/shared/EditProfileModal.vue'

const authStore = useAuthStore()

// Modal state
const showEditProfileModal = ref(false)

// Sidebar navigation items
const sidebarItems = [
  { icon: LayoutDashboard, label: 'Tổng quan', path: '/staff/dashboard' },
  { icon: ListOrdered, label: 'Quản lý hàng chờ', path: '/staff/queue' },
  { icon: FolderOpen, label: 'Xử lý hồ sơ', path: '/staff/documents' },
  { icon: Settings, label: 'Cài đặt', path: '/staff/settings' },
]

// Lấy thông tin staff từ authStore (reactive)
const staffInfo = computed(() => ({
  name: authStore.user?.hoTen || 'Nhân viên',
  id: authStore.user?.maNhanVien || 'N/A',
  role: authStore.user?.roleDisplayName || 'Cán bộ Một cửa',
  avatar: authStore.user?.hoTen?.charAt(0).toUpperCase() || 'NV',
  counter: authStore.user?.tenQuay || 'Chưa phân quầy'
}))

/**
 * Handle logout
 * 
 * Flow:
 * 1. authStore.logout() xóa token/user khỏi store và localStorage
 * 2. authStore.logout() tự động redirect về /login
 */
const handleLogout = async () => {
  await authStore.logout()
}

/**
 * Handle profile updated
 * Refresh user info in store from localStorage
 */
const handleProfileUpdated = () => {
  const userJson = localStorage.getItem('user')
  if (userJson) {
    authStore.user = JSON.parse(userJson)
  }
}
</script>

<style scoped>
.nav-link {
  text-decoration: none;
}
</style>
