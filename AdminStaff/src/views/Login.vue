<template>
  <div class="min-h-screen flex flex-col items-center justify-end pb-8 p-4 relative">
    <!-- Background Image -->
    <div class="absolute inset-0 z-0">
      <img 
        src="@/assets/image/bannereee.png" 
        alt="Background" 
        class="w-full h-full object-cover object-top"
      />
      <!-- Light overlay for subtle contrast, keeping background text visible -->
      <div class="absolute inset-0 bg-black/10"></div>
    </div>

    <!-- Login Container with Layered Design -->
    <div class="relative z-10 w-full max-w-[450px]">
      
      <!-- Back Layer (Decoration) -->
      <div class="absolute inset-0 bg-[#0f172a] rounded-[24px] translate-x-3 translate-y-3"></div>

      <!-- Front Layer (Content) -->
      <div class="relative bg-white rounded-[24px] p-8 md:p-10 shadow-xl overflow-hidden">
        
        <!-- Header -->
        <h2 class="text-center text-2xl font-bold text-gray-800 mb-8 hidden">Đăng nhập</h2>
        
        <form @submit.prevent="handleLogin" class="space-y-6">
          <!-- Username -->
          <div>
            <label for="username" class="block text-sm font-semibold text-gray-700 mb-2">
              Tên đăng nhập
            </label>
            <div class="relative">
              <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400">
                <User class="h-5 w-5" />
              </span>
              <input
                id="username"
                v-model="form.username"
                type="text"
                required
                placeholder="Nhập tên đăng nhập"
                class="w-full pl-11 pr-4 py-3.5 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-600 focus:border-blue-600 transition-all outline-none"
              />
            </div>
          </div>

          <!-- Password -->
          <div>
            <label for="password" class="block text-sm font-semibold text-gray-700 mb-2">
              Mật khẩu
            </label>
            <div class="relative">
              <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400">
                <Lock class="h-5 w-5" />
              </span>
              <input
                id="password"
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                required
                placeholder="Nhập mật khẩu"
                class="w-full pl-11 pr-12 py-3.5 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-600 focus:border-blue-600 transition-all outline-none"
              />
              <button
                type="button"
                @click="showPassword = !showPassword"
                class="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
              >
                <EyeOff v-if="showPassword" class="h-5 w-5" />
                <Eye v-else class="h-5 w-5" />
              </button>
            </div>
          </div>

          <!-- Remember & Forgot Password -->
          <div class="flex items-center justify-between pt-1">
            <label class="flex items-center cursor-pointer">
              <input
                v-model="form.remember"
                type="checkbox"
                class="w-5 h-5 text-blue-600 border-gray-300 rounded focus:ring-blue-500 rounded-md"
              />
              <span class="ml-2 text-sm text-gray-600">Ghi nhớ đăng nhập</span>
            </label>
            <a href="#" class="text-sm text-blue-600 hover:text-blue-800 font-semibold">
              Quên mật khẩu?
            </a>
          </div>

          <!-- Error Message -->
          <div v-if="authStore.error" class="bg-red-50 border-l-4 border-red-500 p-4 rounded-r mt-4">
            <div class="flex">
              <AlertCircle class="h-5 w-5 text-red-500" />
              <p class="ml-3 text-sm text-red-700">{{ authStore.error }}</p>
            </div>
          </div>

          <!-- Login Button -->
          <button
            type="submit"
            :disabled="authStore.loading"
            class="w-full py-4 px-6 bg-[#4f46e5] text-white font-bold rounded-xl shadow-lg hover:bg-[#4338ca] focus:ring-4 focus:ring-indigo-500/30 transition-all transform active:scale-[0.98] mt-2 flex items-center justify-center gap-2 text-base disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <Loader2 v-if="authStore.loading" class="h-5 w-5 animate-spin" />
            <span v-else class="flex items-center gap-2">
              <LogIn class="h-5 w-5" />
              Đăng nhập
            </span>
          </button>
        </form>



      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Eye, EyeOff, LogIn, AlertCircle, Loader2 } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()  // Lấy Pinia store

// Form state
const form = reactive({
  username: '',
  password: '',
  remember: false
})

const showPassword = ref(false)

// Sử dụng loading và error từ store (reactive)
// Hoặc có thể dùng local state nếu muốn

/**
 * Xử lý đăng nhập
 * 
 * Flow:
 * 1. Gọi authStore.login() với credentials
 * 2. Store sẽ gọi API và lưu user/token
 * 3. Nếu thành công → redirect dựa theo role
 * 4. Nếu thất bại → hiển thị error từ store
 */
const handleLogin = async () => {
  // Xóa error cũ
  authStore.clearError()

  // Gọi login từ store
  const success = await authStore.login({
    maNhanVien: form.username,
    password: form.password
  })

  if (success) {
    // Redirect dựa theo role
    if (authStore.isAdmin) {
      router.push('/admin/dashboard')
    } else {
      router.push('/staff/dashboard')
    }
  }
  // Nếu không success, error đã được set trong store
}



/**
 * Kiểm tra nếu đã login thì redirect
 */
onMounted(() => {
  if (authStore.isAuthenticated) {
    if (authStore.isAdmin) {
      router.push('/admin/dashboard')
    } else {
      router.push('/staff/dashboard')
    }
  }
})
</script>

<style scoped>
/* Optional: Add custom font or animation enhancements here */
</style>
