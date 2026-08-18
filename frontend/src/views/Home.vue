<template>
  <div class="home-page">
    <!-- 动态 Banner -->
    <div class="banner-section">
      <el-carousel 
        height="320px" 
        class="home-banner" 
        v-if="banners && banners.length > 0"
        :interval="4000"
        arrow="hover"
        indicator-position="outside"
      >
        <el-carousel-item v-for="(banner, index) in banners" :key="index">
          <div class="banner-item" :class="{ 'has-image': banner.image }" :style="banner.image ? { backgroundImage: `url(${formatImageUrl(banner.image)})` } : {}">
            <div class="banner-overlay">
              <div class="banner-content">
                <h2 class="banner-title">{{ banner.title || 'BZY 外卖' }}</h2>
                <p class="banner-subtitle">{{ banner.subtitle || '品质美食，快速送达' }}</p>
                <el-button 
                  v-if="banner.link" 
                  type="warning" 
                  size="large" 
                  round 
                  class="banner-btn"
                  @click="navigateToBanner(banner)"
                >
                  立即订购
                </el-button>
                <el-button 
                  v-else 
                  type="warning" 
                  size="large" 
                  round 
                  class="banner-btn"
                  @click="navigateToStores"
                >
                  立即订购
                </el-button>
              </div>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
      <div v-else class="banner-placeholder">
        <div class="placeholder-content">
          <div class="placeholder-icon">🍱</div>
          <h2>BZY 外卖</h2>
          <p>品质生活，从一餐开始</p>
        </div>
      </div>
    </div>

    <!-- 分类入口 -->
    <div class="category-section">
      <div class="category-grid">
        <div 
          v-for="(cat, index) in categories" 
          :key="index" 
          class="category-item"
          @click="navigateToCategory(cat.id)"
        >
          <el-icon class="category-icon" :size="32">
            <component :is="cat.icon" />
          </el-icon>
          <span class="category-name">{{ cat.name }}</span>
        </div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="content-section">
      <!-- 左侧：热门商家 -->
      <div class="main-content">
        <div class="section-header">
          <h2 class="section-title">
            <span class="title-icon">🏪</span>
            热门商家
          </h2>
          <el-button text type="primary" @click="$router.push('/stores')" class="more-btn">
            查看更多 <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>

        <div v-if="error" class="error-message">
          <el-alert
            title="加载失败"
            :description="error"
            type="error"
            show-icon
            :closable="false"
          />
        </div>

        <el-row :gutter="20" v-if="stores && stores.length > 0">
          <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="s in stores" :key="s.id">
            <el-card class="store-card" shadow="hover" @click="$router.push(`/store/${s.id}`)">
              <!-- 轮播图容器 -->
              <div class="store-cover-wrapper">
                <el-carousel 
                  v-if="hasCarousel(s)" 
                  height="160px" 
                  :interval="3000"
                  arrow="hover"
                  indicator-position="none"
                  class="store-carousel"
                >
                  <el-carousel-item v-for="(img, idx) in getCarouselImages(s)" :key="idx">
                    <div class="carousel-item" :style="{ backgroundImage: `url(${formatImageUrl(img)})` }"></div>
                  </el-carousel-item>
                </el-carousel>
                <!-- 没有轮播图时显示封面 -->
                <div v-else class="store-cover" :style="getStoreCoverStyle(s)">
                  <span v-if="!getCoverUrl(s)" class="store-name-initial">{{ s.name.charAt(0) }}</span>
                </div>
              </div>
              <div class="store-info">
                <h3>{{ s.name }}</h3>
                <p class="desc">{{ s.description || '暂无描述' }}</p>
                <div class="store-meta">
                  <div class="rating">
                    <el-icon class="star-icon"><StarFilled /></el-icon>
                    <span>{{ s.rating != null && s.rating > 0 ? s.rating : '暂无评分' }}</span>
                  </div>
                  <span class="sales">月售 {{ s.salesCount || 0 }}</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        
        <!-- 空状态 -->
        <div v-else class="empty-state">
          <el-card class="store-card" shadow="never">
            <div class="store-cover" style="background: #f5f5f5; display: flex; align-items: center; justify-content: center;">
              <span style="color: #999; font-size: 48px;">🏪</span>
            </div>
            <div class="store-info">
              <h3>暂无商家</h3>
              <p class="desc">暂无商家数据</p>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 右侧：排行榜 -->
      <div class="sidebar">
        <div class="ranking-section">
          <div class="section-header">
            <h2 class="section-title">
              <span class="title-icon">🏆</span>
              销量榜
            </h2>
          </div>
          <div class="ranking-list">
            <div 
              v-for="(item, index) in rankingList" 
              :key="index" 
              class="ranking-item"
              @click="$router.push(`/store/${item.id}`)"
            >
              <div class="ranking-number" :class="'top-' + (index + 1)">{{ index + 1 }}</div>
              <div class="ranking-info">
                <h4 class="ranking-name">{{ item.name }}</h4>
                <div class="ranking-meta">
                  <span class="ranking-rating">
                    <el-icon><StarFilled /></el-icon> {{ item.rating != null && item.rating > 0 ? item.rating : '暂无评分' }}
                  </span>
                  <span class="ranking-sales">月售 {{ item.salesCount || 0 }}</span>
                </div>
              </div>
            </div>
            <div v-if="rankingList.length === 0" class="empty-ranking">
              <div class="empty-icon">📊</div>
              <p>暂无排行数据</p>
            </div>
          </div>
        </div>

        <div class="ranking-section" style="margin-top: 24px;">
          <div class="section-header">
            <h2 class="section-title">
              <span class="title-icon">⭐</span>
              好评榜
            </h2>
          </div>
          <div class="ranking-list">
            <div 
              v-for="(item, index) in ratingList" 
              :key="index" 
              class="ranking-item"
              @click="$router.push(`/store/${item.id}`)"
            >
              <div class="ranking-number" :class="'top-' + (index + 1)">{{ index + 1 }}</div>
              <div class="ranking-info">
                <h4 class="ranking-name">{{ item.name }}</h4>
                <div class="ranking-meta">
                  <span class="ranking-rating">
                    <el-icon><StarFilled /></el-icon> {{ item.rating != null && item.rating > 0 ? item.rating : '暂无评分' }}
                  </span>
                  <span class="ranking-sales">月售 {{ item.salesCount || 0 }}</span>
                </div>
              </div>
            </div>
            <div v-if="ratingList.length === 0" class="empty-ranking">
              <div class="empty-icon">📊</div>
              <p>暂无排行数据</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { storeList } from '@/api/store'
