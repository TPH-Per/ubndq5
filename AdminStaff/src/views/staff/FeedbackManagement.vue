<template>
  <div class="space-y-6 animate-in fade-in zoom-in duration-300">
    <!-- Header Card -->
    <Card class="bg-gradient-to-r from-blue-50 to-white border-blue-100">
       <CardHeader>
          <div class="flex justify-between items-center">
             <div>
                <CardTitle class="text-blue-900">Tiếp nhận phản ánh</CardTitle>
                <p class="text-blue-600/80 mt-1">Quản lý và giải đáp các ý kiến góp ý của người dân</p>
             </div>
             <Button variant="outline" size="icon" @click="fetchList" :class="{ 'animate-spin': loading }">
                <RefreshCw class="w-4 h-4" />
             </Button>
          </div>
       </CardHeader>
    </Card>

    <!-- Main Content with DataTable -->
    <Card>
       <div class="border-b p-4 text-xs">
          <div class="flex gap-2">
            <Button
              v-for="tab in tabs"
              :key="tab.value"
              @click="currentTab = tab.value"
              :variant="currentTab === tab.value ? 'default' : 'ghost'"
              size="sm"
              class="rounded-full"
            >
              {{ tab.label }}
            </Button>
          </div>
       </div>
       
       <CardContent class="p-6">
          <DataTable 
             :columns="columns" 
             :data="feedbacks" 
             search-placeholder="Tìm kiếm theo tên, tiêu đề..."
          />
       </CardContent>
    </Card>

    <!-- Detail Modal -->
    <div v-if="selectedFeedback" class="fixed inset-0 bg-black/80 z-50 flex items-center justify-center p-4 backdrop-blur-sm animate-in fade-in duration-200" @click.self="selectedFeedback = null">
       <Card class="w-full max-w-2xl max-h-[90vh] flex flex-col overflow-hidden shadow-2xl animate-in zoom-in-95 duration-200 border-none bg-white">
          <div class="p-6 border-b flex justify-between items-center bg-gray-50/50">
             <div>
                <h3 class="font-bold text-lg">Chi tiết phản ánh #{{ selectedFeedback.id }}</h3>
                <p class="text-sm text-muted-foreground">Xem chi tiết và phản hồi công dân</p>
             </div>
             <Button variant="ghost" size="icon" @click="selectedFeedback = null" class="rounded-full">
                <X class="w-5 h-5" />
             </Button>
          </div>
          
          <div class="p-6 overflow-y-auto space-y-6 flex-1 bg-white">
              <!-- Citizen Info -->
              <div class="flex items-start gap-4 p-4 bg-blue-50/50 rounded-xl border border-blue-100">
                 <div class="w-12 h-12 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 font-bold text-lg shadow-sm border-2 border-white">
                    {{ selectedFeedback.citizenName.charAt(0) }}
                 </div>
                 <div class="flex-1">
                    <h4 class="font-bold text-gray-900">{{ selectedFeedback.citizenName }}</h4>
                    <p class="text-sm text-gray-500 flex items-center gap-2">
                        <CreditCard class="w-3 h-3" /> CCCD: {{ selectedFeedback.citizenId }}
                    </p>
                    <div v-if="selectedFeedback.applicationCode" class="mt-2 text-xs">
                       <span class="bg-white px-2 py-1 rounded border shadow-sm">
                          Hồ sơ: <span class="font-mono font-medium text-blue-600">{{ selectedFeedback.applicationCode }}</span>
                       </span>
                    </div>
                 </div>
                 <div class="text-right">
                    <Badge :variant="getStatusVariant(selectedFeedback.status)" class="mb-2">
                        {{ getStatusLabel(selectedFeedback.status) }}
                    </Badge>
                    <p class="text-xs text-muted-foreground">{{ formatDate(selectedFeedback.createdAt) }}</p>
                 </div>
              </div>

              <!-- Content -->
              <div class="space-y-2">
                  <h5 class="text-sm font-semibold flex items-center gap-2">
                      <FileText class="w-4 h-4 text-gray-500" /> Nội dung phản ánh
                  </h5>
                  <div class="bg-gray-50 p-4 rounded-xl border border-gray-100 text-sm leading-relaxed">
                      <p class="font-medium text-gray-900 mb-1">{{ selectedFeedback.title }}</p>
                      <p class="text-gray-700 whitespace-pre-line">{{ selectedFeedback.content }}</p>
                  </div>
              </div>

              <!-- Replies -->
              <div v-if="selectedFeedback.replies && selectedFeedback.replies.length > 0" class="space-y-4">
                 <h5 class="text-sm font-semibold flex items-center gap-2">
                    <MessageSquare class="w-4 h-4 text-blue-500" /> Lịch sử phản hồi
                 </h5>
                 <div class="space-y-4 pl-2">
                    <div v-for="reply in selectedFeedback.replies" :key="reply.id" class="relative pl-6 pb-2 border-l-2 border-blue-100 last:border-0 last:pb-0">
                       <div class="absolute -left-[5px] top-0 h-2.5 w-2.5 rounded-full bg-blue-400 ring-4 ring-white"></div>
                       <div class="bg-blue-50/50 p-3 rounded-lg border border-blue-100/50 ml-2">
                           <div class="flex justify-between items-start mb-1">
                              <span class="font-semibold text-sm text-blue-900">{{ reply.staffName }}</span>
                              <span class="text-xs text-gray-400">{{ formatDate(reply.createdAt) }}</span>
                           </div>
                           <p class="text-sm text-gray-700">{{ reply.content }}</p>
                       </div>
                    </div>
                 </div>
              </div>

              <!-- Reply Form -->
               <div class="pt-4 border-t">
                  <div class="flex items-center justify-between mb-2">
                    <label class="block text-sm font-medium">Thêm câu trả lời</label>
                    <!-- Template picker -->
                    <div class="relative" ref="templatePickerRef">
                      <button
                        type="button"
                        @click="showTemplatePicker = !showTemplatePicker"
                        class="flex items-center gap-1.5 px-3 py-1 text-xs font-medium text-blue-600 bg-blue-50 hover:bg-blue-100 border border-blue-200 rounded-lg transition-colors"
                      >
                        <BookOpen class="w-3.5 h-3.5" />
                        Dùng mẫu có sẵn
                        <ChevronDown class="w-3 h-3" :class="{ 'rotate-180': showTemplatePicker }" />
                      </button>
                      <div
                        v-if="showTemplatePicker"
                        class="absolute right-0 top-full mt-1 w-80 bg-white border border-gray-200 rounded-xl shadow-xl z-30 overflow-hidden"
                      >
                        <div class="p-2 border-b bg-gray-50">
                          <p class="text-xs font-semibold text-gray-500 uppercase tracking-wide px-2">Chọn mẫu phản hồi</p>
                        </div>
                        <div class="max-h-64 overflow-y-auto divide-y divide-gray-50">
                          <div v-if="defaultReplies.length === 0" class="p-4 text-center text-sm text-gray-400">
                            Chưa có mẫu nào. Vào <strong>Phản hồi mặc định</strong> để thêm.
                          </div>
                          <button
                            v-for="tmpl in defaultReplies"
                            :key="tmpl.id"
                            type="button"
                            @click="applyTemplate(tmpl.content)"
                            class="w-full text-left px-4 py-3 hover:bg-blue-50 transition-colors group"
                          >
                            <div class="flex items-center gap-2 mb-0.5">
                              <span class="text-xs px-1.5 py-0.5 rounded-full bg-blue-50 text-blue-600 border border-blue-100 font-medium">{{ tmpl.category }}</span>
                            </div>
                            <p class="text-sm font-medium text-gray-800 group-hover:text-blue-700">{{ tmpl.title }}</p>
                            <p class="text-xs text-gray-400 mt-0.5 line-clamp-2">{{ tmpl.content }}</p>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <textarea 
                    v-model="replyContent" 
                    rows="3" 
                    class="w-full rounded-lg border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 border shadow-sm resize-none"
                    placeholder="Nhập nội dung phản hồi..."
                  ></textarea>
               </div>
          </div>
          
          <div class="p-4 bg-gray-50 border-t flex justify-end gap-3">
             <Button variant="outline" @click="selectedFeedback = null">Đóng</Button>
             <Button 
                @click="submitReply" 
                :disabled="!replyContent.trim() || submitting"
                class="min-w-[100px]"
             >
                <Loader2 v-if="submitting" class="w-4 h-4 mr-2 animate-spin" />
                {{ submitting ? 'Đang gửi...' : 'Gửi trả lời' }}
             </Button>
          </div>
       </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, h, onBeforeUnmount } from 'vue'
