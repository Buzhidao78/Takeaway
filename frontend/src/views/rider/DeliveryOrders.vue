<template>
  <div class="page">
    <h1>配送订单</h1>
    
    <el-card>
      <el-tabs v-model="activeTab" @tab-change="loadOrders">
        <el-tab-pane label="配送中" name="delivering" />
        <el-tab-pane label="已完成" name="3" />
      </el-tabs>
      
      <div v-if="orders.length === 0" class="empty">
        <el-empty description="暂无配送订单" />
      </div>
      <div v-else class="order-list">
        <div class="order-item" v-for="order in orders" :key="order.id">
          <div class="order-info">
            <div class="order-header">
              <span class="order-no">订单 #{{ order.orderId }}</span>
              <el-tag :type="getStatusType(order.status)" size="small">{{ getStatusText(order.status) }}</el-tag>
            </div>
            <div class="order-detail">
              <p>配送费: ¥{{ order.fee }}</p>
              <p v-if="order.pickUpTime">取餐时间: {{ formatTime(order.pickUpTime) }}</p>
              <p v-if="order.deliveryTime">送达时间: {{ formatTime(order.deliveryTime) }}</p>
            </div>
          </div>
          <div class="order-actions">
            <el-button v-if="order.status === 1" type="primary" @click="handlePickup(order.id)">确认取餐</el-button>
            <el-button v-if="order.status === 2" type="success" @click="handleDeliver(order.id)">确认送达</el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { getDeliveryList, pickup, deliver } from '@/api/rider'
import { ElMessage } from 'element-plus'

const activeTab = ref('delivering')
const orders = ref([])
let refreshTimer = null

const loadOrders = async () => {
  try {
    const res = await getDeliveryList(activeTab.value)
    orders.value = res || []
    
    // 如果有配送中的订单，启动自动刷新
    const hasActiveOrders = orders.value.some(o => o.status === 1 || o.status === 2)
    if (hasActiveOrders && !refreshTimer) {
      startAutoRefresh()
    } else if (!hasActiveOrders && refreshTimer) {
      stopAutoRefresh()
    }
  } catch (e) {
    console.error('加载配送订单失败', e)
  }
}

function startAutoRefresh() {
  stopAutoRefresh()
  refreshTimer = setInterval(() => {
    loadOrders()
  }, 5000)
}

function stopAutoRefresh() {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

const handlePickup = async (id) => {
  try {
    await pickup(id)
    ElMessage.success('已确认取餐')
    loadOrders()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleDeliver = async (id) => {
  try {
    await deliver(id)
    ElMessage.success('已确认送达')
    loadOrders()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const getStatusType = (status) => {
  const map = { 1: 'warning', 2: 'primary', 3: 'success' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 1: '待取餐', 2: '配送中', 3: '已送达' }
  return map[status] || '未知'
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

onMounted(() => {
  loadOrders()
})

onUnmounted(stopAutoRefresh)
</script>

<style scoped>
.tabs {
  margin-bottom: 16px;
}

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

.order-detail p {
  margin: 4px 0;
  font-size: 14px;
  color: #666;
}

.order-actions {
  flex-shrink: 0;
}
</style>
