<template>
  <div class="page">
    <h1>订单管理</h1>
    <el-tabs v-model="statusTab" @tab-change="load">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="待支付" name="0" />
      <el-tab-pane label="已支付" name="1" />
      <el-tab-pane label="制作中" name="2" />
      <el-tab-pane label="配送中" name="3" />
      <el-tab-pane label="已完成" name="4" />
    </el-tabs>
    <el-table :data="ordersList" v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="payAmount" label="金额">
        <template #default="{ row }">¥{{ row.payAmount }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">{{ statusMap[row.status] }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="180" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button link v-if="row.status===1" @click="updateStatus(row, 2)">接单</el-button>
          <el-button link v-if="row.status===2" @click="updateStatus(row, 3)">出餐</el-button>
          <el-tag v-if="row.status===3" type="info" size="small">骑手配送中</el-tag>
          <el-tag v-if="row.status===4" type="success" size="small">已完成</el-tag>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination layout="prev,pager,next" :total="total" v-model:current-page="page" @current-change="load" style="margin-top:20px" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { orders, updateOrderStatus } from '@/api/merchant'
import { ElMessage } from 'element-plus'

const ordersList = ref([])
const loading = ref(false)
const statusTab = ref('')
const page = ref(1)
const total = ref(0)
const statusMap = { 0: '待支付', 1: '已支付', 2: '制作中', 3: '配送中', 4: '已完成', 5: '已取消' }
let refreshTimer = null

async function load() {
  loading.value = true
  try {
    const res = await orders(statusTab.value ? Number(statusTab.value) : null, page.value, 10)
    ordersList.value = Array.isArray(res?.records) ? res.records : []
    total.value = res?.total || 0
    
    // 如果有配送中的订单，启动自动刷新
    const hasActiveOrders = ordersList.value.some(o => o.status >= 2 && o.status <= 3)
    if (hasActiveOrders && !refreshTimer) {
      startAutoRefresh()
    } else if (!hasActiveOrders && refreshTimer) {
      stopAutoRefresh()
    }
  } catch (e) {
    console.error('加载订单失败:', e)
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function startAutoRefresh() {
  stopAutoRefresh()
  refreshTimer = setInterval(() => {
    load()
  }, 10000)
}

function stopAutoRefresh() {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

async function updateStatus(row, status) {
  await updateOrderStatus(row.id, status)
  ElMessage.success('已更新')
  load()
}

onMounted(load)
onUnmounted(stopAutoRefresh)
</script>
