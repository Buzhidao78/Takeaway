<template>
  <div class="layout">
    <el-header class="header">
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
              <el-icon><ShoppingCart /></el-icon>
              <span>购物车</span>
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
        <el-badge :value="notificationStore.unreadCount" :hidden="!notificationStore.unreadCount" class="notification-badge">
          <el-button class="notification-btn" @click="handleNotification" title="通知">
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
    </el-header>
    
    <main class="main">
      <div v-if="userStore.isLogin && !userStore.user" class="loading-user-info">
        <p>正在加载用户信息，请稍候...</p>
      </div>
      <div class="content-wrapper">
        <slot></slot>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, onBeforeUnmount, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { useNotificationStore } from '@/stores/notification'
import { ElMessage } from 'element-plus'
import { 
  ShoppingCart, 
  ArrowDown, 
  HomeFilled, 
  Shop, 
  Search, 
  Bell,
  User,
  List,
  Location,
  Monitor,
  SwitchButton,
  Delete,
  Food,
  Microphone,
  Van
} from '@element-plus/icons-vue'
import api from '@/api'
import { getUnreadCount } from '@/api/notification'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const notificationStore = useNotificationStore()
const searchQuery = ref('')
const searchPlaceholder = ref('搜索美食、商家')
const showSearchDropdown = ref(false)
const searchHistory = ref([])
const hotSearches = ref([])
const searchSuggestions = ref({ stores: [], dishes: [] })
const isListening = ref(false)
let searchTimer = null
let recognition = null
let notificationWs = null

