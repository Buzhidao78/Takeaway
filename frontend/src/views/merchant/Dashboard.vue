<template>
  <div class="page">
    <h1>商家中心</h1>
    
    <el-alert v-if="store?.auditStatus === 0" type="warning" :closable="false" style="margin-bottom: 20px">
      <p>您的店铺正在审核中，请耐心等待管理员审核通过</p>
    </el-alert>
    
    <el-alert v-if="store?.auditStatus === 2" type="error" :closable="false" style="margin-bottom: 20px">
      <p>您的店铺审核未通过</p>
      <p v-if="store.rejectReason">拒绝理由：{{ store.rejectReason }}</p>
    </el-alert>
    
    <el-card v-if="store && store.auditStatus === 1">
      <div class="store-info">
        <el-avatar :size="64" shape="square">{{ store.name[0] }}</el-avatar>
        <div class="store-detail">
          <h3>{{ store.name }}</h3>
          <p>{{ store.description }}</p>
          <p class="store-stats">
            <el-icon><Star /></el-icon>
            <span>{{ store.rating || 0 }}</span>
            <span>月售{{ monthSales }}</span>
            <span>{{ store.openTime || '09:00-22:00' }}</span>
          </p>
        </div>
        <div class="store-actions">
          <el-button type="primary" @click="editStore">编辑店铺</el-button>
          <el-dropdown trigger="click" @command="handleStatusChange">
            <el-button type="primary" :icon="statusIcon">
              {{ statusText }}
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="0">
                  <el-icon><Close /></el-icon> 打烊
                </el-dropdown-item>
                <el-dropdown-item command="1">
                  <el-icon><VideoPlay /></el-icon> 营业中
                </el-dropdown-item>
                <el-dropdown-item command="2">
                  <el-icon><CoffeeCup /></el-icon> 休息中
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      
      <el-divider />
      
      <div class="earnings-section">
        <h4 class="section-title">收益统计</h4>
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-value">¥{{ dashboardData.todayEarnings }}</div>
            <div class="stat-label">今日收益</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">¥{{ dashboardData.totalEarnings }}</div>
            <div class="stat-label">累计收益</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ dashboardData.totalOrders }}</div>
            <div class="stat-label">总单量</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">¥{{ dashboardData.balance }}</div>
            <div class="stat-label">账户余额</div>
          </div>
        </div>
        <div class="earnings-actions">
          <el-button type="primary" @click="$router.push('/merchant/earnings')">查看收益明细</el-button>
        </div>
      </div>
      
      <el-divider />
      
      <div class="menu-section">
        <h4 class="section-title">快捷操作</h4>
        <el-menu>
          <el-menu-item index="1" @click="$router.push('/merchant/categories')">
            <el-icon><Folder /></el-icon>
            <span>分类管理</span>
          </el-menu-item>
          <el-menu-item index="2" @click="$router.push('/merchant/dishes')">
            <el-icon><List /></el-icon>
            <span>菜品管理</span>
          </el-menu-item>
          <el-menu-item index="3" @click="$router.push('/merchant/orders')">
            <el-icon><List /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="4" @click="$router.push('/merchant/reviews')">
            <el-icon><ChatDotRound /></el-icon>
            <span>评论管理</span>
          </el-menu-item>
          <el-menu-item index="5" @click="$router.push('/merchant/delivery')">
            <el-icon><Van /></el-icon>
            <span>配送设置</span>
          </el-menu-item>
        </el-menu>
      </div>
    </el-card>
    
    <el-card v-else-if="!store">
      <el-empty description="您还没有店铺，请联系管理员" />
    </el-card>

    <el-dialog v-model="editDialogVisible" title="编辑店铺信息" width="700" :lock-scroll="false">
      <el-form :model="editForm" label-width="100">
        <!-- 店铺封面图片 -->
        <el-form-item label="店铺封面">
          <div class="cover-upload-section">
            <el-upload
              class="cover-uploader"
              action="/api/file/upload"
              :headers="uploadHeaders"
              list-type="picture-card"
              :show-file-list="false"
              :on-success="handleCoverSuccess"
              :before-upload="beforeImageUpload"
            >
              <div v-if="editForm.banner" class="cover-preview-small">
                <img :src="getCoverUrl(editForm.banner)" class="cover-image-small" />
                <div class="cover-mask-small">
                  <el-icon class="mask-icon-small"><Camera /></el-icon>
                </div>
              </div>
              <div v-else class="cover-placeholder-small">
                <el-icon class="upload-icon-small"><Plus /></el-icon>
                <p>上传封面</p>
              </div>
            </el-upload>
            <el-button v-if="editForm.banner" type="danger" :icon="Delete" circle @click="deleteCover" class="delete-cover-btn-small" />
            <p class="form-tip">建议尺寸：750x300，支持 jpg/png，不超过 2MB。未上传封面时，将自动使用店铺名称生成文字封面</p>
          </div>
        </el-form-item>
        
        <!-- 轮播图片 -->
        <el-form-item label="轮播图片">
          <div class="carousel-upload-section">
            <el-upload
              v-model:file-list="carouselFileList"
              action="/api/file/upload"
              :headers="uploadHeaders"
              list-type="picture-card"
              :on-success="handleCarouselSuccess"
              :on-remove="handleCarouselRemove"
              :before-upload="beforeImageUpload"
              :limit="9"
              :on-preview="handleCarouselPreview"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <p class="form-tip">最多可上传 9 张轮播图，单张不超过 2MB，点击可预览</p>
          </div>
        </el-form-item>
        
        <!-- 店铺描述 -->
        <el-form-item label="店铺描述">
          <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="请输入店铺描述" />
        </el-form-item>
        
        <!-- 营业时间 -->
        <el-form-item label="营业时间">
          <el-input v-model="editForm.openTime" placeholder="如：09:00-22:00" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="saveStore">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 图片预览对话框 -->
    <el-dialog v-model="previewVisible" title="图片预览" width="800" :fullscreen="true" destroy-on-close append-to-body>
      <div class="image-preview-container">
        <img :src="previewImage" class="preview-image" />
      </div>
    </el-dialog>
    
    <!-- Element Plus 图片预览组件 -->
    <el-image-viewer
      v-if="showImageViewer"
      :url-list="imageViewerUrlList"
      :initial-index="imageViewerInitialIndex"
      @close="closeImageViewer"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { getStore, updateStoreStatus, updateStore, getDashboardData } from '@/api/merchant'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ElImageViewer } from 'element-plus'