import { feedbackApi, type Feedback } from '@/services/api'
import { RefreshCw, Eye, X, MessageSquare, FileText, CreditCard, Loader2, BookOpen, ChevronDown } from 'lucide-vue-next'
import { useDefaultReplies } from '@/lib/useDefaultReplies'
import { toast } from 'vue-sonner'
import Card from '@/components/ui/card/Card.vue'
import CardHeader from '@/components/ui/card/CardHeader.vue'
import CardTitle from '@/components/ui/card/CardTitle.vue'
import CardContent from '@/components/ui/card/CardContent.vue'
import Button from '@/components/ui/button/Button.vue'
import Badge from '@/components/ui/badge/Badge.vue'
import DataTable from '@/components/ui/data-table/DataTable.vue'
import type { ColumnDef } from '@tanstack/vue-table'

const loading = ref(false)
const submitting = ref(false)
const feedbacks = ref<Feedback[]>([])
const currentTab = ref<number | undefined>(undefined)
const selectedFeedback = ref<Feedback | null>(null)
const replyContent = ref('')

// Default reply templates
const { replies: defaultReplies } = useDefaultReplies()
const showTemplatePicker = ref(false)
const templatePickerRef = ref<HTMLElement | null>(null)

function applyTemplate(content: string) {
  replyContent.value = content
  showTemplatePicker.value = false
}

