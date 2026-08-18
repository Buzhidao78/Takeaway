import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { cartList } from '@/api/cart'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])

  const totalCount = computed(() => {
    if (!Array.isArray(items.value)) return 0
    return items.value.reduce((s, i) => s + (i.quantity || 0), 0)
  })
  
  const totalByStore = computed(() => {
    const m = {}
    if (Array.isArray(items.value)) {
      items.value.forEach(i => {
        if (!m[i.storeId]) m[i.storeId] = 0
        m[i.storeId] += (i.quantity || 0)
      })
    }
    return m
  })

  async function fetchCart() {
    try {
      const token = localStorage.getItem('token')
      if (!token) {
        items.value = []
        return
      }
      const res = await cartList()
      console.log('=== 购物车 API 响应 ===')
      console.log('原始响应 res:', res)
      console.log('res 的类型:', typeof res)
      console.log('res 是否为数组:', Array.isArray(res))
      console.log('res 的内容:', JSON.stringify(res, null, 2))
      
      // res 已经是解析后的 data 部分
      items.value = Array.isArray(res) ? res : []
      
      console.log('=== 购物车数据设置完成 ===')
      console.log('items.value:', items.value)
      console.log('items.value 长度:', items.value.length)
      console.log('totalCount:', totalCount.value)
      console.log('totalByStore:', totalByStore.value)
    } catch (e) {
      console.warn('加载购物车失败:', e)
      items.value = []
    }
  }

  function clear() {
    items.value = []
  }

  return { items, totalCount, totalByStore, fetchCart, clear }
})