import { Close, VideoPlay, CoffeeCup, List, ArrowDown, Folder, Star, ChatDotRound, Plus, Camera, Delete, Van } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

const store = ref(null)
const monthSales = ref(0)
const dashboardData = ref({
  todayEarnings: 0,
  totalEarnings: 0,
  totalOrders: 0,
  balance: 0
})
const editDialogVisible = ref(false)
const previewVisible = ref(false)
const previewImage = ref('')
const showImageViewer = ref(false)
const imageViewerUrlList = ref([])
const imageViewerInitialIndex = ref(0)
const carouselFileList = ref([])
const editForm = ref({ 
  description: '', 
  openTime: '',
  banner: '',
  carouselImages: []
})

const statusText = computed(() => {
  if (!store.value) return ''
  return { 0: '打烊', 1: '营业中', 2: '休息中' }[store.value.status]
})

const statusIcon = computed(() => {
  if (!store.value) return Close
  return { 0: Close, 1: VideoPlay, 2: CoffeeCup }[store.value.status]
})

// 获取上传头信息（包含 token）
const uploadHeaders = computed(() => ({
  'Authorization': 'Bearer ' + userStore.token
}))

// 获取图片 URL
const getCoverUrl = (imagePath) => {
  if (!imagePath) return ''
  if (imagePath.startsWith('http')) return imagePath
  return imagePath.startsWith('/api') ? imagePath : '/api' + imagePath
}

// 图片上传前的验证
function beforeImageUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
  }
  return isImage && isLt2M
}

// 封面上传成功回调
function handleCoverSuccess(response, file) {
  if (response.code === 200 || response.code === 0) {
    editForm.value.banner = response.data
    ElMessage.success('封面上传成功')
  } else {
    ElMessage.error(response.msg || response.message || '上传失败')
  }
}

