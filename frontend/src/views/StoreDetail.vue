<template>
  <div class="store-detail" v-if="store">
    <div class="store-header" :style="{ background: 'linear-gradient(135deg,#ff6b35,#ff8f65)' }">
      <h1>{{ store.name }}</h1>
      <p>{{ store.description }}</p>
      <div class="meta"><span><el-icon><StarFilled /></el-icon> {{ store.rating }}</span> 月售{{ store.salesCount }} {{ store.openTime }}</div>
    </div>
    <div class="page">
      <div class="category-tabs">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="全部" :name="0" />
          <el-tab-pane v-for="c in categories" :key="c.id" :label="c.name" :name="c.id" />
          <el-tab-pane label="店铺评价" :name="'reviews'" />
        </el-tabs>
      </div>
      
      <!-- 菜品列表 -->
      <div class="dish-list" v-if="activeTab !== 'reviews' && dishes.length > 0">
        <div v-for="d in dishes" :key="d.id" class="dish-item" @click="goToDishDetail(d.id)">
          <div class="dish-image">
            <div v-if="!d.image" class="no-image-placeholder">
              <el-icon :size="30"><Picture /></el-icon>
              <span>暂无图片</span>
            </div>
            <el-image 
              v-else 
              :src="getImageUrl(d.image)" 
              fit="cover" 
              style="width: 80px; height: 80px; border-radius: 8px" 
            />
          </div>
          <div class="dish-info">
            <h4>{{ d.name }}</h4>
            <p class="dish-desc">{{ d.description }}</p>
            <div class="dish-price">
              <span class="price">¥{{ d.price }}</span>
              <span class="origin" v-if="d.originPrice && d.originPrice > d.price">¥{{ d.originPrice }}</span>
              <span class="discount-tag" v-if="d.originPrice && d.originPrice > d.price">
                {{ calculateDiscount(d.price, d.originPrice) }}折
              </span>
            </div>
          </div>
          <div class="dish-action" @click.stop>
            <el-input-number v-model="d._qty" :min="0" :max="99" size="small" @change="addCart(d)" />
          </div>
        </div>
      </div>
      <el-empty v-if="activeTab !== 'reviews' && dishes.length === 0" description="暂无菜品" />
      
      <!-- 店铺评价内容 -->
      <div class="reviews-content" v-if="activeTab === 'reviews'">
        <div class="reviews-header">
          <h2 class="section-title">店铺评价 <span class="review-count">({{ storeReviewTotal }}条)</span></h2>
        </div>
        
        <!-- 筛选和排序 -->
        <div class="reviews-filter">
          <div class="filter-group">
            <span class="filter-label">评分：</span>
            <el-radio-group v-model="filterForm.rating" size="small" @change="loadStoreReviews">
              <el-radio-button :label="null">全部</el-radio-button>
              <el-radio-button :label="5">好评</el-radio-button>
              <el-radio-button :label="3">中评</el-radio-button>
              <el-radio-button :label="1">差评</el-radio-button>
            </el-radio-group>
          </div>
          
          <div class="filter-group">
            <span class="filter-label">筛选：</span>
            <el-checkbox v-model="filterForm.withImage" @change="loadStoreReviews" size="small">
              <el-icon><Picture /></el-icon> 有图
            </el-checkbox>
          </div>
          
          <div class="filter-group">
            <span class="filter-label">排序：</span>
            <el-radio-group v-model="filterForm.sortBy" size="small" @change="loadStoreReviews">
              <el-radio-button value="time">最新</el-radio-button>
              <el-radio-button value="like">最热</el-radio-button>
            </el-radio-group>
          </div>
        </div>
        
        <div class="reviews-list" v-loading="reviewsLoading">
          <div v-for="review in storeReviews" :key="review.id" class="review-item">
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
              <div class="review-images" v-if="review.images && getReviewImages(review).length">
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
            
            <!-- 追评内容 -->
            <div class="additional-review" v-if="review.additionalContent || (review.additionalImages && review.additionalImages.length > 0)">
              <div class="additional-label">
                <el-icon><Clock /></el-icon>
                <span>追评</span>
                <span class="additional-time" v-if="review.additionalTime">{{ formatTime(review.additionalTime) }}</span>
              </div>
              <div class="additional-content">
                <p v-if="review.additionalContent">{{ review.additionalContent }}</p>
                <div class="additional-images" v-if="review.additionalImages && getAdditionalImages(review).length">
                  <el-image 
                    v-for="(img, idx) in getAdditionalImages(review)" 
                    :key="idx"
                    :src="getImageUrl(img)" 
                    fit="cover"
                    style="width: 100px; height: 100px; margin-right: 8px; margin-top: 8px; border-radius: 4px; cursor: pointer"
                    :preview-src-list="getAdditionalImages(review).map(getImageUrl)"
                  />
                </div>
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
          
          <el-empty v-if="!reviewsLoading && storeReviews.length === 0" description="暂无评价" />
          
          <el-pagination
            v-if="storeReviewTotal > 0"
            layout="prev, pager, next"
            :total="storeReviewTotal"
            v-model:current-page="storeReviewPage"
            @current-change="loadStoreReviews"
            style="margin-top: 20px; justify-content: center"
          />
        </div>
      </div>
    </div>
    
    <!-- 购物车浮动按钮 -->
    <div class="cart-float" v-if="cartStore.totalByStore[store.id]">
      <el-badge :value="cartStore.totalByStore[store.id]" class="item">
        <el-button type="primary" size="large" @click="$router.push('/cart')">去结算</el-button>
      </el-badge>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeDetail, categories as fetchCategories, dishes as fetchDishes } from '@/api/store'