import { categoryList } from '@/api/category'
import { bannerList } from '@/api/banner'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { StarFilled, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()
const stores = ref([])
const banners = ref([])
const loading = ref(false)
const error = ref(null)
const cartStore = useCartStore()
const userStore = useUserStore()

// 获取商家封面图片 URL
const getCoverUrl = (store) => {
  if (!store) return ''
  // 只使用 banner 字段作为封面
  const imagePath = store.banner
  if (!imagePath) return ''
  if (imagePath.startsWith('http')) return imagePath
  return imagePath.startsWith('/api') ? imagePath : '/api' + imagePath
}

// 格式化图片 URL（用于轮播图）
const formatImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return url.startsWith('/api') ? url : '/api' + url
}

// 判断商家是否有轮播图
const hasCarousel = (store) => {
  if (!store) return false
  const carouselStr = store.carouselImages
  if (!carouselStr) return false
  if (typeof carouselStr === 'string') {
    return carouselStr.split(',').filter(url => url.trim()).length > 0
  }
  return Array.isArray(carouselStr) && carouselStr.length > 0
}

// 获取商家轮播图数组
const getCarouselImages = (store) => {
  if (!store) return []
  const carouselStr = store.carouselImages
  if (!carouselStr) return []
  if (typeof carouselStr === 'string') {
    return carouselStr.split(',').filter(url => url.trim())
  }
  return Array.isArray(carouselStr) ? carouselStr : []
}

// 获取商家封面样式（支持文字封面）
const getStoreCoverStyle = (store) => {
  if (!store) return {}
  const imageUrl = getCoverUrl(store)
  
  // 如果有图片，使用图片
  if (imageUrl) {
    return {
      backgroundImage: `url(${imageUrl})`
    }
  }
  
  // 没有图片时，使用渐变色背景 + 店铺名称首字
  const colors = [
    'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
    'linear-gradient(135deg, #30cfd0 0%, #330867 100%)',
    'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
    'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)'
  ]
  const colorIndex = store.name.length % colors.length
  const gradient = colors[colorIndex]
  
  return {
    background: gradient,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '80px',
    fontWeight: 'bold',
    color: 'rgba(255, 255, 255, 0.9)',
    textShadow: '2px 2px 4px rgba(0,0,0,0.2)'
  }
}

