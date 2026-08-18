<template>
  <div class="dish-detail" v-if="dish">
    <div class="container">
      <div class="dish-header">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="dish-images">
              <!-- 多张图片时显示轮播 -->
              <div class="carousel-container" v-if="images && images.length > 0">
                <el-carousel 
                  trigger="click" 
                  arrow="always" 
                  height="400px" 
                  :autoplay="false"
                  @change="handleCarouselChange"
                >
                  <el-carousel-item v-for="(img, idx) in images" :key="idx">
                    <div class="carousel-item-content">
                      <el-image 
                        :src="getImageUrl(img)" 
                        fit="contain" 
                        style="width: 100%; height: 100%"
                        :preview-src-list="imagePreviewList"
                        :initial-index="idx"
                        class="dish-carousel-image"
                        preview-teleported
                      />
                    </div>
                  </el-carousel-item>
                </el-carousel>
                <!-- 页数指示器 - 细长条样式 -->
                <div class="carousel-dots" v-if="images.length > 1">
                  <span 
                    v-for="(img, idx) in images" 
                    :key="idx"
                    class="dot"
                    :class="{ active: currentImageIndex === idx }"
                    @click.stop="switchTo(idx)"
                  ></span>
                </div>
              </div>
              <!-- 单张图片时直接显示 -->
              <el-image 
                v-else-if="dish.image"
                :src="getImageUrl(dish.image)" 
                fit="contain" 
                style="width: 100%; height: 400px; cursor: pointer;"
                :preview-src-list="[getImageUrl(dish.image)]"
                class="dish-single-image"
              />
              <!-- 没有图片时显示占位符 -->
              <div v-else class="no-image-placeholder">
                <el-icon :size="60"><Picture /></el-icon>
                <p>暂无图片</p>
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="dish-info">
              <h1 class="dish-title">{{ dish.name }}</h1>
              <div class="dish-meta">
                <span class="sales">月售{{ dish.salesCount || 0 }}</span>
                <span class="rating" v-if="dish.rating">
                  <el-rate v-model="dish.rating" disabled size="small" :colors="['#99A9BF', '#F7BA2A', '#FF9900']" />
                  <span class="rating-text">{{ dish.rating }}</span>
                </span>
              </div>
              <div class="dish-price-section">
                <span class="current-price">¥{{ dish.price }}</span>
                <span class="original-price" v-if="dish.originPrice">¥{{ dish.originPrice }}</span>
              </div>
              <div class="dish-description">
                <h4>菜品描述</h4>
                <p>{{ dish.description || '暂无描述' }}</p>
              </div>
              
              <div class="purchase-section">
                <h4>选择数量</h4>
                <el-input-number v-model="quantity" :min="1" :max="99" size="large" style="margin-right: 16px" />
              </div>
              
              <div class="remark-section">
                <h4>备注 <span class="optional">(选填)</span></h4>
                <el-input
                  v-model="remark"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入备注，例如：不要香菜、少辣等"
                />
              </div>
              
              <div class="action-buttons">
                <el-button type="primary" size="large" @click="addToCart" :loading="adding">
                  加入购物车
                </el-button>
                <el-button type="warning" size="large" @click="buyNow" :loading="adding">
                  立即购买
                </el-button>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
      
      <div class="reviews-section">
        <h2 class="section-title">用户评价 <span class="review-count">({{ reviewTotal }}条)</span></h2>
        
        <div class="review-filter">
          <el-radio-group v-model="reviewFilter" size="small" @change="loadReviews">
            <el-radio-button value="">全部</el-radio-button>
            <el-radio-button value="withImage">有图</el-radio-button>
            <el-radio-button value="high">好评</el-radio-button>
            <el-radio-button value="low">差评</el-radio-button>
          </el-radio-group>
        </div>
        
        <div class="reviews-list" v-loading="reviewsLoading">
          <div v-for="review in reviews" :key="review.id" class="review-item">
            <div class="review-header">
              <div class="user-info">
                <el-avatar :size="36">{{ (review.userName || '用').charAt(0) }}</el-avatar>
                <div class="user-detail">
                  <div class="user-name">{{ review.userName || '匿名用户' }}</div>
                  <div class="review-time">{{ review.createTime }}</div>
                </div>
              </div>
              <el-rate v-model="review.rating" disabled size="small" :colors="['#99A9BF', '#F7BA2A', '#FF9900']" />
            </div>
            
            <div class="review-content">
              <p>{{ review.content }}</p>
              <div class="review-images" v-if="hasImages(review)">
                <el-image 
                  v-for="(img, idx) in getReviewImages(review)" 
                  :key="idx"
                  :src="getImageUrl(img)" 
                  fit="cover"
                  style="width: 100px; height: 100px; margin-right: 8px; margin-top: 8px; border-radius: 4px; cursor: pointer"
                  :preview-src-list="getReviewImages(review).map(getImageUrl)"
                />
              </div>
            </div>
            
            <!-- 点赞点踩 -->
            <div class="review-actions">
              <el-button 
                text 
                size="small" 
                @click="handleLike(review)"
                :class="{ 'active': review.userVote === 1 }"
              >
                <span :class="review.userVote === 1 ? 'icon-liked' : ''" style="font-size: 16px;">👍</span>
                {{ review.likeCount || 0 }}
              </el-button>
              <el-button 
                text 
                size="small" 
                @click="handleDislike(review)"
                :class="{ 'active': review.userVote === -1 }"
              >
                <span :class="review.userVote === -1 ? 'icon-disliked' : ''" style="font-size: 16px;">👎</span>
                {{ review.dislikeCount || 0 }}
              </el-button>
            </div>
            
            <div class="reply-section" v-if="review.replyContent">
              <div class="reply-label">商家回复：</div>
              <p class="reply-content">{{ review.replyContent }}</p>
            </div>
          </div>
          
          <el-empty v-if="reviews.length === 0" description="暂无评价" />
        </div>
        
        <el-pagination
          v-if="reviewTotal > 0"
          layout="prev, pager, next"
          :total="reviewTotal"
          v-model:current-page="reviewPage"
          @current-change="loadReviews"
          style="margin-top: 20px; justify-content: center"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getDishDetail } from '@/api/store'
