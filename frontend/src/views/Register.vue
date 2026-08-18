<template>
  <div class="register-page">
    <!-- 主体内容区域 -->
    <div class="register-body">
      <!-- 左侧品牌展示 -->
      <div class="register-left">
      <div class="left-content">
        <div class="brand-logo">
          <span class="logo-icon">🍱</span>
          <h1>BZY 外卖</h1>
        </div>
        <h2 class="slogan">开启美食之旅</h2>
        <p class="description">注册账号，享受便捷外卖服务</p>
        
        <!-- 注册福利 -->
        <div class="benefits">
          <div class="benefit-item">
            <el-icon class="benefit-icon"><StarFilled /></el-icon>
            <div class="benefit-info">
              <h4>新人礼包</h4>
              <p>注册即享优惠券</p>
            </div>
          </div>
          <div class="benefit-item">
            <el-icon class="benefit-icon"><Shop /></el-icon>
            <div class="benefit-info">
              <h4>万店美食</h4>
              <p>百万商家任你选</p>
            </div>
          </div>
          <div class="benefit-item">
            <el-icon class="benefit-icon"><Van /></el-icon>
            <div class="benefit-info">
              <h4>快速配送</h4>
              <p>30 分钟必达</p>
            </div>
          </div>
        </div>
        
        <!-- 装饰元素 -->
        <div class="decoration-food decoration-1">🎉</div>
        <div class="decoration-food decoration-2">🎁</div>
        <div class="decoration-food decoration-3">✨</div>
      </div>
    </div>
    
    <!-- 右侧注册表单 -->
    <div class="register-right">
      <div class="register-form-container">
        <div class="form-header">
          <h2>创建账号</h2>
          <p>填写以下信息，立即开启美食之旅</p>
        </div>
        
        <el-form :model="form" label-width="0" size="large" class="register-form">
          <el-form-item>
            <el-input 
              v-model="form.phone" 
              placeholder="请输入手机号" 
              maxlength="11"
              class="custom-input"
            >
              <template #prefix>
                <el-icon><Iphone /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item>
            <div class="code-row">
              <el-input 
                v-model="form.code" 
                placeholder="请输入验证码" 
                maxlength="6"
                class="custom-input code-input"
              >
                <template #prefix>
                  <el-icon><Key /></el-icon>
                </template>
              </el-input>
              <el-button 
                :disabled="countdown > 0" 
                @click="sendCode"
                class="code-btn"
                :loading="sendingCode"
              >
                {{ countdown > 0 ? countdown + 's' : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>
          
          <el-form-item>
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="设置密码（至少 6 位）" 
              show-password
              class="custom-input"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item>
            <el-input 
              v-model="form.nickname" 
              placeholder="设置昵称（可选）" 
              class="custom-input"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item>
            <el-checkbox v-model="agreeTerms">
              我已阅读并同意 <el-link type="primary" :underline="false">《用户协议》</el-link> 和 <el-link type="primary" :underline="false">《隐私政策》</el-link>
            </el-checkbox>
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              class="register-btn" 
              :loading="loading" 
              @click="submit"
            >
              {{ loading ? '注册中...' : '立即注册' }}
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="form-footer">
          <p class="tip">已有账号？<router-link to="/login">立即登录</router-link></p>
        </div>
      </div>
    </div>
    </div>
    
    <!-- 注册成功动画 -->
    <div v-if="showSuccess" class="success-overlay">
      <div class="success-animation">
        <el-icon class="success-icon"><CircleCheckFilled /></el-icon>
        <h3>注册成功</h3>
        <p>欢迎加入 BZY 外卖大家庭</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register, sendCode as apiSendCode } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { 
  Iphone, 
  Lock, 
  Key, 
  User, 
  CircleCheckFilled, 
  StarFilled, 
  Shop, 
  Van
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
const agreeTerms = ref(false)
const showSuccess = ref(false)
const form = reactive({ phone: '', code: '', password: '', nickname: '' })

async function sendCode() {
  if (!form.phone || !/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning('请输入正确手机号')
    return
  }
  
  sendingCode.value = true
  try {
    await apiSendCode(form.phone, 'register')
    ElMessage.success('验证码已发送，开发模式下请查看控制台日志')
    countdown.value = 60
    const t = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(t)
    }, 1000)
  } catch (e) {
    ElMessage.error(e?.message || '发送失败')
  } finally {
    sendingCode.value = false
  }
}

async function submit() {
  if (!form.phone || !form.code || !form.password) {
    ElMessage.warning('请填写手机号、验证码和密码')
    return
  }
  
  if (!agreeTerms.value) {
    ElMessage.warning('请先同意用户协议和隐私政策')
    return
  }
  
  if (form.password.length < 6) {
    ElMessage.warning('密码至少 6 位')
    return
  }
  
  loading.value = true
  try {
    const res = await register(form)
    console.log('注册响应:', res)
    
    const user = res?.data || res
    const token = user?.token || res?.token
    
    if (!token) {
      throw new Error('未获取到 token')
    }
    
    userStore.setUser(user, token)
    
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
      ElMessage.success('注册成功')
      router.push('/')
    }, 2000)
    
  } catch (error) {
    console.error('注册失败:', error)
    const errorMsg = error?.msg || error?.message || '注册失败，请稍后重试'
    ElMessage.error(errorMsg)
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 60px); /* 减去导航栏高度 */
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

/* 主体内容区域 */
.register-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 左侧区域 */
.register-left {
  flex: 1;
  background: linear-gradient(135deg, #ff6b35 0%, #ff8f65 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

/* 右侧区域 */
.register-right {
  width: 500px;
  background: white;
  display: flex;
  flex-direction: column;
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

.benefits {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-top: 40px;
  max-width: 300px;
  margin-left: auto;
  margin-right: auto;
}

.benefit-item {
  display: flex;
  align-items: center;
  gap: 16px;
  background: rgba(255, 255, 255, 0.1);
  padding: 16px;
  border-radius: var(--radius-md);
  backdrop-filter: blur(10px);
}

.benefit-icon {
  font-size: 32px;
}

.benefit-info h4 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}

.benefit-info p {
  font-size: 12px;
  opacity: 0.9;
}

.decoration-food {
  position: absolute;
  font-size: 80px;
  opacity: 0.15;
  animation: float 6s ease-in-out infinite;
}

.decoration-1 { top: 10%; left: 10%; animation-delay: 0s; }
.decoration-2 { top: 20%; right: 15%; animation-delay: 1s; }
.decoration-3 { bottom: 15%; left: 20%; animation-delay: 2s; }

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(10deg); }
}

/* 右侧区域 */
.register-right {
  width: 500px;
  background: white;
  display: flex;
  flex-direction: column;
}

.register-form-container {
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
  padding: 80px 40px;
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

.register-form {
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

.register-btn {
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

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(255, 107, 53, 0.3);
  background: linear-gradient(135deg, #ff8f65 0%, #ffa080 100%);
}

.register-btn:active {
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
  .register-left {
    display: none;
  }
  
  .register-right {
    width: 100%;
    padding: 20px;
  }
  
  .register-form-container {
    max-width: 100%;
  }
}
</style>
