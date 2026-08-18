import { ref } from 'vue'
import { useUserStore } from '@/stores/user'

const WS_URL = import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws/rider'

export function useRiderWebSocket() {
  const userStore = useUserStore()
  let ws = null
  let heartbeatTimer = null
  const listeners = ref({})

  const connect = () => {
    if (ws && ws.readyState === WebSocket.OPEN) return

    const token = localStorage.getItem('token')
    if (!token) return

    ws = new WebSocket(`${WS_URL}?token=${token}`)

    ws.onopen = () => {
      console.log('WebSocket 连接成功')
      startHeartbeat()
    }

    ws.onmessage = (event) => {
      try {
        const message = JSON.parse(event.data)
        const type = message.type
        if (listeners.value[type]) {
          listeners.value[type].forEach(cb => cb(message.data))
        }
      } catch (e) {
        console.error('解析 WebSocket 消息失败', e)
      }
    }

    ws.onclose = () => {
      console.log('WebSocket 连接关闭')
      stopHeartbeat()
      setTimeout(connect, 5000)
    }

    ws.onerror = (error) => {
      console.error('WebSocket 错误', error)
    }
  }

  const disconnect = () => {
    if (ws) {
      ws.close()
      ws = null
    }
    stopHeartbeat()
  }

  const startHeartbeat = () => {
    stopHeartbeat()
    heartbeatTimer = setInterval(() => {
      if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({ type: 'heartbeat' }))
      }
    }, 30000)
  }

  const stopHeartbeat = () => {
    if (heartbeatTimer) {
      clearInterval(heartbeatTimer)
      heartbeatTimer = null
    }
  }

  const on = (type, callback) => {
    if (!listeners.value[type]) {
      listeners.value[type] = []
    }
    listeners.value[type].push(callback)
  }

  const off = (type, callback) => {
    if (listeners.value[type]) {
      listeners.value[type] = listeners.value[type].filter(cb => cb !== callback)
    }
  }

  const send = (type, data) => {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({ type, ...data }))
    }
  }

  return { connect, disconnect, on, off, send }
}
