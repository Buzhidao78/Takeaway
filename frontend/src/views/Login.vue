<template>
  <div class="login-page">
    <!-- 主体内容区域 -->
    <div class="login-body">
      <!-- 左侧图片区域 -->
      <div class="login-left">
      <div class="left-content">
        <div class="brand-logo">
          <span class="logo-icon">🍱</span>
          <h1>BZY 外卖</h1>
        </div>
        <h2 class="slogan">品质美食，快速送达</h2>
        <p class="description">百万商家，千城万店，30 分钟必达</p>
        
        <!-- 特性列表 -->
        <div class="features">
          <div class="feature-item">
            <el-icon class="feature-icon"><CircleCheckFilled /></el-icon>
            <span>品质商家</span>
          </div>
          <div class="feature-item">
            <el-icon class="feature-icon"><Clock /></el-icon>
            <span>快速配送</span>
          </div>
          <div class="feature-item">
            <el-icon class="feature-icon"><StarFilled /></el-icon>
            <span>好评如潮</span>
          </div>
        </div>
        
        <!-- 装饰元素 -->
        <div class="decoration-food decoration-1">🍔</div>
        <div class="decoration-food decoration-2">🍕</div>
        <div class="decoration-food decoration-3">🍜</div>
        <div class="decoration-food decoration-4">🍰</div>
      </div>
    </div>
    
    <!-- 右侧登录表单 -->
    <div class="login-right">
      <div class="login-form-container">
        <div class="form-header">
          <h2>欢迎回来</h2>
          <p>登录即享品质美食服务</p>
        </div>
        
        <el-form :model="form" label-width="0" size="large" class="login-form">
          <el-form-item>
            <el-input 
              v-model="form.phone" 
              placeholder="请输入手机号" 
              :prefix-icon="Iphone" 
              maxlength="11"
              class="custom-input"
            />
          </el-form-item>
          
          <el-form-item>
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="请输入密码" 
              :prefix-icon="Lock" 
              show-password
              class="custom-input"
              @keyup.enter="submit"
            />
          </el-form-item>
          
          <!-- 验证码输入框（失败 5 次后显示） -->
          <el-form-item v-if="needCaptcha">
            <div class="code-row">
              <el-input 
                v-model="form.captcha" 
                placeholder="请输入验证码" 
                :prefix-icon="Key"
                maxlength="6"
                class="custom-input code-input"
                @keyup.enter="submit"
              />
              <el-button 
                @click="sendCaptcha" 
                :disabled="captchaSending"
                class="code-btn"
                :loading="captchaSending"
              >
                {{ captchaSending ? captchaCountdown + 's' : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>
          
          <div class="form-options">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <router-link to="/forgot-password" class="forgot-link">忘记密码？</router-link>
          </div>
          
          <el-form-item>
            <el-button 
              type="primary" 
              class="login-btn" 
              :loading="loading" 
              @click="submit"
            >
              {{ loading ? '登录中...' : '立即登录' }}
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="form-footer">
          <p class="tip">还没有账号？<router-link to="/register">立即注册</router-link></p>
          <div class="divider">
            <span>其他登录方式</span>
          </div>
          <div class="other-links">
            <router-link to="/merchant-register" class="other-link">商家入驻</router-link>
            <span class="divider-dot">·</span>
            <router-link to="/rider-register" class="other-link">骑手注册</router-link>
          </div>
        </div>
      </div>
    </div>
    </div>
    
    <!-- 登录成功动画 -->
    <div v-if="showSuccess" class="success-overlay">
      <div class="success-animation">
        <el-icon class="success-icon"><CircleCheckFilled /></el-icon>
        <h3>登录成功</h3>
        <p>正在跳转...</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import { login, getCaptcha } from '@/api/auth'
import { ElMessage } from 'element-plus'
import { Iphone, Lock, CircleCheckFilled, Clock, StarFilled, Key } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const rememberMe = ref(false)
const showSuccess = ref(false)
const needCaptcha = ref(false)
const captchaSending = ref(false)
const captchaCountdown = ref(0)
const form = reactive({ 
  phone: '', 
  password: '',
  captcha: ''
})

// 检查登录失败次数
async function checkFailureCount() {
  if (!form.phone || !/^1[3-9]\d{9}$/.test(form.phone)) {
    needCaptcha.value = false
    return
  }
  
  try {
    const { getLoginFailureCount } = await import('@/api/auth')
    const res = await getLoginFailureCount(form.phone)
    // API 拦截器已经返回了 responseData.data，所以直接使用 res
    const count = typeof res === 'number' ? res : (res?.data || 0)
    needCaptcha.value = count >= 5
    
    console.log('登录失败次数:', count, '是否需要验证码:', needCaptcha.value)
  } catch (error) {
    console.error('获取失败次数失败:', error)
    needCaptcha.value = false
  }
}

// 发送验证码
async function sendCaptcha() {
  if (!form.phone || !/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  
  captchaSending.value = true
  captchaCountdown.value = 60
  
  try {
    await getCaptcha(form.phone)
    ElMessage.success('验证码已发送，请注意查收')
    
    // 倒计时
    const timer = setInterval(() => {
      captchaCountdown.value--
      if (captchaCountdown.value <= 0) {
        clearInterval(timer)
        captchaSending.value = false
      }
    }, 1000)
  } catch (error) {
    console.error('发送验证码失败:', error)
    ElMessage.error(error?.msg || '发送验证码失败')
    captchaSending.value = false
    captchaCountdown.value = 0
  }
}

// 组件挂载时检查是否已登录
onMounted(() => {
  // 添加背景动画
  document.body.style.overflow = 'hidden'
})

// 监听手机号变化，检查是否需要验证码
watch(() => form.phone, () => {
  checkFailureCount()
})

// 组件卸载时恢复 body 滚动
onBeforeUnmount(() => {
  document.body.style.overflow = ''
})

async function submit() {
  if (!form.phone || !form.password) {
    ElMessage.warning('请输入手机号和密码')
    return
  }
  
  // 手机号验证
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  
  // 如果需要验证码，检查是否输入
  if (needCaptcha.value && !form.captcha) {
    ElMessage.warning('请输入验证码')
    return
  }
  
  loading.value = true
  try {
    const res = await login(form.phone, form.password, form.captcha)
    console.log('登录响应:', res)
    
    const user = res?.data || res
    const token = user?.token || res?.token
    
    if (!token) {
      throw new Error('未获取到 token')
    }
    
    // 动态导入 useUserStore
    const { useUserStore } = await import('@/stores/user')
    const userStore = useUserStore()
    userStore.setUser(user, token)
    
    // 检查骑手状态
    await userStore.checkRiderStatus()
    
    // 加载未读通知数量
    try {
      const { useNotificationStore } = await import('@/stores/notification')
      const notificationStore = useNotificationStore()
      const { getUnreadCount } = await import('@/api/notification')
      const res = await getUnreadCount()
      notificationStore.setUnreadCount(res.count || 0)
    } catch (error) {
      console.error('加载未读通知数量失败:', error)
    }
    
    // 显示成功动画
    showSuccess.value = true
    
    // 延迟跳转
    setTimeout(() => {
      ElMessage.success('登录成功')
      router.push('/')
    }, 1500)
    
  } catch (error) {
    console.error('登录失败:', error)
    const errorMsg = error?.msg || error?.message || '登录失败，请稍后重试'
    ElMessage.error(errorMsg)
    
    // 登录失败后都检查一次失败次数，以便及时显示验证码输入框
    // 延迟一点检查，确保后端已经记录了失败次数
    setTimeout(async () => {
      await checkFailureCount()
      
      // 如果需要验证码，给个提示
      if (needCaptcha.value) {
        ElMessage.info('失败次数过多，请输入验证码')
      }
    }, 500)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 60px); /* 减去导航栏高度 */
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

/* 主体内容区域 */
.login-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 左侧区域 */
.login-left {
  flex: 1;
  background: linear-gradient(135deg, #ff6b35 0%, #ff8f65 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

/* 右侧区域 */
.login-right {
  width: 500px;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  padding: 80px 40px;
  background: white;
  overflow-y: auto;
}

.left-content {
  color: white;
  text-align: center;
  z-index: 10;
  padding: 40px;
}

.brand-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 40px;
}

.logo-icon {
  font-size: 48px;
}

.brand-logo h1 {
  font-size: 42px;
  font-weight: 700;
  margin: 0;
}

.slogan {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 16px;
}

.description {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 40px;
}

.features {
  display: flex;
  gap: 30px;
  justify-content: center;
  margin-top: 40px;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.feature-icon {
  font-size: 32px;
}

.feature-item span {
  font-size: 14px;
  opacity: 0.9;
}

/* 装饰元素 */
.decoration-food {
  position: absolute;
  font-size: 80px;
  opacity: 0.15;
  animation: float 6s ease-in-out infinite;
}

.decoration-1 { top: 10%; left: 10%; animation-delay: 0s; }
.decoration-2 { top: 20%; right: 15%; animation-delay: 1s; }
.decoration-3 { bottom: 15%; left: 20%; animation-delay: 2s; }
.decoration-4 { bottom: 10%; right: 10%; animation-delay: 3s; }

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(10deg); }
}

.login-form-container {
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
}

.form-header {
  margin-bottom: 40px;
}

.form-header h2 {
  font-size: 32px;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.form-header p {
  font-size: 14px;
  color: var(--text-muted);
}

.login-form {
  margin-bottom: 20px;
}

.custom-input {
  width: 100%;
}

.custom-input :deep(.el-input__wrapper) {
  padding: 12px 16px;
  border-radius: var(--radius-md);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.custom-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(255, 107, 53, 0.2);
}

/* 表单选项 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.forgot-link {
  color: #ff6b35;
  font-size: 14px;
  text-decoration: none;
  font-weight: 600;
}

.forgot-link:hover {
  text-decoration: underline;
}

/* 验证码输入框样式 */
.code-row {
  display: flex;
  gap: 12px;
  width: 100%;
}

.code-input {
  flex: 1;
}

.code-btn {
  padding: 12px 20px;
  font-weight: 600;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, #ff6b35 0%, #ff8f65 100%);
  border: none;
  white-space: nowrap;
  transition: all 0.3s;
  color: #fff;
}

.code-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(255, 107, 53, 0.3);
}

.code-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login-btn {
  width: 100%;
  padding: 14px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, #ff6b35 0%, #ff8f65 100%);
  border: none;
  transition: all 0.3s;
  color: #fff;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(255, 107, 53, 0.3);
  background: linear-gradient(135deg, #ff8f65 0%, #ffa080 100%);
}

.login-btn:active {
  transform: translateY(0);
}

.form-footer {
  text-align: center;
}

.tip {
  font-size: 14px;
  color: var(--text-secondary);
}

.tip a {
  color: var(--primary);
  font-weight: 600;
}

.divider {
  position: relative;
  margin: 24px 0;
  text-align: center;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: #e0e0e0;
}

.divider span {
  position: relative;
  background: white;
  padding: 0 16px;
  color: var(--text-muted);
  font-size: 12px;
}

.other-links {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
}

.other-link {
  color: var(--text-secondary);
  font-size: 14px;
  transition: color 0.3s;
}

.other-link:hover {
  color: var(--primary);
}

.divider-dot {
  color: var(--text-muted);
}

/* 成功动画 */
.success-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.95);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fadeIn 0.3s ease;
}

.success-animation {
  text-align: center;
  animation: slideUp 0.4s ease;
}

.success-icon {
  font-size: 80px;
  color: var(--accent-green);
  margin-bottom: 16px;
}

.success-animation h3 {
  font-size: 24px;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.success-animation p {
  font-size: 14px;
  color: var(--text-muted);
}

/* 响应式设计 */
@media (max-width: 968px) {
  .login-left {
    display: none;
  }
  
  .login-right {
    width: 100%;
    padding: 20px;
  }
  
  .login-form-container {
    max-width: 100%;
  }
}
</style>
