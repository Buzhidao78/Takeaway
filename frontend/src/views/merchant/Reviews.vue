<template>
  <div class="page">
    <h1>评价管理</h1>
    <el-card>
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="菜品分类">
          <el-select v-model="filterForm.categoryId" placeholder="全部分类" clearable style="width: 180px">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="评分">
          <el-select v-model="filterForm.rating" placeholder="全部评分" clearable style="width: 120px">
            <el-option label="5 星" :value="5" />
            <el-option label="4 星" :value="4" />
            <el-option label="3 星" :value="3" />
            <el-option label="2 星" :value="2" />
            <el-option label="1 星" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input v-model="filterForm.keyword" placeholder="搜索用户或内容" clearable style="width: 200px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <el-divider />

      <div class="stats-section" v-if="stats">
        <h4 class="section-title">评价统计</h4>
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-value">{{ stats.totalCount }}</div>
            <div class="stat-label">总评价数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ stats.averageRating || 0 }}</div>
            <div class="stat-label">平均评分</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ stats.positiveRate || 0 }}%</div>
            <div class="stat-label">好评率</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ stats.withImageCount }}</div>
            <div class="stat-label">有图评价</div>
          </div>
        </div>
      </div>

      <el-divider />

      <div class="reviews-list" v-loading="loading">
        <div v-for="review in reviews" :key="review.id" class="review-item">
          <div class="review-header">
            <div class="user-info">
              <el-avatar :size="40" :src="review.userAvatar">{{ review.userName?.charAt(0) || '用' }}</el-avatar>
              <div class="user-detail">
                <div class="user-name">{{ review.userName || '匿名用户' }}</div>
                <div class="review-time">{{ review.createTime }}</div>
              </div>
            </div>
            <div class="rating-section">
              <el-rate v-model="review.rating" disabled :colors="['#99A9BF', '#F7BA2A', '#FF9900']" />
              <el-tag :type="getRatingType(review.rating)" size="small" style="margin-left: 10px">
                {{ getRatingText(review.rating) }}
              </el-tag>
            </div>
          </div>

          <div class="review-content">
            <div class="product-info">
              <el-tag v-if="review.dishName" size="small">{{ review.dishName }}</el-tag>
              <span class="category-tag" v-if="review.categoryName">{{ review.categoryName }}</span>
            </div>
            <p class="content-text">{{ review.content }}</p>
            
            <div class="review-images" v-if="review.images && review.images.length">
              <el-image 
                v-for="(img, idx) in review.images" 
                :key="idx"
                :src="getImageUrl(img)" 
                fit="cover"
                style="width: 80px; height: 80px; margin-right: 8px; border-radius: 4px; cursor: pointer"
                :preview-src-list="review.images.map(getImageUrl)"
              />
            </div>
          </div>

          <div class="review-footer">
            <div class="reply-section" v-if="review.replyContent">
              <el-tag size="small" type="warning">商家回复</el-tag>
              <p class="reply-content">{{ review.replyContent }}</p>
              <span class="reply-time" v-if="review.replyTime">{{ review.replyTime }}</span>
            </div>
            <el-button 
              v-else 
              type="primary" 
              size="small" 
              @click="openReplyDialog(review)"
              plain
            >
              回复评价
            </el-button>
          </div>
        </div>

        <el-empty v-if="reviews.length === 0" description="暂无评价" />
      </div>

      <el-pagination
        v-if="total > 0"
        layout="total, prev, pager, next"
        :total="total"
        v-model:current-page="page"
        @current-change="loadReviews"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 回复对话框 -->
    <el-dialog v-model="replyDialogVisible" title="回复评价" width="500px" :lock-scroll="false">
      <el-form :model="replyForm" label-width="80px">
        <el-form-item label="评价内容">
          <div class="reply-preview">{{ currentReview?.content }}</div>
        </el-form-item>
        <el-form-item label="回复内容">
          <el-input
            v-model="replyForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入回复内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getMerchantReviews, replyReview } from '@/api/review'
import { categories as fetchCategories } from '@/api/merchant'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const reviews = ref([])
const categories = ref([])
const total = ref(0)
const page = ref(1)
const stats = ref(null)

const filterForm = reactive({
  categoryId: null,
  rating: null,
  keyword: ''
})

const replyDialogVisible = ref(false)
const currentReview = ref(null)
const replyForm = reactive({
  content: ''
})
const submitting = ref(false)

const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return path.startsWith('/api') ? path : '/api' + path
}

const getRatingType = (rating) => {
  if (rating >= 5) return 'success'
  if (rating >= 4) return 'primary'
  if (rating >= 3) return 'warning'
  return 'danger'
}

const getRatingText = (rating) => {
  if (rating >= 5) return '好评'
  if (rating >= 4) return '满意'
  if (rating >= 3) return '一般'
  return '差评'
}

async function loadReviews() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: 10,
      ...filterForm
    }
    const res = await getMerchantReviews(params)
    reviews.value = (res.list || []).map(review => {
      if (review.images && typeof review.images === 'string') {
        try {
          review.images = JSON.parse(review.images)
        } catch (e) {
          review.images = []
        }
      }
      if (!Array.isArray(review.images)) {
        review.images = []
      }
      return review
    })
    total.value = res.total || 0
    
    // 加载统计数据
    loadStats()
  } catch (error) {
    console.error('加载评价失败:', error)
    ElMessage.error('加载评价失败')
  } finally {
    loading.value = false
  }
}

async function loadStats() {
  try {
    const res = await getMerchantReviews({ page: 1, pageSize: 1, needStats: true })
    stats.value = res.stats || null
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

async function loadCategories() {
  try {
    const data = await fetchCategories()
    categories.value = Array.isArray(data) ? data : []
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

function handleSearch() {
  page.value = 1
  loadReviews()
}

function handleReset() {
  filterForm.categoryId = null
  filterForm.rating = null
  filterForm.keyword = ''
  page.value = 1
  loadReviews()
}

function openReplyDialog(review) {
  currentReview.value = review
  replyForm.content = ''
  replyDialogVisible.value = true
}

async function submitReply() {
  if (!replyForm.content.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  submitting.value = true
  try {
    await replyReview(currentReview.value.id, replyForm.content)
    ElMessage.success('回复成功')
    replyDialogVisible.value = false
    loadReviews()
  } catch (error) {
    console.error('回复失败:', error)
    ElMessage.error('回复失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadCategories()
  loadReviews()
})
</script>

<style scoped>
.stats-section {
  margin-bottom: 20px;
}

.section-title {
  margin: 0 0 16px;
  font-size: 16px;
  font-weight: 500;
  color: #333;
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

.review-item {
  padding: 20px 0;
  border-bottom: 1px solid #eee;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-detail {
  margin-left: 12px;
}

.user-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.review-time {
  font-size: 12px;
  color: #999;
}

.rating-section {
  display: flex;
  align-items: center;
}

.review-content {
  margin-bottom: 16px;
}

.product-info {
  margin-bottom: 8px;
}

.category-tag {
  margin-left: 8px;
  font-size: 12px;
}

.content-text {
  margin: 0;
  line-height: 1.6;
  color: #333;
}

.review-images {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
}

.review-footer {
  border-top: 1px solid #f5f5f5;
  padding-top: 16px;
}

.reply-section {
  background: #fff7e6;
  padding: 12px;
  border-radius: 4px;
}

.reply-content {
  margin: 8px 0 0 0;
  line-height: 1.6;
}

.reply-time {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
  display: block;
}

.reply-preview {
  background: #f5f5f5;
  padding: 8px 12px;
  border-radius: 4px;
  margin-bottom: 12px;
}
</style>