// 删除封面
function deleteCover() {
  ElMessageBox.confirm('确定要删除封面图片吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    lockScroll: false
  }).then(() => {
    editForm.value.banner = ''
    ElMessage.success('封面已删除')
  }).catch(() => {
    // 用户取消
  })
}

// 轮播图上传成功回调
function handleCarouselSuccess(response, file, fileList) {
  console.log('轮播图上传成功:', response, file, fileList)
  if (response.code === 200 || response.code === 0) {
    // 保存上传成功的图片 URL
    editForm.value.carouselImages = fileList
      .filter(item => item.response && (item.response.code === 200 || item.response.code === 0))
      .map(item => item.response.data)
    console.log('更新后的轮播图列表:', editForm.value.carouselImages)
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.msg || response.message || '上传失败')
  }
}

// 轮播图移除回调
function handleCarouselRemove(file, fileList) {
  console.log('轮播图移除:', file, fileList)
  editForm.value.carouselImages = fileList
    .filter(item => item.response && (item.response.code === 200 || item.response.code === 0))
    .map(item => item.response.data)
  console.log('移除后的轮播图列表:', editForm.value.carouselImages)
}

// 轮播图预览
function handleCarouselPreview(file) {
  const imageUrl = getCoverUrl(file.response?.data || file.url)
  
  // 获取所有图片的 URL 列表
  const urlList = carouselFileList.value
    .filter(item => item.response && (item.response.code === 200 || item.response.code === 0))
    .map(item => getCoverUrl(item.response.data))
  
  // 获取当前点击图片的索引
  const currentIndex = carouselFileList.value.findIndex(item => item.uid === file.uid)
  
  // 设置预览图片
  imageViewerUrlList.value = urlList
  imageViewerInitialIndex.value = currentIndex
  showImageViewer.value = true
}

// 关闭图片预览
function closeImageViewer() {
  showImageViewer.value = false
}

// 预览图片
function previewImageFunc(url) {
  previewImage.value = getCoverUrl(url)
  previewVisible.value = true
}

async function loadStore() {
  try {
    const storeRes = await getStore()
    store.value = storeRes || null
    
    if (store.value) {
      try {
        const dashboardRes = await getDashboardData()
        if (dashboardRes) {
          if (dashboardRes.monthSales) {
            monthSales.value = dashboardRes.monthSales
          }
          if (dashboardRes.todayEarnings !== undefined) {
            dashboardData.value.todayEarnings = dashboardRes.todayEarnings
          }
          if (dashboardRes.totalEarnings !== undefined) {
            dashboardData.value.totalEarnings = dashboardRes.totalEarnings
          }
          if (dashboardRes.totalOrders !== undefined) {
            dashboardData.value.totalOrders = dashboardRes.totalOrders
          }
          if (dashboardRes.balance !== undefined) {
            dashboardData.value.balance = dashboardRes.balance
          }
        }
      } catch (e) {
        console.error('加载销售数据失败:', e)
      }
    }
  } catch (e) {
    console.error('加载店铺信息失败:', e)
    ElMessage.error('加载店铺信息失败：' + (e?.message || '未知错误'))
  }
}

async function editStore() {
  // 处理轮播图：后端返回的是逗号分隔的字符串，需要转换为数组
  let carouselImages = []
  const carouselStr = store.value?.carouselImages
  if (carouselStr && typeof carouselStr === 'string') {
    carouselImages = carouselStr.split(',').filter(url => url.trim())
  } else if (Array.isArray(carouselStr)) {
    carouselImages = carouselStr
  }
  
  editForm.value = {
    id: store.value?.id,
    name: store.value?.name,
    description: store.value?.description || '',
    openTime: store.value?.openTime || '09:00-22:00',
    banner: store.value?.banner || '',
    carouselImages: carouselImages
  }
  // 初始化轮播图文件列表
  carouselFileList.value = carouselImages.map(url => ({
    name: url.split('/').pop(),
    url: getCoverUrl(url),
    response: { code: 200, data: url }
  }))
  console.log('初始化轮播图列表:', carouselFileList.value)
  editDialogVisible.value = true
}

