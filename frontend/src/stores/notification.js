import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useNotificationStore = defineStore('notification', () => {
  const unreadCount = ref(0)

  function setUnreadCount(count) {
    unreadCount.value = count
  }

  function incrementUnreadCount() {
    unreadCount.value++
  }

  function decrementUnreadCount() {
    if (unreadCount.value > 0) {
      unreadCount.value--
    }
  }

  function resetUnreadCount() {
    unreadCount.value = 0
  }

  return { unreadCount, setUnreadCount, incrementUnreadCount, decrementUnreadCount, resetUnreadCount }
})
