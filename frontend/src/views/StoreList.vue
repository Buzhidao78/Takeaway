<template>
  <div class="page">
    <div class="header">
      <h1>商家列表</h1>
      <div class="filters">
        <el-input v-model="keyword" placeholder="搜索商家" @keyup.enter="load" clearable style="width:300px" />
        <el-select v-model="categoryId" placeholder="全部分类" clearable @change="load" style="width:150px;margin-left:10px">
          <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
        </el-select>
      </div>
    </div>
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="8" v-for="s in stores" :key="s.id" v-if="stores && stores.length > 0">
        <el-card class="store-card" shadow="hover" @click="$router.push(`/store/${s.id}`)">
          <div class="store-cover" :style="getStoreCoverStyle(s)">
            <span v-if="!getCoverUrl(s)" class="store-name-initial">{{ s.name.charAt(0) }}</span>
          </div>
          <h3>{{ s.name }}</h3>
          <p class="desc">{{ s.description || '暂无描述' }}</p>
          <div class="meta">
            <span><el-icon><StarFilled /></el-icon> {{ s.rating != null && s.rating > 0 ? s.rating : '暂无评分' }}</span>
            <span>月售 {{ s.salesCount || 0 }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" v-else>
        <el-card class="store-card" shadow="never">
          <div class="store-cover" :style="{ background: '#f5f5f5' }"></div>
          <h3>暂无商家</h3>
          <p class="desc">暂无商家数据</p>
        </el-card>
      </el-col>
    </el-row>
    <el-pagination v-if="total>pageSize" layout="prev,pager,next" :total="total" v-model:current-page="page" @current-change="load" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { storeList } from '@/api/store'
import { categoryList, getStoresByCategory } from '@/api/category'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { StarFilled } from '@element-plus/icons-vue'

const route = useRoute()
const stores = ref([])
const categories = ref([])
const page = ref(1)
const pageSize = 10
const total = ref(0)
const keyword = ref('')
const categoryId = ref(route.query.categoryId ? Number(route.query.categoryId) : null)
const loading = ref(false)
const error = ref(null)
const userStore = useUserStore()
const cartStore = useCartStore()

const getCoverUrl = (store) => {
  if (!store) return ''
  const imagePath = store.banner || store.logo
  if (!imagePath) return ''
  if (imagePath.startsWith('http')) return imagePath
  return imagePath.startsWith('/api') ? imagePath : '/api' + imagePath
}

const getStoreCoverStyle = (store) => {
  if (!store) return {}
  const imageUrl = getCoverUrl(store)
  
  if (imageUrl) {
    return {
      backgroundImage: `url(${imageUrl})`
    }
  }
  
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

async function load() {
  loading.value = true
  error.value = null
  try {
    let res
    if (categoryId.value) {
      res = await getStoresByCategory(categoryId.value, page.value, pageSize)
    } else {
      res = await storeList(page.value, pageSize, keyword.value)
    }
    
    if (res && res.records && Array.isArray(res.records)) {
      stores.value = res.records
      total.value = res.total || 0
    } else if (Array.isArray(res)) {
      stores.value = res
      total.value = 0
    } else {
      stores.value = []
      total.value = 0
    }
  } catch (e) {
    error.value = e.message || '加载失败'
    console.error('加载商店列表失败:', e)
    ElMessage.error(error.value)
  } finally {
    loading.value = false
  }
}

stores.value = [];

onMounted(async () => {
  const [cats] = await Promise.all([
    categoryList()
  ])
  categories.value = cats || []
  loadData();
})

async function loadData() {
  await load();
  
  if (userStore.isLogin && !userStore.user) {
    try {
      await userStore.refreshProfile();
    } catch (err) {
      console.warn('加载用户信息失败:', err);
    }
  }
  if (userStore.isLogin) {
    try {
      await cartStore.fetchCart();
    } catch (err) {
      console.warn('加载购物车失败:', err);
    }
  }
}
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.filters {
  display: flex;
  align-items: center;
}

.store-cover { 
  height: 120px; 
  border-radius: 8px; 
  margin-bottom: 12px;
  position: relative;
  overflow: hidden;
  background-size: cover;
  background-position: center;
}

.store-name-initial {
  font-size: 60px;
  font-weight: bold;
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
}

.store-card h3 { margin-bottom: 8px; }
.desc { color: #999; font-size: 13px; margin-bottom: 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.meta { display: flex; gap: 16px; color: #666; font-size: 13px; }
</style>