async function saveStore() {
  try {
    // 保存店铺信息，包括封面和轮播图
    const saveData = {
      id: editForm.value.id,
      name: editForm.value.name,
      description: editForm.value.description,
      openTime: editForm.value.openTime,
      banner: editForm.value.banner,
      carouselImages: editForm.value.carouselImages
    }
    await updateStore(saveData)
    ElMessage.success('保存成功')
    editDialogVisible.value = false
    await loadStore()
  } catch (e) {
    console.error('保存失败:', e)
    ElMessage.error(e?.message || '保存失败')
  }
}

async function updateStatus(status) {
  try {
    await updateStoreStatus(status)
    // 立即更新本地状态，使按钮文字和图标立即变化
    if (store.value) {
      store.value.status = status
    }
    ElMessage.success('状态已更新')
  } catch (e) {
    console.error('更新状态失败:', e)
    ElMessage.error(e?.message || '更新失败')
    loadStore()
  }
}

async function handleStatusChange(command) {
  const status = parseInt(command)
  if (status === store.value?.status) {
    return
  }
  await updateStatus(status)
}

onMounted(() => {
  loadStore()
})
</script>

<style scoped>
.page {
  margin: 0 auto;
  padding: 20px;
}

.page h1 {
  margin-bottom: 20px;
  color: #333;
}

.store-info {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}

.store-detail {
  flex: 1;
}

.store-detail h3 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 18px;
}

.store-detail p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.store-stats {
  margin: 8px 0 0 0;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #666;
  font-size: 13px;
}

.store-stats .el-icon {
  color: #ff9900;
}

.store-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.section-title {
  margin: 0 0 15px 0;
  font-size: 16px;
  color: #333;
  font-weight: 600;
}

.earnings-section {
  margin-bottom: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
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

.earnings-actions {
  margin-top: 16px;
  text-align: center;
}

.menu-section {
  margin-bottom: 20px;
}

/* 封面上传样式 */
.cover-upload-section {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.cover-uploader {
  flex-shrink: 0;
}

.cover-uploader :deep(.el-upload) {
  width: 100px;
  height: 100px;
}

.cover-uploader :deep(.el-upload-dragger) {
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.cover-preview-small {
  position: relative;
  width: 100px;
  height: 100px;
  overflow: hidden;
  border-radius: 6px;
  cursor: pointer;
}

.cover-image-small {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-mask-small {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.cover-preview-small:hover .cover-mask-small {
  opacity: 1;
}

.mask-icon-small {
  font-size: 20px;
}

.cover-placeholder-small {
  width: 100px;
  height: 100px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: border-color 0.3s;
  background: #fafafa;
}

.cover-placeholder-small:hover {
  border-color: #409EFF;
}

.upload-icon-small {
  font-size: 24px;
  color: #8c939d;
  margin-bottom: 4px;
}

.cover-placeholder-small p {
  margin: 0;
  color: #666;
  font-size: 12px;
}

.delete-cover-btn-small {
  flex-shrink: 0;
  margin-top: 0;
}

.form-tip {
  margin: 8px 0 0 0;
  font-size: 12px;
  color: #999;
}

/* 轮播图上传样式 */
.carousel-upload-section {
  width: 100%;
}

.carousel-upload-section :deep(.el-upload-list) {
  margin-bottom: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.carousel-upload-section :deep(.el-upload-list__item) {
  width: 100px !important;
  height: 100px !important;
  flex-shrink: 0;
}

.carousel-upload-section :deep(.el-upload-list__item-thumbnail) {
  object-fit: cover;
  width: 100%;
  height: 100%;
}

.carousel-upload-section :deep(.el-upload) {
  width: 100px;
  height: 100px;
}

/* 图片预览样式 */
.image-preview-container {
  width: 100%;
  height: 80vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}

.preview-image-component {
  max-width: 100%;
  max-height: 100%;
}

.preview-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

/* 轮播图上传组件的预览样式 */
.carousel-upload-section :deep(.el-upload-list__item) {
  cursor: pointer;
}
</style>
