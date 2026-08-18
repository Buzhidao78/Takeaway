<template>
  <div class="page">
    <h1>通知中心</h1>
    
    <el-tabs v-model="filterType" @tab-change="handleFilterChange">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="审核" name="audit" />
      <el-tab-pane label="订单" name="order" />
      <el-tab-pane label="骑手" name="rider" />
      <el-tab-pane label="评价" name="review" />
      <el-tab-pane label="退款" name="refund" />
      <el-tab-pane label="系统" name="system" />
      <el-tab-pane label="促销" name="promotion" />
    </el-tabs>

    <div class="notification-header">
      <div class="header-left">
        <span class="unread-tip" v-if="unreadCount > 0">
          当前有 <strong>{{ unreadCount }}</strong> 条未读通知
        </span>
      </div>
      <div class="header-actions">
        <div class="batch-controls" :class="{ active: batchMode }">
          <el-checkbox
            v-model="selectAll"
            :indeterminate="isIndeterminate"
            @change="handleSelectAll"
            class="select-all-checkbox"
          >
            全选
          </el-checkbox>
          <el-button
            v-if="selectedIds.length > 0"
            type="primary"
            size="small"
            @click="handleBatchMarkRead"
          >
            批量标记已读 ({{ selectedIds.length }})
          </el-button>
          <el-button
            v-if="selectedIds.length > 0"
            type="primary"
            size="small"
            @click="handleBatchDelete"
          >
            批量删除 ({{ selectedIds.length }})
          </el-button>
          <el-button
            type="primary"
            size="small"
            @click="exitBatchMode"
          >
            取消
          </el-button>
        </div>
        <el-button
          class="batch-mode-btn"
          :class="{ hidden: batchMode }"
          type="primary"
          size="small"
          @click="batchMode = true"
        >
          批量操作
        </el-button>
        <el-button
          type="primary"
          size="small"
          @click="handleMarkAllRead"
        >
          全部标记已读
        </el-button>
      </div>
    </div>

    <el-card v-loading="loading" class="notification-card">
      <div v-if="notificationList.length === 0" class="empty-container">
        <el-empty description="暂无通知" />
      </div>

      <div v-else class="notification-list">
        <div
          v-for="item in notificationList"
          :key="item.id"
          class="notification-item"
          :class="{ 
            'unread': item.isRead === 0,
            'selected': batchMode && selectedIds.includes(item.id),
            'clicking': clickingId === item.id
          }"
          @click="batchMode ? handleSelectItem(item.id) : handleNotificationClick(item)"
          @mousedown="handleMouseDown(item.id)"
          @mouseup="handleMouseUp"
          @mouseleave="handleMouseUp"
        >
          <div class="notification-checkbox" @click.stop>
            <el-checkbox
              v-if="batchMode"
              :model-value="selectedIds.includes(item.id)"
              @change="handleSelectItem(item.id)"
            />
          </div>
          <div class="notification-left">
            <div class="notification-icon" :class="'icon-' + item.type">
              <el-icon v-if="item.type === 'audit'"><DocumentChecked /></el-icon>
              <el-icon v-else-if="item.type === 'order'"><ShoppingCart /></el-icon>
              <el-icon v-else-if="item.type === 'rider'"><Van /></el-icon>
              <el-icon v-else-if="item.type === 'review'"><ChatDotRound /></el-icon>
              <el-icon v-else-if="item.type === 'refund'"><Money /></el-icon>
              <el-icon v-else-if="item.type === 'system'"><Bell /></el-icon>
              <el-icon v-else><Present /></el-icon>
            </div>
            <div class="notification-info">
              <div class="notification-title-row">
                <h3 class="notification-title">{{ item.title }}</h3>
                <el-tag v-if="item.isRead === 0" type="danger" size="small" effect="dark">未读</el-tag>
              </div>
              <p class="notification-desc">{{ item.content }}</p>
              <span class="notification-time">{{ formatTime(item.createTime) }}</span>
            </div>
          </div>
          <div class="notification-actions" @click.stop>
            <el-button
              v-if="item.isRead === 0"
              link
              type="primary"
              size="small"
              @click="handleMarkRead(item.id)"
            >
              标记已读
            </el-button>
            <el-button
              v-if="item.type !== 'audit'"
              link
              type="danger"
              size="small"
              @click="handleDelete(item.id)"
            >
              删除
            </el-button>
          </div>
        </div>

        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </el-card>

    <el-dialog
      v-model="detailDialogVisible"
      :title="currentNotification?.title"
      width="500px"
      :lock-scroll="false"
    >
      <div class="notification-detail">
        <div class="detail-header">
          <el-tag :type="getTypeTagType(currentNotification?.type)">
            {{ getTypeLabel(currentNotification?.type) }}
          </el-tag>
          <span class="detail-time">{{ formatTime(currentNotification?.createTime) }}</span>
        </div>
        <div class="detail-content">
          {{ currentNotification?.content }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ShoppingCart, Bell, Present, Van, ChatDotRound, Money, DocumentChecked } from '@element-plus/icons-vue'