function handleOutsideClick(e: MouseEvent) {
  if (templatePickerRef.value && !templatePickerRef.value.contains(e.target as Node)) {
    showTemplatePicker.value = false
  }
}

onMounted(() => {
  document.addEventListener('mousedown', handleOutsideClick)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleOutsideClick)
})

const tabs = [
   { label: 'Tất cả', value: undefined },
]

// Table Columns Definition
const columns: ColumnDef<Feedback>[] = [
  {
    accessorKey: 'id',
    header: 'ID',
    cell: ({ row }) => h('div', { class: 'font-mono font-medium' }, `#${row.getValue('id')}`),
  },
  {
    accessorKey: 'citizenName',
    header: 'Người gửi',
    cell: ({ row }) => h('div', [
        h('div', { class: 'font-medium' }, row.original.citizenName),
        h('div', { class: 'text-xs text-muted-foreground' }, row.original.citizenId)
    ]),
  },
  {
     accessorKey: 'title',
     header: 'Tiêu đề',
     cell: ({ row }) => h('div', { class: 'truncate max-w-[200px] font-medium', title: row.getValue('title') }, row.getValue('title') || 'Không tiêu đề')
  },
  {
     accessorKey: 'type',
     header: 'Loại',
     cell: ({ row }) => {
        const type = row.getValue('type') as number
        return h(Badge, { 
            variant: getTypeVariant(type),
            class: type === 1 ? 'bg-red-600 hover:bg-red-700 text-white border-red-600' : '' 
        }, () => getTypeLabel(type))
     }
  },

  {
     accessorKey: 'createdAt',
     header: 'Ngày gửi',
     cell: ({ row }) => h('span', { class: 'text-muted-foreground whitespace-nowrap' }, formatDate(row.getValue('createdAt')))
  },
  {
     id: 'actions',
     header: () => h('div', { class: 'text-right' }, 'Thao tác'),
     cell: ({ row }) => {
        return h('div', { class: 'flex justify-end' }, 
            h(Button, { 
                variant: 'outline', 
                size: 'sm',
                class: 'bg-blue-50/50 text-blue-600 border-blue-200 hover:bg-blue-600 hover:text-white hover:border-blue-600 shadow-sm transition-all duration-200 font-medium px-3',
                onClick: () => openDetail(row.original)
            }, () => [
                h(Eye, { class: 'h-4 w-4 mr-2' }),
                'Chi tiết'
            ])
        )
     }
  }
]

const fetchList = async () => {
    loading.value = true
    try {
        const res = await feedbackApi.getList(currentTab.value)
        if (res.data.success) {
            feedbacks.value = res.data.data || []
        }
    } catch (e) {
        toast.error('Không thể tải danh sách phản ánh')
    } finally {
        loading.value = false
    }
}

const openDetail = (item: Feedback) => {
    selectedFeedback.value = item
    replyContent.value = ''
}

const submitReply = async () => {
    if (!selectedFeedback.value || !replyContent.value.trim()) return
    
    submitting.value = true
    try {
        const res = await feedbackApi.reply(selectedFeedback.value.id, replyContent.value)
        if (res.data.success) {
            toast.success('Gửi trả lời thành công')
            // Update UI
            const updated = res.data.data
            // Update list
            const idx = feedbacks.value.findIndex(f => f.id === updated.id)
            if (idx !== -1) feedbacks.value[idx] = updated
            
            // Update modal
            selectedFeedback.value = updated
            replyContent.value = ''
            
            // Refresh list if filtering by NEW
            if (currentTab.value === 0) fetchList()
        }
    } catch (e) {
        toast.error('Gửi trả lời thất bại')
    } finally {
        submitting.value = false
    }
}

watch(currentTab, () => {
    fetchList()
})

onMounted(() => {
    fetchList()
})

// Helpers
const formatDate = (str: string) => {
    if (!str) return ''
    const d = new Date(str)
    return `${d.getHours().toString().padStart(2,'0')}:${d.getMinutes().toString().padStart(2,'0')}:${d.getSeconds().toString().padStart(2,'0')} ${d.getDate().toString().padStart(2,'0')}/${(d.getMonth() + 1).toString().padStart(2,'0')}/${d.getFullYear()}`
}

const getTypeLabel = (type: number) => {
    switch(type) {
        case 0: return 'Góp ý'
        case 1: return 'Khiếu nại'
        case 2: return 'Khen ngợi'
        default: return 'Khác'
    }
}

const getTypeVariant = (type: number) => {
     switch(type) {
        case 0: return 'info'
        case 1: return 'destructive' // Red
        case 2: return 'success'
        default: return 'secondary'
    }
}

const getStatusLabel = (status: number) => {
    switch(status) {
        case 0: return 'Mới'
        case 1: return 'Đang xử lý'
        case 2: return 'Đã giải quyết'
        default: return ''
    }
}

const getStatusVariant = (status: number) => {
    switch(status) {
        case 0: return 'info'
        case 1: return 'warning'
        case 2: return 'success'
        default: return 'secondary'
    }
}
</script>
