<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex justify-between items-center">
      <h1 class="text-2xl font-bold text-gray-900">Tổng quan hệ thống</h1>
      <button class="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 transition-colors">
        Xuất báo cáo
      </button>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      <div 
        v-for="(stat, idx) in stats" 
        :key="idx"
        class="bg-white p-6 rounded-xl shadow-sm border border-gray-100"
      >
        <div class="flex justify-between items-start mb-4">
          <div :class="['p-3 rounded-lg', stat.color]">
            <component :is="stat.icon" class="h-6 w-6 text-white" />
          </div>
          <span class="flex items-center text-green-600 text-xs font-medium bg-green-50 px-2 py-1 rounded-full">
            <TrendingUp class="h-3 w-3 mr-1" /> {{ stat.trend }}
          </span>
        </div>
        <h3 class="text-3xl font-bold text-gray-900 mb-1">{{ stat.value }}</h3>
        <p class="text-sm text-gray-500">{{ stat.label }}</p>
      </div>
    </div>

    <!-- Activity & System Status -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Recent Activity -->
      <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
        <h3 class="font-bold text-lg mb-4">Hoạt động gần đây</h3>
        <div class="space-y-4">
          <div 
            v-for="(activity, i) in recentActivities" 
            :key="i"
            class="flex items-center gap-4 pb-4 border-b border-gray-50 last:border-0 last:pb-0"
          >
            <div class="h-10 w-10 rounded-full bg-gray-100 flex items-center justify-center text-gray-500 font-bold text-xs">
              {{ activity.avatar }}
            </div>
            <div>
              <p class="text-sm font-medium text-gray-900" v-html="activity.text"></p>
              <p class="text-xs text-gray-400">{{ activity.time }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- System Health -->
      <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
        <h3 class="font-bold text-lg mb-4">Trạng thái hệ thống</h3>
        <div class="space-y-3">
          <div class="flex items-center justify-between p-3 bg-green-50 text-green-700 rounded-lg">
            <span class="text-sm font-medium flex items-center gap-2">
              <div class="h-2 w-2 rounded-full bg-green-500 animate-pulse"></div>
              Máy chủ hoạt động tốt
            </span>
            <span class="text-xs font-bold">99.9% Uptime</span>
          </div>
          <div class="flex items-center justify-between p-3 bg-yellow-50 text-yellow-700 rounded-lg">
            <span class="text-sm font-medium flex items-center gap-2">
              <div class="h-2 w-2 rounded-full bg-yellow-500"></div>
              Cảnh báo tải cao
            </span>
            <span class="text-xs font-bold">11:00 AM</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Users, Building2, MessageSquare, Activity, TrendingUp } from 'lucide-vue-next'

// Stats data
const stats = [
  { label: 'Tổng tài khoản', value: '12', icon: Users, color: 'bg-blue-500', trend: '+2 tuần này' },
  { label: 'Tổng số quầy', value: '8', icon: Building2, color: 'bg-indigo-500', trend: 'Ổn định' },
  { label: 'Phản ánh mới', value: '5', icon: MessageSquare, color: 'bg-orange-500', trend: '+3 hôm nay' },
  { label: 'Lượt phục vụ', value: '128', icon: Activity, color: 'bg-green-500', trend: '+12% vs hôm qua' },
]

// Recent activities
const recentActivities = [
  { avatar: 'AD', text: 'Admin đã cập nhật thông tin <span class="font-bold">Quầy A</span>', time: '2 giờ trước' },
  { avatar: 'NV', text: 'Nhân viên Nguyễn Văn B đã đăng nhập', time: '3 giờ trước' },
  { avatar: 'AD', text: 'Admin đã thêm tài khoản mới', time: '5 giờ trước' },
  { avatar: 'SY', text: 'Hệ thống đã backup dữ liệu', time: '1 ngày trước' },
]
</script>
