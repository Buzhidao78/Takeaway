<template>
  <div class="page">
    <h1>收益明细</h1>
    
    <el-card>
      <div class="summary">
        <div class="summary-item">
          <div class="summary-value">¥{{ earnings.totalEarnings }}</div>
          <div class="summary-label">累计收益</div>
        </div>
        <div class="summary-item">
          <div class="summary-value">¥{{ earnings.todayEarnings }}</div>
          <div class="summary-label">今日收益</div>
        </div>
        <div class="summary-item">
          <div class="summary-value">{{ earnings.totalOrders }}</div>
          <div class="summary-label">总单量</div>
        </div>
        <div class="summary-item">
          <div class="summary-value">¥{{ earnings.balance }}</div>
          <div class="summary-label">账户余额</div>
        </div>
      </div>
      
      <el-divider />
      
      <div class="history">
        <h4 class="section-title">收益记录</h4>
        <el-table :data="records" v-loading="loading" style="width: 100%" empty-text=" " :show-summary="false">
          <el-table-column prop="orderNo" label="订单号" min-width="180" />
          <el-table-column prop="amount" label="收益金额" min-width="120">
            <template #default="{ row }">
              <span class="amount">+¥{{ row.amount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="时间" min-width="180">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="type" label="类型" min-width="100">
            <template #default="{ row }">
              <el-tag :type="row.type === 1 ? 'success' : row.type === 2 ? 'danger' : 'warning'" size="small">
                {{ row.type === 1 ? '订单收入' : row.type === 2 ? '退款' : '提现' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        
        <el-pagination
          v-model:current-page="page"
          :page-size="20"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadEarningsRecords"
          style="margin-top: 20px; justify-content: flex-end"
        />
        
        <div v-if="records.length === 0 && !loading" class="empty">
          <el-empty description="暂无收益记录" />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getEarningsStats, getEarnings } from '@/api/merchant'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const earnings = ref({
  totalEarnings: 0,
  todayEarnings: 0,
  totalOrders: 0,
  balance: 0
})
const records = ref([])
const total = ref(0)
const page = ref(1)

const loadEarningsStats = async () => {
  try {
    const res = await getEarningsStats()
    earnings.value = res || earnings.value
  } catch (e) {
    console.error('加载收益统计失败', e)
  }
}

const loadEarningsRecords = async () => {
  loading.value = true
  try {
    const res = await getEarnings({ page: page.value, size: 20 })
    records.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    console.error('加载收益记录失败', e)
    ElMessage.error('加载收益记录失败')
  } finally {
    loading.value = false
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', { 
    year: 'numeric',
    month: '2-digit', 
    day: '2-digit', 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

onMounted(async () => {
  await loadEarningsStats()
  await loadEarningsRecords()
})
</script>

<style scoped>
.summary {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.summary-item {
  text-align: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.summary-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--primary);
}

.summary-label {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.section-title {
  margin: 0 0 16px;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.amount {
  color: #67c23a;
  font-weight: 500;
}

.empty {
  padding: 40px 0;
  text-align: center;
}

.empty :deep(.el-empty__description) {
  color: #999;
  font-size: 14px;
}

:deep(.el-table__inner-wrapper::before) {
  display: none;
}
</style>
