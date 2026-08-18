<template>
  <div class="page">
    <h1>骑手中心</h1>
    
    <el-card>
      <div class="rider-info">
        <el-avatar :size="64" :style="{ background: 'var(--primary-gradient)' }">
          {{ (riderInfo?.name || 'R')[0] }}
        </el-avatar>
        <div class="rider-detail">
          <h3>{{ riderInfo?.name }}</h3>
          <p>{{ riderInfo?.phone }}</p>
          <el-tag :type="riderInfo?.auditStatus === 1 ? 'success' : 'warning'" size="small">
            {{ {0:'待审核',1:'已通过',2:'已拒绝'}[riderInfo?.auditStatus || 0] }}
          </el-tag>
        </div>
        <div class="online-switch">
          <span class="online-label">{{ isOnline ? '在线中' : '已离线' }}</span>
          <el-switch
            v-model="isOnline"
            @change="handleToggleOnline"
            active-text="在线"
            inactive-text="离线"
            :disabled="riderInfo?.auditStatus !== 1"
          />
          <el-tooltip v-if="riderInfo?.auditStatus !== 1" content="审核通过后方可上线" placement="top">
            <el-icon class="audit-tip-icon"><Warning /></el-icon>
          </el-tooltip>
        </div>
      </div>
      
      <el-divider />
      
      <div class="stats-section">
        <h4 class="section-title">今日数据</h4>
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-value">{{ stats.todayOrders }}</div>
            <div class="stat-label">今日接单</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ stats.completedOrders }}</div>
            <div class="stat-label">已完成</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">¥{{ stats.todayEarnings }}</div>
            <div class="stat-label">今日收益</div>
          </div>
        </div>
      </div>
      
      <el-divider />
      
      <div class="menu-section">
        <h4 class="section-title">快捷操作</h4>
        <el-menu>
          <el-menu-item @click="router.push('/rider/grab')">
            <el-icon><ShoppingCart /></el-icon>
            <span>抢单大厅</span>
          </el-menu-item>
          <el-menu-item @click="router.push('/rider/delivery')">
            <el-icon><Van /></el-icon>
            <span>配送订单</span>
          </el-menu-item>
          <el-menu-item @click="router.push('/rider/earnings')">
            <el-icon><Wallet /></el-icon>
            <span>收益明细</span>
          </el-menu-item>
        </el-menu>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getRiderInfo, toggleOnline, getEarningsStats } from '@/api/rider'
import { Van, ShoppingCart, Wallet, Warning } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const isOnline = ref(false)
const riderInfo = ref(null)
const stats = ref({
  todayOrders: 0,
  completedOrders: 0,
  todayEarnings: 0
})

const loadRiderInfo = async () => {
  try {
    const res = await getRiderInfo()
    riderInfo.value = res
    if (res) {
      isOnline.value = res.onlineStatus || false
    }
  } catch (e) {
    ElMessage.error('加载骑手信息失败')
  }
}

const loadStats = async () => {
  try {
    const res = await getEarningsStats()
    stats.value = res || stats.value
  } catch (e) {
    console.error('加载统计数据失败', e)
  }
}

const handleToggleOnline = async (val) => {
  try {
    await toggleOnline(val)
    ElMessage.success(val ? '已上线' : '已离线')
  } catch (e) {
    isOnline.value = !val
    const message = e.response?.data?.message || e.message || '操作失败'
    ElMessage.error(message)
  }
}

onMounted(async () => {
  await loadRiderInfo()
  loadStats()
})
</script>

<style scoped>
.rider-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.rider-detail {
  flex: 1;
}

.rider-detail h3 {
  margin: 0 0 4px;
  font-size: 18px;
}

.rider-detail p {
  margin: 0 0 8px;
  color: #666;
  font-size: 14px;
}

.online-switch {
  display: flex;
  align-items: center;
  gap: 12px;
}

.audit-tip-icon {
  color: #e6a23c;
  font-size: 16px;
}

.online-label {
  font-size: 14px;
  color: #666;
}

.stats-section {
  margin-top: 16px;
}

.section-title {
  margin: 0 0 16px;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.stat-item {
  text-align: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--primary);
}

.stat-label {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.menu-section {
  margin-top: 16px;
}
</style>