import { getDishReviews, likeReview as apiLikeReview, dislikeReview as apiDislikeReview, getUserVote } from '@/api/product-review'
import { addCart } from '@/api/cart'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Picture, Select, Close } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const dish = ref(null)
const quantity = ref(1)
const remark = ref('')
const adding = ref(false)
const images = ref([])
const currentImageIndex = ref(0)

const reviews = ref([])
const reviewsLoading = ref(false)
const reviewPage = ref(1)
const reviewTotal = ref(0)
const reviewFilter = ref('')

// 图片预览列表（计算属性）
const imagePreviewList = computed(() => {
  return images.value.map(getImageUrl)
})

const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return path.startsWith('/api') ? path : '/api' + path
}

// 处理评价图片，兼容不同的数据格式
const getReviewImages = (review) => {
  if (!review.images) return []
  // 如果已经是数组，直接返回
  if (Array.isArray(review.images)) {
    return review.images.filter(img => img)
  }
  // 如果是字符串，尝试解析
  if (typeof review.images === 'string') {
    try {
      // 尝试解析 JSON
      if (review.images.startsWith('[')) {
        const parsed = JSON.parse(review.images)
        return Array.isArray(parsed) ? parsed : []
      }
      // 如果是逗号分隔的字符串
      if (review.images.includes(',')) {
        return review.images.split(',').map(s => s.trim()).filter(s => s)
      }
      // 单个图片 URL
      return review.images ? [review.images] : []
    } catch (e) {
      console.warn('解析 images 失败:', e)
      return []
    }
  }
  return []
}

// 判断是否有图片
const hasImages = (review) => {
  return getReviewImages(review).length > 0
}

// 打开图片预览
const openImagePreview = (index) => {
  const urlList = imagePreviewList.value
  if (urlList.length === 0) {
    ElMessage.warning('没有图片')
    return
  }
  
  console.log('打开预览，索引:', index, '图片列表:', urlList)
}

// 轮播图切换事件
const handleCarouselChange = (index) => {
  currentImageIndex.value = index
  console.log('轮播图切换，当前索引:', index)
}

// 打开预览
const openPreview = (index) => {
  const urlList = imagePreviewList.value
  if (urlList.length === 0) {
    ElMessage.warning('没有图片')
    return
  }
  
  // 使用 Element Plus 的 ElImagePreview 方法
  // 注意：需要在 el-image 上设置 preview-src-list 才能正常工作
  console.log('打开预览，索引:', index)
}

// 切换到指定图片
const switchTo = (index) => {
  currentImageIndex.value = index
  console.log('切换到图片:', index)
}

