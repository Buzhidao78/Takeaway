<template>
  <div class="page">
    <h1>我的订单</h1>
    <el-tabs v-model="statusTab" @tab-change="load">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="待支付" name="0" />
      <el-tab-pane label="已支付" name="1" />
      <el-tab-pane label="制作中" name="2" />
      <el-tab-pane label="配送中" name="3" />
      <el-tab-pane label="已完成" name="4" />
    </el-tabs>
    <el-table :data="orders" v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="payAmount" label="金额">
        <template #default="{ row }">¥{{ row.payAmount }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">{{ statusMap[row.status] || '未知' }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="180" />
      <el-table-column label="操作" width="300">
        <template #default="{ row }">
          <el-button link type="primary" @click="viewDetail(row)">详情</el-button>
          <el-button link type="primary" v-if="row.status===0" @click="pay(row)">去支付</el-button>
          <el-button link type="danger" v-if="row.status===0" @click="cancel(row)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination layout="prev,pager,next" :total="total" v-model:current-page="page" @current-change="load" style="margin-top:20px" />

    <!-- 订单详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="订单详情" width="700px">
      <div v-if="currentOrder">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">{{ statusMap[currentOrder.status] }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ currentOrder.createTime }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">¥{{ currentOrder.payAmount }}</el-descriptions-item>
        </el-descriptions>
        <el-divider>订单商品</el-divider>
        <el-table :data="currentOrder.items || []" border>
          <el-table-column prop="dishName" label="商品名称" />
          <el-table-column label="商品图片" width="100">
            <template #default="{ row }">
              <div v-if="!row.dishImage" class="no-image-placeholder">
                <el-icon :size="24"><Picture /></el-icon>
                <span>暂无图片</span>
              </div>
              <el-image 
                v-else
                :src="getImageUrl(row.dishImage)" 
                fit="cover" 
                style="width: 60px; height: 60px; border-radius: 4px;" 
              />
            </template>
          </el-table-column>
          <el-table-column prop="price" label="单价">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" />
          <el-table-column label="小计">
            <template #default="{ row }">¥{{ row.amount }}</template>
          </el-table-column>
          <el-table-column label="评价状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.reviewed ? 'success' : 'info'">
                {{ row.reviewed ? '已评价' : '未评价' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" v-if="currentOrder.status === 4">
            <template #default="{ row }">
              <el-button link type="primary" v-if="!row.reviewed" @click="reviewProduct(row)">评价</el-button>
              <el-button link type="warning" v-else @click="reviewProduct(row)">追评</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <!-- 商品评价对话框 -->
    <ProductReviewDialog
      v-model="reviewDialogVisible"
      :order-item="currentOrderItem"
      :order-id="currentOrder?.id"
      :store-id="currentOrder?.storeId"
      @submitted="handleReviewSubmitted"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { myOrders, orderDetail, payOrder, cancelOrder } from '@/api/order'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import ProductReviewDialog from '@/components/ProductReviewDialog.vue'

const route = useRoute()
const orders = ref([])
const loading = ref(false)
const statusTab = ref('')
const page = ref(1)
const total = ref(0)
const statusMap = { 0: '待支付', 1: '已支付', 2: '制作中', 3: '配送中', 4: '已完成', 5: '已取消' }
const detailDialogVisible = ref(false)
const reviewDialogVisible = ref(false)
const currentOrder = ref(null)
const currentOrderItem = ref(null)

// 获取图片 URL，确保有 /api 前缀
const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return path.startsWith('/api') ? path : '/api' + path
}

async function load() {
  loading.value = true
  try {
    if (route.params.id) {
      const res = await orderDetail(route.params.id)
      currentOrder.value = res?.order || res
      detailDialogVisible.value = true
      orders.value = []
      total.value = 0
    } else {
      const res = await myOrders(statusTab.value ? Number(statusTab.value) : null, page.value, 10)
      orders.value = Array.isArray(res?.records) ? res.records : []
      total.value = res?.total || 0
    }
  } catch (e) {
    console.error('加载订单失败:', e)
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function viewDetail(row) {
  try {
    const res = await orderDetail(row.id)
    currentOrder.value = res?.order || res
    detailDialogVisible.value = true
  } catch (e) {
    console.error('加载订单详情失败:', e)
    ElMessage.error('加载订单详情失败')
  }
}

async function pay(row) {
  try {
    const res = await payOrder(row.id)
    const payHtml = res || res?.data
    if (payHtml) {
      document.body.innerHTML = payHtml
      document.forms[0]?.submit()
    } else {
      ElMessage.error('支付创建失败，请配置支付宝沙箱')
    }
  } catch (e) {
    console.error('支付失败:', e)
    ElMessage.error(e?.message || '支付失败')
  }
}

async function cancel(row) {
  try {
    await cancelOrder(row.id)
    ElMessage.success('已取消')
    load()
  } catch (e) {
    console.error('取消失败:', e)
    ElMessage.error(e?.message || '取消失败')
  }
}

function reviewProduct(item) {
  currentOrderItem.value = item
  reviewDialogVisible.value = true
}

async function handleReviewSubmitted() {
  await viewDetail(currentOrder.value)
}

onMounted(load)
</script>

<style scoped>
.page { padding: 20px; }
h1 { margin-bottom: 20px; }

/* 暂无图片占位符 */
.no-image-placeholder {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 11px;
  gap: 2px;
}
</style>
