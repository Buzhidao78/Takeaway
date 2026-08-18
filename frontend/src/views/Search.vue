<template>
  <div class="page search-page">
    <div class="search-header">
      <div class="search-input-wrapper">
        <el-input
          v-model="keyword"
          placeholder="搜索美食、商家"
          prefix-icon="Search"
          clearable
          size="large"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button :icon="Search" @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
      </div>
    </div>

    <div class="search-content" v-loading="loading">
      <!-- 搜索结果 -->
      <div v-if="!loading && searchResult.total > 0" class="search-result">
        <div class="result-summary">
          <span class="result-count">找到 {{ searchResult.total }} 个结果</span>
          <div class="result-tabs">
            <el-radio-group v-model="activeTab" size="small" @change="handleTabChange">
              <el-radio-button value="all">全部</el-radio-button>
              <el-radio-button value="store">商家</el-radio-button>
              <el-radio-button value="dish">菜品</el-radio-button>
            </el-radio-group>
          </div>
        </div>

        <!-- 商家列表 -->
        <div v-if="activeTab === 'all' || activeTab === 'store'" class="store-list">
          <div v-for="store in searchResult.stores" :key="store.id" class="store-card" @click="goToStore(store.id)">
            <div class="store-image">
              <img :src="store.image || '/default-store.png'" :alt="store.name" />
            </div>
            <div class="store-info">
              <div class="store-name">{{ store.name }}</div>
              <div class="store-meta">
                <span class="store-score">评分 {{ store.score }}</span>
                <span class="store-sales">月售 {{ store.sales }}</span>
                <span class="store-distance">距离 {{ store.distance }}</span>
              </div>
              <div class="store-tags">
                <el-tag size="small" v-if="store.isNew">新店</el-tag>
                <el-tag size="small" type="success" v-if="store.promotion">{{ store.promotion }}</el-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- 菜品列表 -->
        <div v-if="activeTab === 'all' || activeTab === 'dish'" class="dish-list">
          <div v-for="dish in searchResult.dishes" :key="dish.id" class="dish-card" @click="goToDish(dish.id)">
            <div class="dish-image">
              <img v-if="dish.image" :src="dish.image?.startsWith('http') ? dish.image : (baseURL + dish.image)" :alt="dish.name" />
              <div v-else class="no-image-placeholder">
                <el-icon :size="32"><Picture /></el-icon>
                <p>暂无图片</p>
              </div>
            </div>
            <div class="dish-info">
              <div class="dish-name">{{ dish.name }}</div>
              <div class="dish-meta">
                <span class="dish-price">¥{{ dish.price }}</span>
                <span class="dish-sales">月售 {{ dish.salesCount || 0 }}</span>
              </div>
              <div class="dish-store">{{ dish.storeName }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 无结果 -->
      <div v-if="!loading && searchResult.total === 0" class="no-result">
        <el-empty :description="`未找到与'${keyword}'相关的结果`">
          <el-button type="primary" @click="resetSearch">返回首页</el-button>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search, Picture } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const route = useRoute()
const router = useRouter()
const keyword = ref('')
const loading = ref(false)
const activeTab = ref('all')
const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

const searchResult = reactive({
  total: 0,
  stores: [],
  dishes: []
})

// 监听路由参数变化
watch(() => route.query.keyword, (newKeyword) => {
  if (newKeyword) {
    keyword.value = newKeyword
    performSearch()
  }
}, { immediate: true })

onMounted(() => {
  if (route.query.keyword) {
    keyword.value = route.query.keyword
    performSearch()
  }
})

async function performSearch() {
  if (!keyword.value.trim()) {
    console.log('关键词为空，不搜索')
    return
  }

  console.log('开始搜索，关键词:', keyword.value)
  loading.value = true
  try {
    // 同时搜索商家和菜品
    console.log('发送商家搜索请求...')
    const storesRes = await api.get('/store/list', {
      params: { page: 1, size: 20, keyword: keyword.value }
    })
    console.log('商家搜索结果:', storesRes)
    
    console.log('发送菜品搜索请求...')
    const dishesRes = await api.get('/store/dishes/search', {
      params: { page: 1, size: 20, keyword: keyword.value }
    })
    console.log('菜品搜索结果:', dishesRes)

    searchResult.stores = storesRes.records || []
    searchResult.dishes = dishesRes.records || []
    searchResult.total = searchResult.stores.length + searchResult.dishes.length
    console.log('搜索结果总数:', searchResult.total, '商家数:', searchResult.stores.length, '菜品数:', searchResult.dishes.length)
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  if (!keyword.value.trim()) {
    ElMessage.warning('请输入搜索内容')
    return
  }

  router.push({
    path: '/search',
    query: { keyword: keyword.value }
  })
  performSearch()
}

function handleTabChange() {
  // 切换标签时重新加载数据（如果需要分页）
}

function goToStore(storeId) {
  router.push(`/store/${storeId}`)
}

function goToDish(dishId) {
  if (!dishId) {
    ElMessage.warning('菜品 ID 不存在')
    return
  }
  router.push(`/dish/${dishId}`)
}

function resetSearch() {
  router.push('/')
}
</script>

<style scoped>
.search-page {
  min-height: 100vh;
  background: var(--bg);
}

.search-header {
  background: #fff;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 16px;
}

.search-input-wrapper {
  max-width: 600px;
  margin: 0 auto;
}

.search-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.result-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.result-count {
  font-size: 16px;
  color: var(--text-secondary);
  font-weight: 500;
}

.store-list,
.dish-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.store-card,
.dish-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.store-card:hover,
.dish-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.store-image,
.dish-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.store-image img,
.dish-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #909399;
}

.no-image-placeholder p {
  margin-top: 8px;
  font-size: 12px;
}

.store-info,
.dish-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.store-name,
.dish-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.store-meta,
.dish-meta {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: var(--text-secondary);
}

.store-score,
.store-sales,
.store-distance,
.dish-price,
.dish-sales {
  display: flex;
  align-items: center;
}

.dish-price {
  color: var(--primary);
  font-weight: 600;
  font-size: 16px;
}

.store-tags {
  display: flex;
  gap: 8px;
}

.no-result {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

@media (max-width: 768px) {
  .store-list,
  .dish-list {
    grid-template-columns: 1fr;
  }

  .result-summary {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}
</style>