import { getStoreReviews, likeReview as apiLikeReview, dislikeReview as apiDislikeReview, getUserVote } from '@/api/product-review'
import { addCart as apiAddCart } from '@/api/cart'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { StarFilled, Picture, Clock, Select, Close } from '@element-plus/icons-vue'

const route = useRoute()
const userStore = useUserStore()
const store = ref(null)
const categories = ref([])
const dishesList = ref([])
const activeTab = ref(0)
const cartStore = useCartStore()

const router = useRouter()

// 评价相关
const storeReviews = ref([])
const reviewsLoading = ref(false)
const storeReviewPage = ref(1)
const storeReviewTotal = ref(0)
const reviewsLoaded = ref(false) // 标记是否已加载过评价

// 筛选和排序
const filterForm = ref({
  rating: null,
  withImage: false,
  sortBy: 'time' // time: 时间排序，like: 热度排序
})

const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return path.startsWith('/api') ? path : '/api' + path
}

// 获取评价图片，兼容不同的数据格式
const getReviewImages = (review) => {
  if (!review || !review.images) return []
  const images = review.images
  if (Array.isArray(images)) return images
  if (typeof images === 'string') {
    if (images.trim() === '') return []
    try {
      const parsed = JSON.parse(images)
      return Array.isArray(parsed) ? parsed : [images]
    } catch {
      return images.split(',').map(s => s.trim()).filter(Boolean)
    }
  }
  return []
}