// 分类数据
const categories = ref([])

// 排行榜数据（从 stores 中计算得出）
const rankingList = computed(() => {
  if (!stores.value || stores.value.length === 0) return []
  // 按销量排序取前 5
  return [...stores.value]
    .sort((a, b) => (b.salesCount || 0) - (a.salesCount || 0))
    .slice(0, 5)
})

const ratingList = computed(() => {
  if (!stores.value || stores.value.length === 0) return []
  return [...stores.value]
    .sort((a, b) => {
      const ratingA = Number(a.rating) || 0
      const ratingB = Number(b.rating) || 0
      return ratingB - ratingA
    })
    .slice(0, 5)
})

console.log('Home 组件开始加载')

// 立即设置初始状态，让页面快速渲染
stores.value = []
banners.value = []

onMounted(async () => {
  console.log('Home 组件 onMounted 执行')
  
  // 在下一个 tick 或微任务中异步加载数据，避免阻塞页面渲染
  loadBannerData();
})

// 分类导航
function navigateToCategory(categoryId) {
  router.push(`/stores?categoryId=${categoryId}`)
}

// Banner 跳转
function navigateToBanner(banner) {
  if (banner.link) {
    router.push(banner.link)
  }
}

// 默认跳转到商家列表
function navigateToStores() {
  router.push('/stores')
}

async function loadBannerData() {
  loading.value = true
  error.value = null
  try {
    const [s, b, c] = await Promise.all([
      storeList(1, 8),
      bannerList(),
      categoryList()
    ])
    
    let storeData = []
    if (s && s.records && Array.isArray(s.records)) {
      storeData = s.records
    } else if (Array.isArray(s)) {
      storeData = s
    }
    
    stores.value = storeData
    banners.value = (b && b.length) ? b : []
    categories.value = c || []
    if (userStore.isLogin && !userStore.user) {
      try {
        // 刷新用户信息确保数据是最新的
        await userStore.refreshProfile();
        console.log('用户信息刷新成功');
      } catch (err) {
        console.warn('用户信息刷新失败:', err);
      }
    }
    if (userStore.isLogin) {
      try {
        await cartStore.fetchCart()
      } catch (cartErr) {
        console.warn('购物车加载失败:', cartErr)
      }
    }
  } catch (e) {
    error.value = e.message || '加载失败'
    console.error('加载失败:', e)
    console.error('错误详情:', e.response || e)
    ElMessage.error(error.value || '加载失败')
    // 即使出错也要确保页面内容显示，不能让页面空白
    stores.value = []
    banners.value = [] // 使用空数组，这样会显示占位符
  } finally {
    loading.value = false
    console.log('Home 组件加载完成')
  }
}
</script>

<style scoped>
/* 页面容器 */
.home-page {
  min-height: 100%;
  background: var(--bg-primary);
  padding: 20px;
}

/* Banner 区域 */
.banner-section {
  margin-bottom: 24px;
}

.home-banner {
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-md);
}

.banner-item {
  height: 100%;
  background-size: cover;
  background-position: center;
  position: relative;
}

/* 有图片时的样式 */
.banner-item.has-image {
  background-color: #f5f5f5;
}