import { getNotificationList, markAsRead, markAllAsRead, deleteNotification } from '@/api/notification'
import { useNotificationStore } from '@/stores/notification'
import { useUserStore } from '@/stores/user'
import api from '@/api'

const router = useRouter()
const notificationStore = useNotificationStore()
const userStore = useUserStore()

const loading = ref(false)
const notificationList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterType = ref('')
const unreadCount = ref(0)
const selectedIds = ref([])
const selectAll = ref(false)
const clickingId = ref(null)
const detailDialogVisible = ref(false)
const currentNotification = ref(null)
const batchMode = ref(false)

const isIndeterminate = computed(() => {
  return selectedIds.value.length > 0 && selectedIds.value.length < notificationList.value.length
})

async function loadNotifications() {
  loading.value = true
  try {
    const res = await getNotificationList({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      type: filterType.value || undefined
    })
    notificationList.value = res.records || []
    total.value = res.total || 0
    
    unreadCount.value = notificationList.value.filter(item => item.isRead === 0).length
    selectedIds.value = []
    selectAll.value = false
  } catch (error) {
    console.error('加载通知列表失败:', error)
    ElMessage.error('加载通知列表失败')
  } finally {
    loading.value = false
  }
}

function handleFilterChange() {
  currentPage.value = 1
  loadNotifications()
}

function handlePageChange(page) {
  currentPage.value = page
  loadNotifications()
}

async function handleMarkRead(id) {
  try {
    await markAsRead(id)
    notificationStore.decrementUnreadCount()
    const item = notificationList.value.find(n => n.id === id)
    if (item) {
      item.isRead = 1
      unreadCount.value = notificationList.value.filter(n => n.isRead === 0).length
    }
  } catch (error) {
    console.error('标记已读失败:', error)
    ElMessage.error('标记已读失败')
  }
}

async function handleMarkAllRead() {
  try {
    await ElMessageBox.confirm('确定要将所有通知标记为已读吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
      lockScroll: false
    })
    await markAllAsRead()
    ElMessage.success('已全部标记为已读')
    notificationStore.resetUnreadCount()
    loadNotifications()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('全部标记已读失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除这条通知吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      lockScroll: false
    })
    await deleteNotification(id)
    ElMessage.success('删除成功')
    loadNotifications()
    try {
      const res = await api.get('/notification/unread-count')
      notificationStore.setUnreadCount(res.count || 0)
    } catch (e) {
      console.error('刷新未读数量失败:', e)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除通知失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

async function handleBatchMarkRead() {
  if (selectedIds.value.length === 0) return
  try {
    await Promise.all(selectedIds.value.map(id => markAsRead(id)))
    ElMessage.success(`已标记 ${selectedIds.value.length} 条通知为已读`)
    notificationList.value.forEach(item => {
      if (selectedIds.value.includes(item.id)) {
        item.isRead = 1
      }
    })
    unreadCount.value = notificationList.value.filter(n => n.isRead === 0).length
    notificationStore.setUnreadCount(unreadCount.value)
    selectedIds.value = []
    selectAll.value = false
  } catch (error) {
    console.error('批量标记已读失败:', error)
    ElMessage.error('批量标记已读失败')
  }
}

async function handleBatchDelete() {
  const deletableIds = selectedIds.value.filter(id => {
    const item = notificationList.value.find(n => n.id === id)
    return item && item.type !== 'audit'
  })
  
  if (deletableIds.length === 0) {
    ElMessage.warning('审核通知不可删除')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${deletableIds.length} 条通知吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      lockScroll: false
    })
    await Promise.all(deletableIds.map(id => deleteNotification(id)))
    ElMessage.success(`已删除 ${deletableIds.length} 条通知`)
    loadNotifications()
    try {
      const res = await api.get('/notification/unread-count')
      notificationStore.setUnreadCount(res.count || 0)
    } catch (e) {
      console.error('刷新未读数量失败:', e)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

function handleSelectItem(id) {
  const item = notificationList.value.find(n => n.id === id)
  if (item && item.type === 'audit') {
    ElMessage.warning('审核通知不可删除')
    return
  }
  const index = selectedIds.value.indexOf(id)
  if (index > -1) {
    selectedIds.value.splice(index, 1)
  } else {
    selectedIds.value.push(id)
  }
  selectAll.value = selectedIds.value.length === notificationList.value.filter(n => n.type !== 'audit').length
}

function handleSelectAll(val) {
  if (val) {
    selectedIds.value = notificationList.value.filter(n => n.type !== 'audit').map(item => item.id)
  } else {
    selectedIds.value = []
  }
}

function exitBatchMode() {
  batchMode.value = false
  selectedIds.value = []
  selectAll.value = false
}

function handleMouseDown(id) {
  clickingId.value = id
}

function handleMouseUp() {
  clickingId.value = null
}

async function handleNotificationClick(item) {
  if (!item.relatedId) return
  
  if (item.isRead === 0) {
    await handleMarkRead(item.id)
  }
  
  if (item.type === 'audit') {
    currentNotification.value = item
    detailDialogVisible.value = true
  } else if (item.type === 'order' || item.type === 'rider' || item.type === 'refund') {
    if (userStore.role === 1) {
      router.push('/merchant/orders')
    } else {
      router.push(`/user/orders/${item.relatedId}`)
    }
  } else if (item.type === 'review') {
    if (userStore.role === 1) {
      router.push('/merchant/reviews')
    } else {
      router.push(`/stores/${item.relatedId}`)
    }
  } else {
    currentNotification.value = item
    detailDialogVisible.value = true
  }
}

function getTypeTagType(type) {
  const map = {
    audit: 'warning',
    order: 'primary',
    rider: 'success',
    review: 'info',
    refund: 'danger',
    system: '',
    promotion: 'success'
  }
  return map[type] || ''
}

function getTypeLabel(type) {
  const map = {
    audit: '审核',
    order: '订单',
    rider: '骑手',
    review: '评价',
    refund: '退款',
    system: '系统',
    promotion: '促销'
  }
  return map[type] || '通知'
}

function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) {
    return '刚刚'
  }
  if (diff < 3600000) {
    return Math.floor(diff / 60000) + '分钟前'
  }
  if (diff < 86400000) {
    return Math.floor(diff / 3600000) + '小时前'
  }
  if (diff < 604800000) {
    return Math.floor(diff / 86400000) + '天前'
  }
  
  return date.toLocaleDateString('zh-CN')
}

