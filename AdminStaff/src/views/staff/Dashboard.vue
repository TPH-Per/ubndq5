<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex justify-between items-center">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Tổng quan</h1>
        <p class="text-sm text-gray-500 mt-1">Xem tình hình hoạt động hôm nay</p>
      </div>
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

    <!-- Quick Actions & Recent Activity -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Current Queue -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
        <h3 class="font-bold text-lg mb-4 flex items-center gap-2">
          <ListOrdered class="h-5 w-5 text-blue-600" />
          Hàng chờ hiện tại
        </h3>
        <div class="space-y-3">
          <div 
            v-for="item in queueItems" 
            :key="item.number"
            class="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
          >
            <div class="flex items-center gap-3">
              <span class="font-mono font-bold text-lg text-blue-600">{{ item.number }}</span>
              <div>
                <p class="font-medium text-gray-900">{{ item.name }}</p>
                <p class="text-xs text-gray-500">{{ item.service }}</p>
              </div>
            </div>
            <span :class="['px-2 py-1 rounded text-xs font-medium', item.statusClass]">
              {{ item.status }}
            </span>
          </div>
        </div>
      </div>

      <!-- Today's Activity Chart -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
        <h3 class="font-bold text-lg mb-4 flex items-center gap-2">
          <BarChart3 class="h-5 w-5 text-blue-600" />
          Lượt phục vụ theo giờ
        </h3>
        <div class="flex items-end justify-between h-48 gap-2">
          <div 
            v-for="(hour, idx) in chartData" 
            :key="idx"
            class="flex-1 flex flex-col items-center gap-2"
          >
            <div 
              class="w-full bg-blue-500 rounded-t transition-all duration-300 hover:bg-blue-600"
              :style="{ height: `${(hour.value / maxChartValue) * 100}%` }"
            ></div>
            <span class="text-xs text-gray-500">{{ hour.label }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { 
  Users, 
  Clock, 
  CheckCircle, 
  TrendingUp, 
  ListOrdered,
  BarChart3
} from 'lucide-vue-next'

// Stats data
const stats = [
  { 
    label: 'Khách hôm nay', 
    value: '48', 
    icon: Users, 
    color: 'bg-blue-500', 
    trend: '+12%' 
  },
  { 
    label: 'Thời gian chờ TB', 
    value: '8 phút', 
    icon: Clock, 
    color: 'bg-orange-500', 
    trend: '-2 phút' 
  },
  { 
    label: 'Đã phục vụ', 
    value: '35', 
    icon: CheckCircle, 
    color: 'bg-green-500', 
    trend: '+8' 
  },
  { 
    label: 'Đang chờ', 
    value: '13', 
    icon: TrendingUp, 
    color: 'bg-purple-500', 
    trend: 'Stable' 
  },
]

// Queue items
const queueItems = [
  { number: 'A-015', name: 'Nguyễn Văn A', service: 'Đăng ký kết hôn', status: 'Đang xử lý', statusClass: 'bg-blue-100 text-blue-700' },
  { number: 'A-016', name: 'Trần Thị B', service: 'Sao y công chứng', status: 'Chờ gọi', statusClass: 'bg-yellow-100 text-yellow-700' },
  { number: 'A-017', name: 'Lê Văn C', service: 'Đất đai', status: 'Chờ gọi', statusClass: 'bg-yellow-100 text-yellow-700' },
]

// Chart data
const chartData = [
  { label: '8h', value: 10 },
  { label: '9h', value: 25 },
  { label: '10h', value: 45 },
  { label: '11h', value: 30 },
  { label: '12h', value: 15 },
  { label: '13h', value: 35 },
  { label: '14h', value: 50 },
  { label: '15h', value: 28 },
]

const maxChartValue = computed(() => Math.max(...chartData.map(d => d.value)))
</script>