async function loadDish() {
  try {
    const id = route.params.id
    console.log('加载菜品详情，ID:', id)
    const data = await getDishDetail(id)
    console.log('菜品数据:', data)
    dish.value = data
    
    // 处理图片：优先使用 images，如果没有则使用 image
    images.value = []
    if (data.images) {
      try {
        const imgs = data.images.split(',').filter(img => img && img.trim())
        if (imgs.length > 0) {
          images.value = imgs
          console.log('多张图片:', imgs)
        }
      } catch (e) {
        console.error('解析图片失败:', e)
      }
    }
    
    // 如果没有多张图片，但有主图
    if (images.value.length === 0 && data.image) {
      images.value = [data.image]
    }
    
    console.log('最终图片列表:', images.value, '数量:', images.value.length)
    
    // 加载评价
    loadReviews()
  } catch (error) {
    console.error('加载菜品详情失败:', error)
    ElMessage.error('加载菜品详情失败')
  }
}

async function loadReviews() {
  reviewsLoading.value = true
  try {
    const id = route.params.id
    const params = {
      page: reviewPage.value,
      size: 20
    }
    
    // 添加筛选条件
    if (reviewFilter.value === 'withImage') {
      params.withImage = true
    } else if (reviewFilter.value === 'high') {
      params.rating = 1
    } else if (reviewFilter.value === 'low') {
      params.rating = 0
    }
    
    console.log('加载评价，菜品 ID:', id, '参数:', params)
    
    // 调用评价 API
    const res = await getDishReviews(id, params)
    console.log('评价响应:', res)
    
    // 处理不同的返回格式
    if (res.data) {
      reviews.value = res.data
      reviewTotal.value = res.total || 0
    } else if (Array.isArray(res)) {
      reviews.value = res
      reviewTotal.value = res.length
    } else {
      reviews.value = []
      reviewTotal.value = 0
    }
    
    // 加载每个评价的用户投票状态
    if (userStore.isLogin) {
      for (const review of reviews.value) {
        try {
          const vote = await getUserVote(review.id)
          review.userVote = vote
        } catch (e) {
          console.error('加载投票状态失败:', e)
          review.userVote = 0
        }
      }
    }
    
    console.log('处理后的评价数据:', reviews.value, '总数:', reviewTotal.value)
  } catch (error) {
    console.error('加载评价失败:', error)
    // 未登录时也会加载评价，所以不应该报错
    // 只有真正发生错误时才显示提示
    if (error?.response?.status === 401 || error?.code === 401) {
      // 401 错误时显示空列表，不提示错误
      console.log('未登录，显示空评价列表')
      reviews.value = []
      reviewTotal.value = 0
    } else {
      // 其他错误才提示用户
      ElMessage.error('加载评价失败，请稍后重试')
      reviews.value = []
      reviewTotal.value = 0
    }
  } finally {
    reviewsLoading.value = false
  }
}

// 处理点赞
const handleLike = async (review) => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    const res = await apiLikeReview(review.id)
    // 更新本地数据
    review.likeCount = res.likeCount
    review.dislikeCount = res.dislikeCount
    review.userVote = res.userVote
    ElMessage.success(res.action === 'cancelled' ? '已取消点赞' : '点赞成功')
  } catch (error) {
    ElMessage.error(error?.msg || '点赞失败')
  }
}

// 处理点踩
const handleDislike = async (review) => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    const res = await apiDislikeReview(review.id)
    // 更新本地数据
    review.likeCount = res.likeCount
    review.dislikeCount = res.dislikeCount
    review.userVote = res.userVote
    ElMessage.success(res.action === 'cancelled' ? '已取消点踩' : '已点踩')
  } catch (error) {
    ElMessage.error(error?.msg || '操作失败')
  }
}

async function addToCart() {
  try {
    if (!userStore.isLogin) {
      ElMessage.warning('请先登录')
      return
    }
    
    adding.value = true
    await addCart(dish.value.storeId, dish.value.id, quantity.value, remark.value)
    await cartStore.fetchCart()
    ElMessage.success('已加入购物车')
  } catch (error) {
    console.error('添加购物车失败:', error)
    ElMessage.error(error?.msg || '添加购物车失败')
  } finally {
    adding.value = false
  }
}

async function buyNow() {
  try {
    if (!userStore.isLogin) {
      ElMessage.warning('请先登录')
      return
    }
    
    adding.value = true
    await addCart(dish.value.storeId, dish.value.id, quantity.value, remark.value)
    await cartStore.fetchCart()
    router.push('/cart')
  } catch (error) {
    console.error('购买失败:', error)
    ElMessage.error(error?.msg || '购买失败')
  } finally {
    adding.value = false
  }
}