const activeMenu = computed(() => {
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
  // 移除重复的
  searchHistory.value = searchHistory.value.filter(item => item !== keyword)
  // 添加到开头
  searchHistory.value.unshift(keyword)
  // 只保留最近 10 条
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

// 加载热门搜索（模拟数据，后续可从后端获取）
function loadHotSearches() {
  // 模拟数据，实际应该从后端 API 获取
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

// 隐藏搜索下拉框（延迟隐藏，避免点击标签时失效）
function hideSearchDropdown() {
  setTimeout(() => {
    showSearchDropdown.value = false
  }, 200)
}

// 处理输入变化，获取搜索建议
function handleInputChange() {
  // 清除之前的定时器
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  
  // 如果输入为空，清空建议
  if (!searchQuery.value.trim()) {
    searchSuggestions.value = { stores: [], dishes: [] }
    return
  }
  
  // 防抖 300ms
  searchTimer = setTimeout(async () => {
    try {
      const res = await api.get('/store/suggest', {
        params: { keyword: searchQuery.value, limit: 5 }
      })
      searchSuggestions.value = res || { stores: [], dishes: [] }
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

onMounted(async () => {
  if (userStore.isLogin && !userStore.user) {
    try {
      await userStore.refreshProfile()
    } catch (err) {
      console.warn('Layout 中用户信息刷新失败:', err)
    }
  }
  
  // 检查骑手状态
  if (userStore.isLogin) {
    await userStore.checkRiderStatus()
    loadUnreadCount()
    // 每30秒刷新一次未读通知数量
    notificationTimer = setInterval(loadUnreadCount, 30000)
    // 连接通知 WebSocket
    connectNotificationWebSocket()
  }
  
  // 加载搜索历史和热门搜索
  loadSearchHistory()
  loadHotSearches()
  
  // 初始化语音识别
  initSpeechRecognition()
})

// 监听登录状态变化
let prevLoginStatus = userStore.isLogin
watch(() => userStore.isLogin, (newVal) => {
  if (newVal && !prevLoginStatus) {
    // 用户刚登录，加载未读通知数量并连接 WebSocket
    loadUnreadCount()
    connectNotificationWebSocket()
    // 重置定时器
    if (notificationTimer) {
      clearInterval(notificationTimer)
    }
    notificationTimer = setInterval(loadUnreadCount, 30000)
  } else if (!newVal && prevLoginStatus) {
    // 用户刚退出，清理通知相关状态
    notificationStore.resetUnreadCount()
    if (notificationTimer) {
      clearInterval(notificationTimer)
      notificationTimer = null
    }
    if (notificationWs) {
      notificationWs.close()
      notificationWs = null
    }
  }
  prevLoginStatus = newVal
})

// 加载未读通知数量
async function loadUnreadCount() {
  if (!userStore.isLogin) {
    notificationStore.resetUnreadCount()
    return
  }
  try {
    const res = await getUnreadCount()
    notificationStore.setUnreadCount(res.count || 0)
  } catch (error) {
    console.error('获取未读通知数量失败:', error)
  }
}

let notificationTimer = null

// 连接通知 WebSocket
function connectNotificationWebSocket() {
  if (!userStore.isLogin || !userStore.token) return
  
  const proto = location.protocol === 'https:' ? 'wss' : 'ws'
  const wsBaseUrl = `${proto}://${location.host}/ws`
  const wsUrl = `${wsBaseUrl}/notification?token=${userStore.token}`
  notificationWs = new WebSocket(wsUrl)
  
  notificationWs.onopen = () => {
    console.log('通知 WebSocket 已连接')
    // 发送心跳
    setInterval(() => {
      if (notificationWs && notificationWs.readyState === WebSocket.OPEN) {
        notificationWs.send(JSON.stringify({ type: 'heartbeat' }))
      }
    }, 30000)
  }
  
  notificationWs.onmessage = (event) => {
    console.log('收到 WebSocket 消息:', event.data)
    const data = JSON.parse(event.data)
    console.log('解析后的消息:', data)
    if (data.type === 'notification:new') {
      console.log('当前未读数量:', notificationStore.unreadCount)
      // 直接增加角标数量（实时响应）
      notificationStore.incrementUnreadCount()
      console.log('增加后未读数量:', notificationStore.unreadCount)
      // 显示提示
      ElMessage.info(data.data.title)
    }
  }
  
  notificationWs.onerror = (error) => {
    console.error('通知 WebSocket 错误:', error)
  }
  
  notificationWs.onclose = () => {
    console.log('通知 WebSocket 已断开')
    // 5秒后重连
    setTimeout(() => {
      if (userStore.isLogin) {
        connectNotificationWebSocket()
      }
    }, 5000)
  }
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

// 组件卸载时清理
onBeforeUnmount(() => {
  showSearchDropdown.value = false
  if (notificationTimer) {
    clearInterval(notificationTimer)
  }
  if (notificationWs) {
    notificationWs.close()
  }
})

function handleSearch() {
  if (!searchQuery.value.trim()) {
    ElMessage.warning('请输入搜索内容')
    return
  }
  
  // 保存搜索历史
  saveSearchHistory(searchQuery.value.trim())
  
  // 跳转到搜索页面或商家列表
  router.push({
    path: '/search',
    query: { keyword: searchQuery.value }
  })
  
  // 隐藏下拉框
  showSearchDropdown.value = false
}

function handleNotification() {
  router.push('/notifications')
}

function handleUser(cmd) {
  if (cmd === 'logout') {
    // 清理通知相关状态
    notificationStore.resetUnreadCount()
    if (notificationTimer) {
      clearInterval(notificationTimer)
      notificationTimer = null
    }
    if (notificationWs) {
      notificationWs.close()
      notificationWs = null
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
</script>

<style scoped>
.layout {
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1;
  overflow: visible !important;
}

.header {
  display: flex;
  align-items: center;
  padding: 0 24px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 1000;
  transition: all 0.3s;
  width: 100%;
  box-sizing: border-box;
  overflow: visible !important;
}

.header-left {
  display: flex;
  align-items: center;
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
}

.nav :deep(.el-menu-item) {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  color: var(--text-secondary);
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
  overflow: visible !important;
  position: relative !important;
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
  margin-left: auto;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-input {
  width: 240px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 20px;
  background: rgba(245, 245, 245, 0.8);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  cursor: pointer;
}

.search-input :deep(.el-input__wrapper:hover) {
  background: rgba(245, 245, 245, 1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.search-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(255, 107, 53, 0.2);
}

/* 搜索图标按钮样式 */
.search-input :deep(.el-input__prefix) {
  cursor: pointer;
}

.search-input :deep(.el-input__prefix .search-icon) {
  font-size: 16px;
  color: var(--primary);
  transition: all 0.3s;
}

.search-input :deep(.el-input__prefix .search-icon:hover) {
  transform: scale(1.2);
}

/* 语音搜索按钮 */
.voice-search-btn {
  padding: 8px;
  border-radius: 50%;
  border: none;
  background: rgba(245, 245, 245, 0.8);
  color: var(--primary);
  cursor: pointer;
  transition: all 0.3s;
  min-width: auto;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.voice-search-btn:hover {
  background: rgba(255, 107, 53, 0.1);
  transform: scale(1.1);
}

.voice-search-btn.listening {
  background: rgba(255, 107, 53, 0.2);
  animation: pulse 1.5s ease-in-out infinite;
}

.voice-search-btn .el-icon {
  font-size: 18px;
}

/* 语音识别动画 */
.listening-icon {
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(1.2);
  }
}

/* 搜索下拉框 */
.search-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 8px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.15);
  z-index: 1001;
  padding: 16px;
  min-width: 400px;
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
  color: #999;
  cursor: pointer;
  transition: color 0.3s;
}

.clear-icon:hover {
  color: var(--primary);
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
  box-shadow: 0 2px 8px rgba(255, 107, 53, 0.3);
}

.hot-tag {
  font-weight: 500;
}

/* 搜索建议列表 */
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
  transition: all 0.3s;
  font-size: 14px;
  color: var(--text-primary);
}

.suggestion-item:hover {
  background: rgba(255, 107, 53, 0.08);
}

.suggestion-item .el-icon {
  font-size: 16px;
  color: var(--primary);
}

.suggestion-text {
  flex: 1;
}

.suggestion-text :deep(.highlight) {
  color: var(--primary);
  font-weight: 600;
}

.notification-badge {
  cursor: pointer;
}

.notification-btn {
  padding: 8px;
  border-radius: 50%;
  border: none;
  background: rgba(245, 245, 245, 0.8);
  color: var(--primary);
  cursor: pointer;
  transition: all 0.3s;
  min-width: auto;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notification-btn:hover {
  background: rgba(255, 107, 53, 0.1);
  transform: scale(1.1);
}

.notification-btn .el-icon {
  font-size: 18px;
}

.user-drop {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 20px;
  transition: background 0.3s;
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
}

.cart-badge-wrapper {
  position: relative;
  display: inline-block;
}

/* 角标样式 - 使用 margin 调整位置 */
.cart-badge :deep(.el-badge__content) {
  margin-top: 8px !important;
  margin-right: -8px !important;
}

.nav :deep(.el-menu-item) {
  overflow: visible !important;
  position: relative !important;
}

.main {
  flex: 1;
  min-height: 0;
  background: var(--bg-gradient);
  display: flex;
  flex-direction: column;
  /* 方案 2：只在右侧预留滚动条空间 */
  overflow-y: auto;
  scrollbar-gutter: stable;
}

.content-wrapper {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

.loading-user-info {
  padding: 20px;
  text-align: center;
  color: var(--text-muted);
  font-size: 16px;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 968px) {
  .header {
    padding: 0 16px;
  }
  
  .search-input {
    width: 180px;
  }
  
  .user-name {
    display: none;
  }
  
  .nav :deep(.el-menu-item span) {
    display: none;
  }
}

@media (max-width: 768px) {
  .search-box {
    display: none;
  }
  
  .logo-text {
    font-size: 18px;
  }
  
  .nav :deep(.el-menu-item) {
    padding: 0 12px;
  }
}
</style>
