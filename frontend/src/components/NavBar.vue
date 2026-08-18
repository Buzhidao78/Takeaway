<template>
  <div class="nav-bar">
    <div class="nav-content">
      <div class="header-left">
        <router-link to="/" class="logo">
          <span class="logo-icon">🍱</span>
          <span class="logo-text">BZY 外卖</span>
        </router-link>
      </div>
      
      <el-menu mode="horizontal" :default-active="activeMenu" class="nav" router>
        <el-menu-item index="/">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/stores">
          <el-icon><Shop /></el-icon>
          <span>商家</span>
        </el-menu-item>
        <el-menu-item index="/cart" v-if="userStore.isLogin">
          <div class="cart-badge-wrapper">
            <el-badge :value="cartCount" :hidden="!cartCount" class="cart-badge">
              <template #default>
                <el-icon><ShoppingCart /></el-icon>
                <span>购物车</span>
              </template>
            </el-badge>
          </div>
        </el-menu-item>
      </el-menu>
      
      <div class="right-section">
        <!-- 搜索框 -->
        <div class="search-box">
          <el-input
            v-model="searchQuery"
            :placeholder="searchPlaceholder"
            clearable
            class="search-input"
            @keyup.enter="handleSearch"
            @focus="showSearchDropdown = true"
            @blur="hideSearchDropdown"
            @input="handleInputChange"
          >
            <template #prefix>
              <el-icon v-if="isListening" class="listening-icon" @click="startListening"><Microphone /></el-icon>
              <el-icon v-else class="search-icon" @click="handleSearch"><Search /></el-icon>
            </template>
          </el-input>
          <!-- 语音搜索按钮 -->
          <el-button v-if="!isListening" class="voice-search-btn" @click="startListening" title="语音搜索">
            <el-icon><Microphone /></el-icon>
          </el-button>
          <el-button v-else class="voice-search-btn listening" @click="stopListening" title="停止语音搜索" type="danger">
            <el-icon><Microphone /></el-icon>
          </el-button>
          <!-- 搜索下拉框 -->
          <transition name="el-zoom-in-top">
            <div v-if="showSearchDropdown" class="search-dropdown">
              <!-- 实时搜索建议 -->
              <div v-if="searchQuery.trim() && searchSuggestions.stores.length > 0" class="search-section">
                <div class="search-section-header">
                  <span class="section-title">🏪 商家</span>
                </div>
                <div class="suggestion-list">
                  <div
                    v-for="(item, index) in searchSuggestions.stores"
                    :key="index"
                    class="suggestion-item"
                    @click="searchByTag(item)"
                  >
                    <el-icon><Shop /></el-icon>
                    <span class="suggestion-text" v-html="highlightKeyword(item)"></span>
                  </div>
                </div>
              </div>
              
              <div v-if="searchQuery.trim() && searchSuggestions.dishes.length > 0" class="search-section">
                <div class="search-section-header">
                  <span class="section-title">🍱 菜品</span>
                </div>
                <div class="suggestion-list">
                  <div
                    v-for="(item, index) in searchSuggestions.dishes"
                    :key="index"
                    class="suggestion-item"
                    @click="searchByTag(item)"
                  >
                    <el-icon><Food /></el-icon>
                    <span class="suggestion-text" v-html="highlightKeyword(item)"></span>
                  </div>
                </div>
              </div>
              
              <!-- 搜索历史和热门搜索（仅在没有输入时显示） -->
              <template v-if="!searchQuery.trim()">
                <div v-if="searchHistory.length > 0" class="search-section">
                  <div class="search-section-header">
                    <span class="section-title">🔥 搜索历史</span>
                    <el-icon class="clear-icon" @click="clearSearchHistory"><Delete /></el-icon>
                  </div>
                  <div class="search-tags">
                    <el-tag
                      v-for="(item, index) in searchHistory"
                      :key="index"
                      size="small"
                      class="search-tag"
                      @click="searchByTag(item)"
                    >
                      {{ item }}
                    </el-tag>
                  </div>
                </div>
                
                <div v-if="hotSearches.length > 0" class="search-section">
                  <div class="search-section-header">
                    <span class="section-title">🔥 热门搜索</span>
                  </div>
                  <div class="search-tags">
                    <el-tag
                      v-for="(item, index) in hotSearches"
                      :key="index"
                      size="small"
                      class="search-tag hot-tag"
                      :type="index < 3 ? 'warning' : undefined"
                      @click="searchByTag(item)"
                    >
                      {{ item }}
                    </el-tag>
                  </div>
                </div>
              </template>
            </div>
          </transition>
        </div>
        
        <!-- 通知图标 -->
        <el-badge :value="notificationCount" :hidden="!notificationCount" class="notification-badge">
          <el-button 
            type="default" 
            class="notification-btn" 
            @click="handleNotification" 
            title="通知"
            :style="{
              width: '32px',
              height: '32px',
              minWidth: '32px',
              padding: '0',
              borderRadius: '50%',
              background: 'rgba(245, 245, 245, 0.8)',
              border: '1px solid #e0e0e0',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--primary)'
            }"
          >
            <el-icon><Bell /></el-icon>
          </el-button>
        </el-badge>
        
        <!-- 用户菜单 -->
        <el-dropdown v-if="userStore.isLogin" trigger="click" @command="handleUser">
          <span class="user-drop">
            <el-avatar :size="32" :style="{ background: 'var(--primary-gradient)' }">
              {{ (userStore.user?.nickname || 'U')[0] }}
            </el-avatar>
            <span class="user-name">{{ userStore.user?.nickname }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="user">
                <el-icon><User /></el-icon>
                个人中心
              </el-dropdown-item>
              <el-dropdown-item command="orders">
                <el-icon><List /></el-icon>
                我的订单
              </el-dropdown-item>
              <el-dropdown-item command="address">
                <el-icon><Location /></el-icon>
                收货地址
              </el-dropdown-item>
              <el-dropdown-item v-if="userStore.isRider" command="rider" divided>
                <el-icon><Van /></el-icon>
                骑手中心
              </el-dropdown-item>
              <el-dropdown-item v-if="userStore.role === 1 || userStore.role === 2" command="merchant" divided>
                <el-icon><Shop /></el-icon>
                商家中心
              </el-dropdown-item>
              <el-dropdown-item v-if="userStore.role === 2" command="admin">
                <el-icon><Monitor /></el-icon>
                管理后台
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        
        <template v-else>
          <el-button type="primary" round @click="$router.push('/login')">登录</el-button>
          <el-button round @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { ElMessage } from 'element-plus'
import { 
  HomeFilled, 
  Shop, 
  ShoppingCart, 
  Bell, 
  User, 
  List, 
  Location, 
  Monitor, 
  SwitchButton, 
  ArrowDown,
  Search,
  Microphone,
  Delete,
  Food,
  Van
} from '@element-plus/icons-vue'
import api from '@/api'
import { getUnreadCount } from '@/api/notification'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()

const searchQuery = ref('')
const searchPlaceholder = ref('搜索美食、商家')
const notificationCount = ref(0)
const showSearchDropdown = ref(false)
const searchHistory = ref([])
const hotSearches = ref([])
const searchSuggestions = ref({ stores: [], dishes: [] })
const isListening = ref(false)
let searchTimer = null
let recognition = null
let notificationTimer = null

const activeMenu = computed(() => {
  // 认证页面不显示菜单高亮
  if (['/login', '/register', '/merchant-register', '/rider-register'].includes(route.path)) {
    return ''
  }
  return route.path === '/' ? '/' : 
         route.path.startsWith('/stores') ? '/stores' : 
         route.path.startsWith('/cart') ? '/cart' : ''
})

const cartCount = computed(() => cartStore.totalCount)

// 加载搜索历史
function loadSearchHistory() {
  const history = localStorage.getItem('searchHistory')
  if (history) {
    searchHistory.value = JSON.parse(history)
  }
}

// 保存搜索历史
function saveSearchHistory(keyword) {
  searchHistory.value = searchHistory.value.filter(item => item !== keyword)
  searchHistory.value.unshift(keyword)
  if (searchHistory.value.length > 10) {
    searchHistory.value = searchHistory.value.slice(0, 10)
  }
  localStorage.setItem('searchHistory', JSON.stringify(searchHistory.value))
}

// 清空搜索历史
function clearSearchHistory() {
  searchHistory.value = []
  localStorage.removeItem('searchHistory')
  ElMessage.success('已清空搜索历史')
}

// 加载热门搜索
function loadHotSearches() {
  hotSearches.value = [
    '麻辣烫',
    '汉堡',
    '披萨',
    '奶茶',
    '炸鸡',
    '寿司',
    '烧烤',
    '酸菜鱼'
  ]
}

// 隐藏搜索下拉框
function hideSearchDropdown() {
  setTimeout(() => {
    showSearchDropdown.value = false
  }, 200)
}

// 处理输入变化，获取搜索建议
function handleInputChange() {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  
  if (!searchQuery.value.trim()) {
    searchSuggestions.value = { stores: [], dishes: [] }
    return
  }
  
  searchTimer = setTimeout(async () => {
    try {
      const res = await api.get('/store/suggest', {
        params: { keyword: searchQuery.value, limit: 5 }
      })
      console.log('搜索建议原始响应:', res)
      console.log('searchSuggestions赋值前:', searchSuggestions.value)
      searchSuggestions.value = res || { stores: [], dishes: [] }
      console.log('searchSuggestions赋值后:', searchSuggestions.value)
    } catch (error) {
      console.error('获取搜索建议失败:', error)
    }
  }, 300)
}

// 高亮关键词
function highlightKeyword(text) {
  if (!searchQuery.value.trim()) {
    return text
  }
  const regex = new RegExp(`(${searchQuery.value})`, 'gi')
  return text.replace(regex, '<span class="highlight">$1</span>')
}

// 通过标签搜索
function searchByTag(keyword) {
  searchQuery.value = keyword
  handleSearch()
}

// 初始化语音识别
function initSpeechRecognition() {
  if ('webkitSpeechRecognition' in window || 'SpeechRecognition' in window) {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
    recognition = new SpeechRecognition()
    recognition.continuous = false
    recognition.interimResults = false
    recognition.lang = 'zh-CN'
    
    recognition.onresult = (event) => {
      const transcript = event.results[0][0].transcript
      searchQuery.value = transcript
      ElMessage.success(`识别结果：${transcript}`)
      handleSearch()
    }
    
    recognition.onerror = (event) => {
      console.error('语音识别错误:', event.error)
      ElMessage.error('语音识别失败，请重试')
      isListening.value = false
    }
    
    recognition.onend = () => {
      isListening.value = false
    }
  } else {
    console.warn('浏览器不支持语音识别')
  }
}

// 开始语音识别
function startListening() {
  if (!recognition) {
    ElMessage.warning('您的浏览器不支持语音识别')
    return
  }
  
  try {
    recognition.start()
    isListening.value = true
    ElMessage.info('请开始说话...')
  } catch (error) {
    console.error('启动语音识别失败:', error)
    ElMessage.error('启动语音识别失败')
  }
}

// 停止语音识别
function stopListening() {
  if (recognition) {
    recognition.stop()
    isListening.value = false
  }
}

// 搜索
function handleSearch() {
  if (!searchQuery.value.trim()) {
    ElMessage.warning('请输入搜索内容')
    return
  }
  
  saveSearchHistory(searchQuery.value.trim())
  
  router.push({
    path: '/search',
    query: { keyword: searchQuery.value }
  })
  
  showSearchDropdown.value = false
}

// 通知
function handleNotification() {
  router.push('/notifications')
}

// 加载未读通知数量
async function loadUnreadCount() {
  if (!userStore.isLogin) {
    notificationCount.value = 0
    return
  }
  try {
    const res = await getUnreadCount()
    notificationCount.value = res.count || 0
  } catch (error) {
    console.error('获取未读通知数量失败:', error)
  }
}

// 用户菜单
function handleUser(cmd) {
  if (cmd === 'logout') {
    // 清理通知相关状态
    notificationCount.value = 0
    if (notificationTimer) {
      clearInterval(notificationTimer)
      notificationTimer = null
    }
    userStore.logout()
    cartStore.clear()
    router.push('/')
    ElMessage.success('已退出登录')
  } else if (cmd === 'user') {
    router.push('/user')
  } else if (cmd === 'orders') {
    router.push('/user/orders')
  } else if (cmd === 'address') {
    router.push('/user/address')
  } else if (cmd === 'rider') {
    if (!userStore.isRider) {
      ElMessage.warning('您不是骑手用户')
      return
    }
    router.push('/rider/home')
  } else if (cmd === 'merchant') {
    if (userStore.role !== 1 && userStore.role !== 2) {
      ElMessage.warning('您没有商家权限')
      return
    }
    router.push('/merchant')
  } else if (cmd === 'admin') {
    if (userStore.role !== 2) {
      ElMessage.warning('您没有管理员权限')
      return
    }
    router.push('/admin')
  }
}

onMounted(async () => {
  // 检查骑手状态
  if (userStore.isLogin) {
    await userStore.checkRiderStatus()
    loadUnreadCount()
    // 每30秒刷新一次未读通知数量
    notificationTimer = setInterval(loadUnreadCount, 30000)
  }
  
  loadSearchHistory()
  loadHotSearches()
  initSpeechRecognition()
})

onBeforeUnmount(() => {
  showSearchDropdown.value = false
  if (notificationTimer) {
    clearInterval(notificationTimer)
  }
})
</script>

<style scoped>
.nav-bar {
  width: 100%;
  height: 60px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.nav-content {
  width: 100%;
  height: 100%;
  padding: 0 24px;
  display: flex;
  align-items: center;
  gap: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 22px;
  font-weight: 700;
  color: var(--primary);
  text-decoration: none;
  transition: transform 0.3s;
}

.logo:hover {
  transform: scale(1.05);
}

.logo-icon {
  font-size: 28px;
}

.logo-text {
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav {
  flex: 1;
  border: none;
  background: transparent;
  min-width: 0;
}

.nav :deep(.el-menu-item) {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  color: var(--text-secondary);
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
  overflow: visible;
  position: relative;
}

.nav :deep(.el-menu-item:hover) {
  background: transparent;
  color: var(--primary);
}

.nav :deep(.el-menu-item.is-active) {
  color: var(--primary);
  border-bottom-color: var(--primary);
}

.right-section {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.search-box {
  position: relative;
}

.search-input {
  width: 280px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 20px;
  background: rgba(245, 245, 245, 0.8);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.search-input :deep(.el-input__inner) {
  padding-left: 35px;
}

.search-input :deep(.el-input__prefix) {
  left: 12px;
  cursor: pointer;
}

.search-input :deep(.el-input__prefix .el-icon) {
  font-size: 18px;
  color: var(--text-secondary);
}

.search-input :deep(.el-input__prefix .listening-icon) {
  color: var(--primary);
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.voice-search-btn {
  position: absolute;
  right: -40px;
  top: 50%;
  transform: translateY(-50%);
  width: 32px;
  height: 32px;
  padding: 0;
  border-radius: 50%;
  background: rgba(245, 245, 245, 0.8);
  border: 1px solid #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.voice-search-btn:hover {
  background: rgba(255, 107, 53, 0.1);
  border-color: var(--primary);
  color: var(--primary);
}

.voice-search-btn.listening {
  background: var(--primary);
  border-color: var(--primary);
  color: white;
  animation: pulse 1.5s infinite;
}

.voice-search-btn .el-icon {
  font-size: 16px;
}

.search-input :deep(.el-input__wrapper:hover) {
  background: rgba(245, 245, 245, 1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.search-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(255, 107, 53, 0.2);
}

/* 搜索下拉框 */
.search-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: -40px;
  margin-top: 8px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  padding: 16px;
  max-height: 400px;
  overflow-y: auto;
  z-index: 1001;
}

.search-section {
  margin-bottom: 16px;
}

.search-section:last-child {
  margin-bottom: 0;
}

.search-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.clear-icon {
  font-size: 16px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: color 0.3s;
}

.clear-icon:hover {
  color: var(--primary);
}

.suggestion-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.3s;
}

.suggestion-item:hover {
  background: rgba(255, 107, 53, 0.1);
}

.suggestion-item .el-icon {
  font-size: 16px;
  color: var(--text-secondary);
}

.suggestion-text {
  font-size: 14px;
  color: var(--text-primary);
}

.suggestion-text :deep(.highlight) {
  color: var(--primary);
  font-weight: 600;
}

.search-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.search-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.search-tag:hover {
  transform: translateY(-2px);
}

.hot-tag {
  background: rgba(255, 107, 53, 0.1);
  border-color: rgba(255, 107, 53, 0.2);
  color: var(--primary);
}

.notification-badge {
  cursor: pointer;
  flex-shrink: 0;
}

.notification-badge :deep(.el-badge__content) {
  border: none;
}

.notification-btn {
  width: 32px !important;
  height: 32px !important;
  min-width: 32px !important;
  padding: 0 !important;
  border-radius: 50% !important;
  background: rgba(245, 245, 245, 0.8) !important;
  border: 1px solid #e0e0e0 !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  transition: all 0.3s;
  color: var(--primary) !important;
}

.notification-btn:hover {
  background: rgba(255, 107, 53, 0.1) !important;
  border-color: var(--primary) !important;
  color: var(--primary) !important;
}

.notification-btn .el-icon {
  font-size: 16px;
}

.user-drop {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 20px;
  transition: background 0.3s;
  flex-shrink: 0;
}

.user-drop:hover {
  background: rgba(255, 107, 53, 0.1);
}

.user-name {
  font-size: 14px;
  color: var(--text-primary);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cart-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  position: relative;
}

.cart-badge-wrapper {
  position: relative;
  display: inline-block;
}
</style>
