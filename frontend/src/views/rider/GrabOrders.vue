<template>
  <div class="page">
    <h1>抢单大厅</h1>
    
    <el-card>
      <div v-if="orders.length === 0" class="empty">
        <el-empty description="暂无可抢订单" />
      </div>
      <div v-else class="order-list">
        <div class="order-item" v-for="order in orders" :key="order.id">
          <div class="order-info">
            <div class="order-header">
              <span class="order-no">订单 #{{ order.orderId }}</span>
              <span class="order-fee">¥{{ order.fee }}</span>
            </div>
            <div class="order-detail">
              <p>距离: {{ order.distance }} km</p>
              <p>过期时间: {{ formatExpireTime(order.expireTime) }}</p>
            </div>
          </div>
          <div class="order-actions">
            <el-button type="primary" @click="handleGrab(order.id)">抢单</el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { getGrabList, grabOrder } from '@/api/rider'
import { ElMessage } from 'element-plus'

const orders = ref([])
let refreshTimer = null

const loadOrders = async () => {
  try {
    const res = await getGrabList()
    orders.value = res || []
  } catch (e) {
    console.error('加载抢单列表失败', e)
  }
}

const handleGrab = async (orderId) => {
  try {
    await grabOrder(orderId)
    ElMessage.success('抢单成功')
    loadOrders()
  } catch (e) {
    const message = e.response?.data?.message || e.message || '抢单失败'
    ElMessage.error(message)
  }
}

const formatExpireTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

onMounted(() => {
  loadOrders()
  refreshTimer = setInterval(loadOrders, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.empty {
  padding: 40px 0;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.order-no {
  font-size: 16px;
  font-weight: 500;
}

.order-fee {
  font-size: 18px;
  font-weight: 600;
  color: var(--primary);
}

.order-detail p {
  margin: 4px 0;
  font-size: 14px;
  color: #666;
}

.order-actions {
  flex-shrink: 0;
}
</style>