/* 没有图片时的橙色渐变背景 */
.banner-item:not(.has-image) {
  background: linear-gradient(135deg, #ff6b35 0%, #ff8f65 100%);
}

.banner-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 有图片时的遮罩 */
.banner-item.has-image .banner-overlay {
  background: linear-gradient(135deg, rgba(0, 0, 0, 0.4) 0%, rgba(0, 0, 0, 0.3) 100%);
}

/* 没有图片时的遮罩（透明） */
.banner-item:not(.has-image) .banner-overlay {
  background: transparent;
}

.banner-content {
  text-align: center;
  color: #fff;
  padding: 20px;
}

.banner-title {
  font-size: 36px;
  font-weight: var(--font-weight-bold);
  margin-bottom: 12px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.banner-subtitle {
  font-size: 18px;
  margin-bottom: 24px;
  opacity: 0.95;
}

.banner-btn {
  background: #fff;
  color: var(--primary);
  font-weight: var(--font-weight-semibold);
  padding: 12px 32px;
  font-size: 16px;
  border: none;
  transition: all 0.3s;
}

.banner-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(255, 255, 255, 0.3);
}

.banner-placeholder {
  height: 320px;
  background: linear-gradient(135deg, #ff6b35 0%, #ff8f65 100%);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-md);
}

.placeholder-content {
  text-align: center;
  color: #fff;
}

.placeholder-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.placeholder-content h2 {
  font-size: 32px;
  margin-bottom: 8px;
}

.placeholder-content p {
  font-size: 16px;
  opacity: 0.9;
}

/* 分类区域 */
.category-section {
  margin-bottom: 32px;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
  gap: 16px;
  padding: 20px 24px;
  background: #fff;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 12px 8px;
  border-radius: var(--radius-md);
  transition: all 0.3s;
}

.category-item:hover {
  background: rgba(255, 107, 53, 0.08);
  transform: translateY(-2px);
}

.category-icon {
  font-size: 28px;
  line-height: 1;
  color: var(--text-primary);
}

.category-name {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: var(--font-weight-medium);
  white-space: nowrap;
}

/* 主内容区 */
.content-section {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
  margin-bottom: 32px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 22px;
  font-weight: var(--font-weight-bold);
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
}

.title-icon {
  font-size: 24px;
}

.more-btn {
  font-size: 14px;
  color: var(--primary);
}

.more-btn:hover {
  color: var(--primary-dark);
}

/* 商家卡片 */
.store-card {
  margin-bottom: 20px;
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
}

.store-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.store-cover {
  height: 160px;
  background-size: cover;
  background-position: center;
  border-radius: var(--radius-lg) var(--radius-lg) 0 0;
  position: relative;
  overflow: hidden;
}

.store-cover-wrapper {
  position: relative;
}

.store-carousel {
  border-radius: var(--radius-lg) var(--radius-lg) 0 0;
  overflow: hidden;
}

/* 箭头样式 - 覆盖在图片上 */
.store-carousel :deep(.el-carousel__arrow) {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  border: none;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 16px;
  transition: all 0.3s;
}

.store-carousel :deep(.el-carousel__arrow:hover) {
  background: rgba(0, 0, 0, 0.7);
}

.store-carousel :deep(.el-carousel__arrow--left) {
  left: 10px;
}

.store-carousel :deep(.el-carousel__arrow--right) {
  right: 10px;
}

.carousel-item {
  height: 100%;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.store-name-initial {
  font-size: 80px;
  font-weight: bold;
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
}

.store-info {
  padding: 16px;
}

.store-info h3 {
  font-size: 18px;
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.desc {
  color: var(--text-secondary);
  font-size: 13px;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.store-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}

.rating {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--accent-yellow);
  font-weight: var(--font-weight-semibold);
}

.star-icon {
  font-size: 16px;
}

.sales {
  color: var(--text-secondary);
}

/* 侧边栏 */
.sidebar {
  display: flex;
  flex-direction: column;
}

.ranking-section {
  background: #fff;
  padding: 20px;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ranking-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.3s;
}

.ranking-item:hover {
  background: rgba(255, 107, 53, 0.08);
}

.ranking-number {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
  background: #f0f0f0;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: var(--font-weight-bold);
  font-size: 16px;
  flex-shrink: 0;
}

.ranking-number.top-1 {
  background: linear-gradient(135deg, #ffd700 0%, #ffb800 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(255, 215, 0, 0.4);
}

.ranking-number.top-2 {
  background: linear-gradient(135deg, #c0c0c0 0%, #a0a0a0 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(192, 192, 192, 0.4);
}

.ranking-number.top-3 {
  background: linear-gradient(135deg, #cd7f32 0%, #b87333 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(205, 127, 50, 0.4);
}

.ranking-info {
  flex: 1;
  min-width: 0;
}

.ranking-name {
  font-size: 15px;
  font-weight: var(--font-weight-medium);
  color: var(--text-primary);
  margin: 0 0 6px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ranking-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--text-secondary);
}

.ranking-sales,
.ranking-rating {
  display: flex;
  align-items: center;
  gap: 4px;
}

.empty-ranking {
  text-align: center;
  padding: 40px 20px;
  color: var(--text-muted);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

/* 错误提示 */
.error-message {
  margin-bottom: 20px;
}

/* 空状态 */
.empty-state {
  margin-bottom: 20px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .content-section {
    grid-template-columns: 1fr;
  }
  
  .sidebar {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 24px;
  }
}

@media (max-width: 768px) {
  .category-grid {
    grid-template-columns: repeat(4, 1fr);
  }
  
  .banner-title {
    font-size: 24px;
  }
  
  .banner-subtitle {
    font-size: 14px;
  }
  
  .sidebar {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .category-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
