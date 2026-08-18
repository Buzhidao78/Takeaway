<template>
  <div class="page">
    <div class="header-info">
      <h1>管理后台</h1>
      <div class="current-user">
        <el-tag type="primary" size="small">管理员</el-tag>
        <span class="user-info">{{ userStore.user?.nickname }} ({{ userStore.user?.phone }})</span>
      </div>
    </div>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="用户管理" name="users">
        <el-table :data="users" v-loading="loading">
          <el-table-column prop="phone" label="手机号" width="140" />
          <el-table-column prop="nickname" label="昵称" min-width="120" />
          <el-table-column prop="role" label="角色" width="100">
            <template #default="{ row }"> {{ {0:'用户',1:'商家',2:'管理员',3:'骑手'}[row.role] }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status===1?'success':'danger'"> {{ row.status===1?'正常':'禁用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" align="center">
            <template #default="{ row }">
              <el-button link @click="toggleUserStatus(row)"> {{ row.status===1?'禁用':'启用' }}</el-button>
              <el-button link type="danger" @click="deleteUser(row)">注销</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="已注销用户" name="deletedUsers">
        <el-table :data="deletedUsers" v-loading="loading">
          <el-table-column prop="phone" label="原手机号" width="150" />
          <el-table-column prop="nickname" label="昵称" min-width="120" />
          <el-table-column prop="role" label="角色" width="100">
            <template #default="{ row }"> {{ {0:'用户',1:'商家',2:'管理员',3:'骑手'}[row.role] }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag type="danger">已注销</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="骑手审核" name="riders">
        <el-table :data="riders" v-loading="loading">
          <el-table-column prop="name" label="姓名" min-width="100" />
          <el-table-column prop="phone" label="联系电话" width="130" />
          <el-table-column prop="idCard" label="身份证号" width="180" />
          <el-table-column prop="auditStatus" label="审核状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.auditStatus===0?'warning':row.auditStatus===1?'success':'danger'">
                {{ {0:'待审核',1:'已通过',2:'已拒绝'}[row.auditStatus] }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="totalOrders" label="配送单数" width="100" />
          <el-table-column prop="rating" label="评分" width="80" />
          <el-table-column label="操作" width="200" fixed="right" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.auditStatus===1" type="success" size="small">已通过</el-tag>
              <el-tag v-if="row.auditStatus===2" type="danger" size="small">已拒绝</el-tag>
              <el-button link v-if="row.auditStatus===0" type="primary" @click="verifyRider(row, 1)">通过</el-button>
              <el-button link v-if="row.auditStatus===0" type="danger" @click="showRiderRejectDialog(row)">拒绝</el-button>
              <el-button link v-if="row.auditStatus===1||row.auditStatus===2" type="primary" @click="showRiderAuditHistory(row)">审核记录</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="店铺审核" name="stores">
        <el-table :data="stores" v-loading="loading">
          <el-table-column prop="name" label="店铺名" min-width="150" />
          <el-table-column prop="phone" label="联系电话" width="130" />
          <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
          <el-table-column prop="auditStatus" label="审核状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.auditStatus===0?'warning':row.auditStatus===1?'success':'danger'">
                {{ {0:'待审核',1:'已通过',2:'已拒绝'}[row.auditStatus] }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.auditStatus===1" type="success" size="small">已通过</el-tag>
              <el-tag v-if="row.auditStatus===2" type="danger" size="small">已拒绝</el-tag>
              <el-button link v-if="row.auditStatus===2" type="warning" @click="showRejectReason(row)">查看理由</el-button>
              <el-button link v-if="row.auditStatus===0" type="primary" @click="audit(row, 1)">通过</el-button>
              <el-button link v-if="row.auditStatus===0" type="danger" @click="showRejectDialog(row)">拒绝</el-button>
              <el-button link v-if="row.auditStatus===1" type="primary" @click="showStoreCategoryDialog(row)">管理分类</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="分类管理" name="categories">
        <div class="category-toolbar">
          <el-button type="primary" @click="showCategoryDialog()">
            <el-icon><Plus /></el-icon> 添加分类
          </el-button>
        </div>
        <el-table :data="categories" v-loading="loading" style="margin-top: 16px;">
          <el-table-column prop="name" label="分类名称" />
          <el-table-column label="图标" width="150">
            <template #default="{ row }">
              <el-icon :size="24">
                <component :is="row.icon" />
              </el-icon>
            </template>
          </el-table-column>
          <el-table-column prop="sort" label="排序" width="150" />
          <el-table-column prop="status" label="状态" width="150">
            <template #default="{ row }">
              <el-tag :type="row.status===1?'success':'danger'">
                {{ row.status===1?'启用':'禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button link type="primary" @click="showCategoryDialog(row)">编辑</el-button>
              <el-button link type="danger" @click="deleteCategory(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="Banner 管理" name="banners">
        <div class="banner-toolbar">
          <el-button type="primary" @click="showBannerDialog()">
            <el-icon><Plus /></el-icon> 添加 Banner
          </el-button>
        </div>
        <el-table :data="banners" v-loading="loading" style="margin-top: 16px;">
          <el-table-column prop="title" label="标题" />
          <el-table-column prop="subtitle" label="副标题" />
          <el-table-column label="图片" width="200">
            <template #default="{ row }">
              <div v-if="row.image" style="display: inline-block;">
                <el-image 
                  :src="formatImageUrl(row.image)" 
                  fit="cover"
                  style="width: 120px; height: 60px; border-radius: 4px;"
                  :preview-src-list="[formatImageUrl(row.image)]"
                />
              </div>
              <div v-else class="banner-placeholder-image">
                <span>无图片</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="link" label="跳转链接" show-overflow-tooltip />
          <el-table-column prop="sort" label="排序" width="80" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status===1?'success':'info'">
                {{ row.status===1?'启用':'禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="showBannerDialog(row)">编辑</el-button>
              <el-button link type="danger" @click="deleteBanner(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="rejectDialogVisible" title="拒绝审核" width="400px">
      <el-input v-model="rejectReason" type="textarea" :rows="4" placeholder="请输入拒绝理由" />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="riderRejectDialogVisible" title="拒绝骑手审核" width="400px">
      <el-input v-model="riderRejectReason" type="textarea" :rows="4" placeholder="请输入拒绝理由" />
      <template #footer>
        <el-button @click="riderRejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmRiderReject">确认拒绝</el-button>
      </template>
    </el-dialog>

    <!-- Banner 编辑对话框 -->
    <el-dialog v-model="bannerDialogVisible" :title="bannerForm.id ? '编辑 Banner' : '添加 Banner'" width="600px">
      <el-form :model="bannerForm" label-width="100px" label-position="left">
        <el-form-item label="标题" required>
          <el-input v-model="bannerForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="bannerForm.subtitle" placeholder="请输入副标题" />
        </el-form-item>
        <el-form-item label="Banner 图片">
          <el-upload
            class="banner-uploader"
            action="/api/file/upload"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleBannerImageSuccess"
            :before-upload="beforeBannerUpload"
          >
            <img v-if="bannerForm.image" :src="formatImageUrl(bannerForm.image)" class="banner-preview" />
            <el-icon v-else class="banner-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="form-tip">建议尺寸：1200x320，支持 jpg/png，不超过 2MB（可选）</div>
        </el-form-item>
        <el-form-item label="跳转链接">
          <el-input v-model="bannerForm.link" placeholder="可选，如：/stores" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="bannerForm.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="bannerForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bannerDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveBanner">保存</el-button>
      </template>
    </el-dialog>

    <!-- 分类编辑对话框 -->
    <el-dialog v-model="categoryDialogVisible" :title="categoryForm.id ? '编辑分类' : '添加分类'" width="500px">
      <el-form :model="categoryForm" label-width="100px" label-position="left">
        <el-form-item label="分类名称" required>
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="图标" required>
          <el-select v-model="categoryForm.icon" placeholder="请选择图标" style="width: 100%">
            <el-option v-for="icon in iconOptions" :key="icon" :label="icon" :value="icon">
              <div style="display: flex; align-items: center; gap: 8px;">
                <el-icon :size="20"><component :is="icon" /></el-icon>
                <span>{{ icon }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="categoryForm.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="categoryForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCategory">保存</el-button>
      </template>
    </el-dialog>

    <!-- 店铺分类关联对话框 -->
    <el-dialog v-model="storeCategoryDialogVisible" title="管理店铺分类" width="500px">
      <div v-if="currentStore">
        <p class="dialog-tip">为店铺 <strong>{{ currentStore.name }}</strong> 选择分类:</p>
        <el-checkbox-group v-model="selectedCategories">
          <el-checkbox v-for="cat in categories" :key="cat.id" :label="cat.id" style="display: block; margin: 8px 0;">
            <el-icon :size="16" style="vertical-align: middle;"><component :is="cat.icon" /></el-icon>
            <span style="margin-left: 8px;">{{ cat.name }}</span>
          </el-checkbox>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="storeCategoryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveStoreCategories">保存</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, watch, onMounted, computed } from 'vue'
import { users as fetchUsers, setUserStatus, stores as fetchStores, auditStore } from '@/api/admin'
import { categoryListAll, addCategory, updateCategory, deleteCategory as deleteCategoryApi } from '@/api/category'
import { getStoreCategories, bindStoreCategories } from '@/api/store'
import api from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const userStore = useUserStore()

// 上传请求头
const uploadHeaders = computed(() => ({
  'Authorization': 'Bearer ' + userStore.token
}))

const activeTab = ref('users')
const users = ref([])
const deletedUsers = ref([])
const riders = ref([])
const stores = ref([])
const banners = ref([])
const categories = ref([])
const loading = ref(false)
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const currentStore = ref(null)
const currentRider = ref(null)
const riderRejectDialogVisible = ref(false)
const riderRejectReason = ref('')
const bannerDialogVisible = ref(false)
const categoryDialogVisible = ref(false)
const storeCategoryDialogVisible = ref(false)
const selectedCategories = ref([])
const bannerForm = ref({
  title: '',
  subtitle: '',
  image: '',
  link: '',
  sort: 0,
  status: 1
})
const categoryForm = ref({
  name: '',
  icon: '',
  sort: 0,
  status: 1
})

const iconOptions = [
  'Food', 'ShoppingCart', 'Apple', 'Van', 'Present', 
  'IceCream', 'Coffee', 'IceDrink', 'Burger', 'Chicken',
  'Watermelon', 'Cherry', 'Orange', 'Pear', 'Grape',
  'Goblet', 'Bowl', 'ForkSpoon', 'KnifeFork', 'Sugar',
  'Flag', 'Star', 'Trophy', 'Medal'
]

async function loadUsers() {
  loading.value = true
  try {
    console.log('加载未注销用户，参数：page=1, size=20, deleted=0')
    const res = await fetchUsers(1, 20, undefined, 0)  // 只加载未注销用户
    console.log('加载未注销用户结果:', res)
    users.value = Array.isArray(res?.records) ? res.records : []
    console.log('未注销用户数量:', users.value.length)
  } catch (e) {
    console.error('加载用户失败:', e)
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function loadDeletedUsers() {
  loading.value = true
  try {
    console.log('加载已注销用户，参数：page=1, size=20, deleted=1')
    const res = await fetchUsers(1, 20, undefined, 1)  // 只加载已注销用户
    console.log('加载已注销用户结果:', res)
    deletedUsers.value = Array.isArray(res?.records) ? res.records : []
    console.log('已注销用户数量:', deletedUsers.value.length)
  } catch (e) {
    console.error('加载已注销用户失败:', e)
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function loadRiders() {
  loading.value = true
  try {
    const res = await api.get('/admin/rider/list', { params: { page: 1, size: 20 } })
    riders.value = Array.isArray(res?.records) ? res.records : []
  } catch (e) {
    console.error('加载骑手失败:', e)
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function showRiderAuditHistory(row) {
  currentRider.value = row
  loading.value = true
  try {
    const params = { page: 1, size: 50 }
    if (row.id) params.riderId = row.id
    const res = await api.get('/admin/rider/audit-history', { params })
    const records = Array.isArray(res?.records) ? res.records : []
    
    // 构建 HTML 表格内容
    let tableHtml = '<div style="padding: 10px 0;">'
    tableHtml += `<p style="margin-bottom: 12px; color: #606266;">骑手 <strong>${row.name}</strong> 的审核记录:</p>`
    
    if (records.length === 0) {
      tableHtml += '<p style="color: #909399;">暂无审核记录</p>'
    } else {
      tableHtml += '<table style="width: 100%; border-collapse: collapse; font-size: 14px;">'
      tableHtml += '<thead><tr style="background: #f5f7fa;">'
      tableHtml += '<th style="border: 1px solid #ebeef5; padding: 8px; text-align: center;">审核结果</th>'
      tableHtml += '<th style="border: 1px solid #ebeef5; padding: 8px; text-align: center;">拒绝理由</th>'
      tableHtml += '<th style="border: 1px solid #ebeef5; padding: 8px; text-align: center;">审核人</th>'
      tableHtml += '<th style="border: 1px solid #ebeef5; padding: 8px; text-align: center;">审核时间</th>'
      tableHtml += '</tr></thead><tbody>'
      
      records.forEach(record => {
        const statusTag = record.auditStatus === 1 
          ? '<span style="color: #67c23a; font-weight: bold;">已通过</span>'
          : '<span style="color: #f56c6c; font-weight: bold;">已拒绝</span>'
        const rejectReason = record.rejectReason || '-'
        const auditorName = record.auditorName || '-'
        const auditTime = record.auditTime || '-'
        
        tableHtml += '<tr>'
        tableHtml += `<td style="border: 1px solid #ebeef5; padding: 8px; text-align: center;">${statusTag}</td>`
        tableHtml += `<td style="border: 1px solid #ebeef5; padding: 8px;">${rejectReason}</td>`
        tableHtml += `<td style="border: 1px solid #ebeef5; padding: 8px; text-align: center;">${auditorName}</td>`
        tableHtml += `<td style="border: 1px solid #ebeef5; padding: 8px; text-align: center;">${auditTime}</td>`
        tableHtml += '</tr>'
      })
      
      tableHtml += '</tbody></table>'
    }
    tableHtml += '</div>'
    
    ElMessageBox.alert(tableHtml, '审核记录', {
      confirmButtonText: '知道了',
      dangerouslyUseHTMLString: true,
      customClass: 'rider-audit-history-dialog'
    })
  } catch (e) {
    console.error('加载骑手审核历史失败:', e)
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function loadStores() {
  loading.value = true
  try {
    const res = await fetchStores(1, 20)
    stores.value = Array.isArray(res?.records) ? res.records : []
  } catch (e) {
    console.error('加载店铺失败:', e)
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function loadBanners() {
  loading.value = true
  try {
    const res = await api.get('/banner/list')
    banners.value = Array.isArray(res) ? res : []
  } catch (e) {
    console.error('加载 Banner 失败:', e)
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  loading.value = true
  try {
    const res = await categoryListAll()
    categories.value = Array.isArray(res) ? res : []
  } catch (e) {
    console.error('加载分类失败:', e)
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function toggleUserStatus(row) {
  try {
    await setUserStatus(row.id, row.status === 1 ? 0 : 1)
    ElMessage.success('已更新')
    loadUsers();
  } catch (e) {
    console.error('更新用户状态失败:', e)
    ElMessage.error(e?.message || '更新失败')
  }
}

async function deleteUser(row) {
  console.log('开始注销用户:', row)
  try {
    await ElMessageBox.confirm(`确定要注销用户"${row.nickname}"吗？此操作不可恢复！`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      lockScroll: false
    })
    console.log('用户确认删除，调用 API:', `/admin/user/${row.id}`)
    await api.delete(`/admin/user/${row.id}`)
    console.log('删除成功')
    ElMessage.success('已注销')
    loadUsers()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('注销用户失败:', e)
      ElMessage.error(e?.message || '注销失败')
    }
  }
}

async function verifyRider(row, auditStatus) {
  try {
    await api.put(`/admin/rider/verify/${row.id}`, { auditStatus })
    ElMessage.success('已处理')
    loadRiders()
  } catch (e) {
    console.error('审核骑手失败:', e)
    ElMessage.error(e?.message || '审核失败')
  }
}

function showRejectReason(row) {
  ElMessageBox.alert(
    `<div style="padding: 10px 0; line-height: 1.6; color: #606266;">${row.rejectReason || '暂无拒绝理由'}</div>`,
    '拒绝理由',
    {
      confirmButtonText: '知道了',
      dangerouslyUseHTMLString: true,
      customClass: 'reject-reason-dialog'
    }
  )
}

function showRiderRejectDialog(row) {
  currentRider.value = row
  riderRejectReason.value = ''
  riderRejectDialogVisible.value = true
}

async function confirmRiderReject() {
  if (!riderRejectReason.value.trim()) {
    ElMessage.warning('请输入拒绝理由')
    return
  }
  try {
    await api.put(`/admin/rider/verify/${currentRider.value.id}`, { auditStatus: 2, rejectReason: riderRejectReason.value })
    ElMessage.success('已拒绝')
    riderRejectDialogVisible.value = false
    loadRiders()
  } catch (e) {
    console.error('拒绝骑手失败:', e)
    ElMessage.error(e?.message || '拒绝失败')
  }
}

function showRejectDialog(row) {
  currentStore.value = row
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

async function confirmReject() {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入拒绝理由')
    return
  }
  try {
    await auditStore(currentStore.value.id, { auditStatus: 2, rejectReason: rejectReason.value })
    ElMessage.success('已拒绝')
    rejectDialogVisible.value = false
    loadStores()
  } catch (e) {
    console.error('拒绝店铺失败:', e)
    ElMessage.error(e?.message || '拒绝失败')
  }
}

// Banner 相关函数
function formatImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return url.startsWith('/api') ? url : '/api' + url
}

function beforeBannerUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

function handleBannerImageSuccess(response, file) {
  if (response.code === 200 || response.code === 0) {
    bannerForm.value.image = response.data
  } else {
    ElMessage.error(response.msg || response.message || '上传失败')
  }
}

function showBannerDialog(row) {
  if (row) {
    bannerForm.value = {
      id: row.id,
      title: row.title,
      subtitle: row.subtitle,
      image: row.image,
      link: row.link,
      sort: row.sort,
      status: row.status
    }
  } else {
    bannerForm.value = {
      title: '',
      subtitle: '',
      image: '',
      link: '',
      sort: 0,
      status: 1
    }
  }
  bannerDialogVisible.value = true
}

async function saveBanner() {
  if (!bannerForm.value.title) {
    ElMessage.warning('请输入标题')
    return
  }
  
  try {
    const url = bannerForm.value.id ? '/banner/update' : '/banner/save'
    await api.post(url, bannerForm.value)
    ElMessage.success('保存成功')
    bannerDialogVisible.value = false
    loadBanners()
  } catch (e) {
    console.error('保存 Banner 失败:', e)
    ElMessage.error(e?.message || '保存失败')
  }
}

async function deleteBanner(row) {
  try {
    await ElMessageBox.confirm(`确定要删除 Banner"${row.title}"吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      lockScroll: false
    })
    await api.delete(`/banner/delete/${row.id}`)
    ElMessage.success('已删除')
    loadBanners()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('删除 Banner 失败:', e)
      ElMessage.error(e?.message || '删除失败')
    }
  }
}

function showCategoryDialog(row) {
  if (row) {
    categoryForm.value = {
      id: row.id,
      name: row.name,
      icon: row.icon,
      sort: row.sort,
      status: row.status
    }
  } else {
    categoryForm.value = {
      name: '',
      icon: '',
      sort: 0,
      status: 1
    }
  }
  categoryDialogVisible.value = true
}

async function saveCategory() {
  if (!categoryForm.value.name) {
    ElMessage.warning('请输入分类名称')
    return
  }
  if (!categoryForm.value.icon) {
    ElMessage.warning('请选择图标')
    return
  }
  
  try {
    if (categoryForm.value.id) {
      await updateCategory(categoryForm.value.id, categoryForm.value)
    } else {
      await addCategory(categoryForm.value)
    }
    ElMessage.success('保存成功')
    categoryDialogVisible.value = false
    loadCategories()
  } catch (e) {
    console.error('保存分类失败:', e)
    ElMessage.error(e?.message || '保存失败')
  }
}

async function deleteCategory(row) {
  try {
    await ElMessageBox.confirm(`确定要删除分类"${row.name}"吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      lockScroll: false
    })
    await deleteCategoryApi(row.id)
    ElMessage.success('已删除')
    loadCategories()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('删除分类失败:', e)
      ElMessage.error(e?.message || '删除失败')
    }
  }
}

async function audit(row, auditStatus) {
  try {
    await auditStore(row.id, { auditStatus })
    ElMessage.success('已处理')
    loadStores()
  } catch (e) {
    console.error('审核店铺失败:', e)
    ElMessage.error(e?.message || '审核失败')
  }
}

async function showStoreCategoryDialog(row) {
  currentStore.value = row
  try {
    const res = await getStoreCategories(row.id)
    selectedCategories.value = Array.isArray(res) ? res : []
  } catch (e) {
    console.error('获取店铺分类失败:', e)
    selectedCategories.value = []
  }
  storeCategoryDialogVisible.value = true
}

async function saveStoreCategories() {
  if (!currentStore.value) return
  
  try {
    await bindStoreCategories(currentStore.value.id, selectedCategories.value)
    ElMessage.success('分类已更新')
    storeCategoryDialogVisible.value = false
  } catch (e) {
    console.error('保存店铺分类失败:', e)
    ElMessage.error(e?.message || '保存失败')
  }
}

watch(activeTab, v => {
  if (v === 'users') loadUsers()
  else if (v === 'deletedUsers') loadDeletedUsers()
  else if (v === 'riders') loadRiders()
  else if (v === 'banners') loadBanners()
  else if (v === 'categories') loadCategories()
  else loadStores()
})
onMounted(() => {
  if (activeTab.value === 'users') loadUsers()
  else if (activeTab.value === 'deletedUsers') loadDeletedUsers()
  else if (activeTab.value === 'riders') loadRiders()
  else if (activeTab.value === 'banners') loadBanners()
  else if (activeTab.value === 'categories') loadCategories()
  else loadStores()
})
</script>

<style scoped>
.page {
  padding: 20px;
}

.header-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.header-info h1 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.current-user {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 15px;
  background: #f5f7fa;
  border-radius: 8px;
}

.user-info {
  font-size: 14px;
  color: #606266;
}

.banner-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.category-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.category-toolbar + .el-table {
  width: 100% !important;
}

.banner-uploader {
  width: 200px;
  height: 100px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.banner-uploader:hover {
  border-color: #409EFF;
}

.banner-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.banner-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.form-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.banner-placeholder-image {
  width: 120px;
  height: 60px;
  border-radius: 4px;
  background: linear-gradient(135deg, #ff6b35 0%, #ff8f65 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 12px;
}

.dialog-tip {
  margin: 0 0 16px 0;
  font-size: 14px;
  color: #606266;
}

/* 修复对话框遮罩层覆盖问题 */
:deep(.el-overlay-dialog) {
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.el-overlay) {
  position: fixed !important;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100% !important;
  height: 100% !important;
  margin: 0 !important;
  padding: 0 !important;
}

/* 拒绝理由对话框样式 */
:deep(.reject-reason-dialog .el-message-box__content) {
  padding: 20px;
  min-height: 80px;
}

:deep(.reject-reason-dialog .el-message-box__message) {
  padding: 0;
}

:deep(.reject-reason-dialog .el-message-box__btns) {
  padding: 10px 20px 20px;
}
</style>