onMounted(() => {
  loadNotifications()
})
</script>

<style scoped>
.page { padding: 20px; }
h1 { margin-bottom: 20px; }

.notification-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding: 0 4px;
  flex-wrap: wrap;
  gap: 12px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.batch-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  overflow: hidden;
  max-width: 0;
  opacity: 0;
  transition: max-width 0.3s ease, opacity 0.3s ease, margin 0.3s ease;
}

.batch-controls.active {
  max-width: 600px;
  opacity: 1;
}

.batch-mode-btn {
  transition: max-width 0.3s ease, opacity 0.3s ease, margin 0.3s ease;
  overflow: hidden;
  max-width: 100px;
  opacity: 1;
}

.batch-mode-btn.hidden {
  max-width: 0;
  opacity: 0;
  margin: 0;
  padding: 0;
  border: none;
  pointer-events: none;
}

.unread-tip {
  font-size: 14px;
  color: #666;
}

.unread-tip strong {
  color: var(--primary);
}

.select-all-checkbox {
  font-size: 14px;
}

.empty-container {
  padding: 60px 0;
}

.notification-list {
  display: flex;
  flex-direction: column;
}

.notification-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  margin: 0 -20px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: background 0.2s, transform 0.1s;
  border-radius: 0;
  user-select: none;
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-item:hover {
  background: #f5f7fa;
}

.notification-item.unread {
  background: rgba(255, 107, 53, 0.04);
}

.notification-item.unread:hover {
  background: rgba(255, 107, 53, 0.08);
}

.notification-item.selected {
  background: rgba(64, 158, 255, 0.08);
}

.notification-item.selected:hover {
  background: rgba(64, 158, 255, 0.12);
}

.notification-item.clicking {
  transform: scale(0.995);
}

.notification-checkbox {
  flex-shrink: 0;
  margin-right: 12px;
}

.notification-left {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  flex: 1;
  min-width: 0;
}

.notification-icon {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(255, 107, 53, 0.1);
}

.notification-icon.icon-audit {
  background: rgba(230, 162, 60, 0.1);
}

.notification-icon.icon-audit .el-icon {
  color: #e6a23c;
}

.notification-icon.icon-order {
  background: rgba(64, 158, 255, 0.1);
}

.notification-icon.icon-order .el-icon {
  color: #409eff;
}

.notification-icon.icon-rider {
  background: rgba(103, 194, 58, 0.1);
}

.notification-icon.icon-rider .el-icon {
  color: #67c23a;
}

.notification-icon.icon-review {
  background: rgba(144, 147, 153, 0.1);
}

.notification-icon.icon-review .el-icon {
  color: #909399;
}

.notification-icon.icon-refund {
  background: rgba(245, 108, 108, 0.1);
}

.notification-icon.icon-refund .el-icon {
  color: #f56c6c;
}

.notification-icon.icon-system {
  background: rgba(144, 147, 153, 0.1);
}

.notification-icon.icon-system .el-icon {
  color: #909399;
}

.notification-icon .el-icon {
  font-size: 18px;
  color: var(--primary);
}

.notification-info {
  flex: 1;
  min-width: 0;
}

.notification-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.notification-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.notification-desc {
  margin: 0 0 6px 0;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}

.notification-time {
  font-size: 12px;
  color: #909399;
}

.notification-actions {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex-shrink: 0;
  align-items: flex-end;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.notification-detail {
  padding: 10px 0;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.detail-time {
  font-size: 13px;
  color: #909399;
}

.detail-content {
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
  margin-bottom: 20px;
  white-space: pre-wrap;
}

.detail-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}
</style>
