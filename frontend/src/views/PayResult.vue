<template>
  <div class="page result-page">
    <el-card class="result-card" v-loading="loading">
      <div class="result-content">
        <div class="result-icon" :class="success ? 'success' : 'error'">
          <el-icon v-if="success" :size="64"><CircleCheckFilled /></el-icon>
          <el-icon v-else :size="64"><CircleCloseFilled /></el-icon>
        </div>

        <h2 class="result-title">{{ success ? '支付成功' : '支付失败' }}</h2>
        <p class="result-subtitle">{{ success ? '感谢您的购买，我们将尽快为您配送' : '订单支付未完成，请重新支付或联系客服' }}</p>

        <el-divider />

        <div class="order-info" v-if="order">
          <div class="info-row">
            <span class="label">订单号</span>
            <span class="value">{{ order.orderNo }}</span>
          </div>
          <div class="info-row">
            <span class="label">订单金额</span>
            <span class="value price">¥{{ order.payAmount }}</span>
          </div>
          <div class="info-row">
            <span class="label">下单时间</span>
            <span class="value">{{ order.createTime }}</span>
          </div>
          <div class="info-row" v-if="order.payTime">
            <span class="label">支付时间</span>
            <span class="value">{{ order.payTime }}</span>
          </div>
        </div>

        <div class="actions">
          <el-button type="primary" size="large" @click="$router.push('/user/orders')">
            <el-icon><List /></el-icon>
            查看订单
          </el-button>
          <el-button type="primary" plain size="large" @click="$router.push('/')">
            <el-icon><HomeFilled /></el-icon>
            返回首页
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { myOrders } from '@/api/order'
import api from '@/api/index'
import { ElMessage } from 'element-plus'
import { CircleCheckFilled, CircleCloseFilled, List, HomeFilled } from '@element-plus/icons-vue'

const route = useRoute()
const success = ref(false)
const loading = ref(true)
const order = ref(null)

onMounted(async () => {
  const hash = location.hash
  const queryIndex = hash.indexOf('?')
  const params = new URLSearchParams(queryIndex !== -1 ? hash.substring(queryIndex + 1) : '')
  const outTradeNo = params.get('out_trade_no')

  if (!outTradeNo) {
    ElMessage.warning('未找到订单信息')
    success.value = false
    loading.value = false
    return
  }

  try {
    const res = await myOrders('', 1, 20)
    const found = res.records?.find(o => o.orderNo === outTradeNo)

    if (found) {
      order.value = found
      if (found.status === 1 || found.status >= 2) {
        success.value = true
      } else if (found.status === 0) {
        // 订单仍为待支付，可能是异步通知有延迟，主动查询支付宝状态
        try {
          const queryRes = await api.get(`/order/query-pay/${outTradeNo}`)
          if (queryRes.data?.paid) {
            success.value = true
            order.value.status = 1
          } else {
            success.value = false
          }
        } catch (e) {
          success.value = false
        }
      }
    } else {
      ElMessage.error('未找到该订单')
      success.value = false
    }
  } catch (error) {
    console.error('查询订单状态失败', error)
    ElMessage.error('查询订单状态失败')
    success.value = false
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.result-page {
  padding: 40px 20px;
  max-width: 600px;
  margin: 0 auto;
}

.result-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.result-content {
  text-align: center;
  padding: 20px 0;
}

.result-icon {
  margin-bottom: 20px;
}

.result-icon.success {
  color: #67c23a;
}

.result-icon.error {
  color: #f56c6c;
}

.result-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 10px;
  color: #303133;
}

.result-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0 0 20px;
}

.order-info {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px 20px;
  margin: 20px 0;
  text-align: left;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.info-row:not(:last-child) {
  border-bottom: 1px solid #ebeef5;
}

.info-row .label {
  color: #909399;
  font-size: 14px;
}

.info-row .value {
  color: #303133;
  font-size: 14px;
  font-weight: 500;
}

.info-row .price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: 600;
}

.actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 24px;
}
</style>