// 获取追评图片，兼容不同的数据格式
const getAdditionalImages = (review) => {
  if (!review || !review.additionalImages) return []
  const images = review.additionalImages
  if (Array.isArray(images)) return images
  if (typeof images === 'string') {
    if (images.trim() === '') return []
    try {
      const parsed = JSON.parse(images)
      return Array.isArray(parsed) ? parsed : [images]
    } catch {
      return images.split(',').map(s => s.trim()).filter(Boolean)
    }
  }
  return []
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
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

const dishes = computed(() => {
  // activeTab 为 0 或 null 时返回所有菜品
  if (activeTab.value === 0 || activeTab.value === null || activeTab.value === undefined) {
    return dishesList.value
  }
  return dishesList.value.filter(d => d.categoryId === activeTab.value)
})

function goToDishDetail(id) {
  router.push(`/dish/${id}`)
}

async function load() {
  try {
    const id = route.params.id
    console.log('加载店铺 ID:', id)
    const [s, c, d] = await Promise.all([
      storeDetail(id),
      fetchCategories(id),
      fetchDishes(id)
    ])
    console.log('店铺数据:', s)
    console.log('分类数据:', c)
    console.log('菜品数据:', d)
    console.log('菜品数量:', d?.length)
    store.value = s || null
    categories.value = Array.isArray(c) ? c : []
    dishesList.value = (Array.isArray(d) ? d : []).map(x => ({ ...x, _qty: 0 }))
    console.log('处理后的菜品数据:', dishesList.value)
    console.log('当前分类:', activeTab.value)
    console.log('筛选后的菜品:', dishes.value)
  } catch (error) {
    console.error('加载店铺信息失败:', error)
    ElMessage.error('加载店铺信息失败，请稍后重试')
  }
}

// 加载店铺评价
async function loadStoreReviews() {
  reviewsLoading.value = true
  try {
    const id = route.params.id
    console.log('=== 开始加载店铺评价 ===')
    console.log('店铺 ID:', id)
    console.log('页码:', storeReviewPage.value)
    console.log('筛选条件:', filterForm.value)
    
    const params = {
      page: storeReviewPage.value,
      size: 10,
      ...filterForm.value
    }
    
    const res = await getStoreReviews(id, params)
    
    console.log('=== 店铺评价 API 响应 ===')
    console.log('原始响应 res:', res)
    console.log('res 的类型:', typeof res)
    console.log('res 是否为对象:', typeof res === 'object')
    console.log('res.data:', res?.data)
    console.log('res.total:', res?.total)
    console.log('res.code:', res?.code)
    console.log('完整 res 对象:', JSON.stringify(res, null, 2))
    
    // 处理不同的返回格式
    if (res && res.data) {
      storeReviews.value = Array.isArray(res.data) ? res.data : []
      storeReviewTotal.value = res.total || 0
      console.log('✓ 评价数据加载成功，数量:', storeReviews.value.length)
    } else if (Array.isArray(res)) {
      storeReviews.value = res
      storeReviewTotal.value = res.length
      console.log('✓ 评价数据加载成功（数组格式），数量:', storeReviews.value.length)
    } else {
      console.warn('⚠ 评价数据格式异常，设置为空数组')
      console.warn('res 的值:', res)
      storeReviews.value = []
      storeReviewTotal.value = 0
    }
    
    console.log('最终评价数据:', storeReviews.value)
    console.log('最终评价总数:', storeReviewTotal.value)
    console.log('=== 评价加载结束 ===')
    
    // 加载每个评价的用户投票状态
    if (userStore.isLogin) {
      for (const review of storeReviews.value) {
        try {
          const vote = await getUserVote(review.id)
          review.userVote = vote
        } catch (e) {
          console.error('加载投票状态失败:', e)
          review.userVote = 0
        }
      }
    }
    
    reviewsLoaded.value = true
  } catch (error) {
    console.error('❌ 加载店铺评价失败:', error)
    console.error('错误类型:', typeof error)
    console.error('错误是否是对象:', typeof error === 'object')
    console.error('错误详情:', error)
    if (error && typeof error === 'object') {
      console.error('错误码:', error.code)
      console.error('错误消息:', error.message)
      console.error('错误数据:', error.data)
    }
    // 未登录时也会加载评价，所以不应该报错
    // 只有真正发生错误时才显示提示
    if (error?.response?.status === 401 || error?.code === 401) {
      // 401 错误时显示空列表，不提示错误
      console.log('未登录，显示空评价列表')
      storeReviews.value = []
      storeReviewTotal.value = 0
    } else {
      // 其他错误才提示用户
      const errorMsg = error?.message || (typeof error === 'string' ? error : '未知错误')
      ElMessage.error('加载评价失败，请稍后重试：' + errorMsg)
      storeReviews.value = []
      storeReviewTotal.value = 0
    }
  } finally {
    reviewsLoading.value = false
  }
}

// 监听标签页切换
watch(activeTab, (newVal) => {
  console.log('标签页切换:', newVal)
  if (newVal === 'reviews') {
    console.log('切换到评价标签页，开始加载评价')
    loadStoreReviews()
  }
})

// 计算折扣率
function calculateDiscount(price, originPrice) {
  if (!originPrice || originPrice <= 0) return '';
  const discount = (price / originPrice) * 10;
  return discount.toFixed(1);
}

async function addCart(dish) {
  try {
    if (!userStore.isLogin) {
      ElMessage.warning('请先登录')
      return
    }
    if (dish._qty > 0) {
      console.log('添加购物车参数:', {
        storeId: store.value.id,
        dishId: dish.id,
        quantity: dish._qty
      })
      await apiAddCart(store.value.id, dish.id, dish._qty)
      await cartStore.fetchCart()
      ElMessage.success('已加入购物车')
      console.log('购物车数据:', cartStore.items)
      console.log('店铺购物车数量:', cartStore.totalByStore[store.value.id])
    }
  } catch (error) {
    console.error('添加购物车失败:', error)
    const errorMsg = error?.msg || error?.message || (typeof error === 'string' ? error : '添加购物车失败，请稍后重试')
    console.error('错误详情:', errorMsg, error)
    ElMessage.error(errorMsg)
  }
}

onMounted(async () => {
  try {
    await load()
    // 不再自动加载评价，改为点击标签页时才加载
    
    // 如果用户已登录且未加载用户信息，则刷新
    if (userStore.isLogin && !userStore.user) {
      try {
        await userStore.refreshProfile();
      } catch (error) {
        console.warn('加载用户信息失败:', error);
      }
    }
    // 加载购物车
    if (userStore.isLogin) {
      await cartStore.fetchCart()
    }
  } catch (error) {
    console.error('页面加载失败:', error)
  }
})
</script>

<style scoped>
.store-header { padding: 24px; color: #fff; }
.store-header h1 { margin-bottom: 8px; }
.store-header .meta { margin-top: 12px; opacity: 0.9; }
.page { padding: 20px 24px; background: #fff; min-height: calc(100vh - 200px); }
.category-tabs { margin-bottom: 20px; }
.dish-list { margin-top: 20px; }

/* 评价页面样式 */
.reviews-content {
  margin-top: 20px;
}
.reviews-header {
  margin-bottom: 20px;
}
.dish-item { 
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
  padding: 16px 0; 
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background-color 0.2s;
  gap: 16px;
}
.dish-item:hover {
  background-color: #f5f5f5;
}
.dish-image {
  flex-shrink: 0;
}

/* 暂无图片占位符 */
.no-image-placeholder {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 12px;
  gap: 4px;
}

.no-image-placeholder span {
  font-size: 11px;
}
.dish-info { 
  flex: 1;
  min-width: 0;
}
.dish-info h4 { margin-bottom: 4px; }
.dish-desc { color: #999; font-size: 13px; margin-bottom: 8px; }
.dish-price .price { color: #ff6b35; font-weight: 600; margin-right: 8px; }
.dish-price .origin { color: #999; text-decoration: line-through; font-size: 13px; }
.dish-price .discount-tag {
  background: linear-gradient(90deg, #ff6b35, #ff8f5e);
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  margin-left: 6px;
  font-weight: 500;
}
.dish-action {
  flex-shrink: 0;
}
.cart-float { position: fixed; bottom: 24px; right: 24px; z-index: 100; }
.section-title {
  font-size: 18px;
  margin-bottom: 16px;
  color: #333;
}
.review-count {
  color: #999;
  font-size: 14px;
  font-weight: normal;
}

/* 评价筛选样式 */
.reviews-filter {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 16px;
}

.filter-group {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.filter-group:last-child {
  margin-bottom: 0;
}

.filter-label {
  font-size: 14px;
  color: #666;
  margin-right: 12px;
  min-width: 50px;
}

.reviews-list {
  margin-top: 16px;
}
.review-item {
  padding: 16px 0;
  border-bottom: 1px solid #f5f5f5;
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
  gap: 12px;
}
.user-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.user-name {
  font-size: 14px;
  color: #333;
}
.review-time {
  font-size: 12px;
  color: #999;
}
.review-content p {
  margin: 8px 0;
  color: #666;
  line-height: 1.6;
}
.reply-section {
  margin-top: 12px;
  padding: 12px;
  background: #fafafa;
  border-radius: 4px;
  border-left: 2px solid #ff6b35;
}
.reply-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}
.reply-content {
  color: #666;
  line-height: 1.6;
}

/* 追评样式 */
.additional-review {
  margin-top: 12px;
  padding: 12px;
  background: #fff7f0;
  border-radius: 4px;
  border-left: 2px solid #ff9500;
}

.additional-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #ff9500;
  font-weight: 500;
  margin-bottom: 8px;
}

.additional-label .additional-time {
  color: #999;
  font-weight: normal;
  font-size: 11px;
  margin-left: auto;
}

.additional-content p {
  margin: 8px 0;
  color: #666;
  line-height: 1.6;
}

.additional-images {
  margin-top: 8px;
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