onMounted(loadDish)
</script>

<style scoped>
.dish-detail {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.dish-header {
  padding: 24px;
}

.dish-images {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  position: relative;
}

/* 没有图片时的占位符 */
.no-image-placeholder {
  width: 100%;
  height: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #909399;
}

.no-image-placeholder p {
  margin-top: 16px;
  font-size: 14px;
}

/* 轮播图容器 */
.carousel-container {
  position: relative;
}

/* 轮播图项目内容 */
.carousel-item-content {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

/* 轮播图图片 */
.dish-carousel-image,
.dish-single-image {
  width: 100%;
  height: 100%;
}

.dish-images :deep(.el-image) {
  width: 100%;
  height: 100%;
  display: block;
}

.dish-images :deep(.el-image__wrapper) {
  width: 100%;
  height: 100%;
}

.dish-images :deep(.el-image__inner) {
  cursor: pointer;
  transition: opacity 0.3s;
}

.dish-images :deep(.el-image__inner:hover) {
  opacity: 0.9;
}

.dish-images :deep(.el-carousel__item) {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 页数指示器 - 细长条圆点样式 */
.carousel-dots {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 10px;
  z-index: 20;
  pointer-events: none;
  background: rgba(0, 0, 0, 0.3);
  padding: 8px 16px;
  border-radius: 20px;
}

.carousel-dots .dot {
  width: 20px;
  height: 4px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 2px;
  cursor: pointer;
  transition: all 0.3s ease;
  pointer-events: auto;
}

.carousel-dots .dot:hover {
  background: rgba(255, 255, 255, 0.8);
  transform: scaleY(1.2);
}

.carousel-dots .dot.active {
  background: #fff;
  width: 30px;
  box-shadow: 0 0 8px rgba(255, 255, 255, 0.6);
}

.dish-info {
  padding-left: 20px;
}

.dish-title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #333;
}

.dish-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  color: #999;
}

.sales {
  font-size: 14px;
}

.rating {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rating-text {
  font-size: 14px;
  color: #ff6b35;
}

.dish-price-section {
  margin-bottom: 24px;
}

.current-price {
  font-size: 32px;
  font-weight: bold;
  color: #ff6b35;
  margin-right: 12px;
}

.original-price {
  font-size: 18px;
  color: #999;
  text-decoration: line-through;
}

.dish-description {
  margin-bottom: 24px;
  padding: 16px;
  background: #f5f5f5;
  border-radius: 8px;
}

.dish-description h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #333;
}

.dish-description p {
  margin: 0;
  color: #666;
  line-height: 1.6;
}

.purchase-section {
  margin-bottom: 24px;
}

.purchase-section h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #333;
}

.remark-section {
  margin-bottom: 24px;
}

.remark-section h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #333;
}

.optional {
  font-weight: normal;
  color: #999;
  font-size: 14px;
}

.action-buttons {
  display: flex;
  gap: 16px;
}

.action-buttons .el-button {
  flex: 1;
}

.reviews-section {
  padding: 24px;
  border-top: 1px solid #eee;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #333;
}

.review-count {
  font-weight: normal;
  color: #999;
  font-size: 16px;
}

.review-filter {
  margin-bottom: 20px;
}

.reviews-list {
  min-height: 200px;
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
  margin-bottom: 12px;
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

.review-content p {
  margin: 0;
  line-height: 1.6;
  color: #333;
}

.review-images {
  display: flex;
  flex-wrap: wrap;
  margin-top: 12px;
}

.reply-section {
  margin-top: 12px;
  padding: 12px;
  background: #fff7e6;
  border-radius: 4px;
}

.reply-label {
  font-weight: 500;
  margin-bottom: 8px;
  color: #ff9800;
}

.reply-content {
  margin: 0;
  line-height: 1.6;
  color: #666;
}

/* 点赞点踩按钮样式 */
.review-actions {
  display: flex;
  gap: 16px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.review-actions .el-button {
  color: #909399;
  font-size: 13px;
  padding: 4px 8px;
}

.review-actions .el-button:hover {
  color: #409EFF;
}

.review-actions .el-button.active {
  color: #409EFF;
}

.review-actions .el-button.active .icon-disliked {
  color: #f56c6c;
}

.review-actions .el-icon {
  margin-right: 4px;
  font-size: 16px;
}

.review-actions .icon-liked {
  color: #409EFF;
}

.review-actions .icon-disliked {
  color: #f56c6c;
}
</style>
