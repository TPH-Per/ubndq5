import { ref } from 'vue'
import seedData from '@/data/defaultReplies.json'

export interface DefaultReply {
  id: number
  title: string
  content: string
  category: string
}

const STORAGE_KEY = 'default_replies_v1'

function loadFromStorage(): DefaultReply[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) return JSON.parse(raw) as DefaultReply[]
  } catch {
    // fall through to seed
  }
  // First run: seed from bundled JSON
  localStorage.setItem(STORAGE_KEY, JSON.stringify(seedData))
  return seedData as DefaultReply[]
}

function persist(list: DefaultReply[]) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(list))
}

export function useDefaultReplies() {
  const replies = ref<DefaultReply[]>(loadFromStorage())

  function add(item: Omit<DefaultReply, 'id'>) {
    const newId =
      replies.value.length > 0
        ? Math.max(...replies.value.map((r) => r.id)) + 1
        : 1
    replies.value = [...replies.value, { ...item, id: newId }]
    persist(replies.value)
  }

  function update(updated: DefaultReply) {
    replies.value = replies.value.map((r) => (r.id === updated.id ? updated : r))
    persist(replies.value)
  }

  function remove(id: number) {
    replies.value = replies.value.filter((r) => r.id !== id)
    persist(replies.value)
  }

  function resetToDefaults() {
    replies.value = seedData as DefaultReply[]
    persist(replies.value)
  }

  return { replies, add, update, remove, resetToDefaults }
}
