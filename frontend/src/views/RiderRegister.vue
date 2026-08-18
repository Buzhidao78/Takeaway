<template>
  <div class="rider-register-page">
    <!-- 主体内容区域 -->
    <div class="rider-register-body">
      <!-- 左侧图片区域 -->
      <div class="register-left">
      <div class="left-content">
        <div class="brand-logo">
          <span class="logo-icon">🛵</span>
          <h1>BZY 骑手</h1>
        </div>
        <h2 class="slogan">时间自由，多劳多得</h2>
        <p class="description">加入 BZY 骑手团队，开启您的配送赚钱之旅</p>
        
        <!-- 骑手优势 -->
        <div class="benefits">
          <div class="benefit-item">
            <el-icon class="benefit-icon"><Clock /></el-icon>
            <div class="benefit-info">
              <h4>时间自由</h4>
              <span>灵活接单，工作生活两不误</span>
            </div>
          </div>
          <div class="benefit-item">
            <el-icon class="benefit-icon"><Money /></el-icon>
            <div class="benefit-info">
              <h4>收入可观</h4>
              <span>多劳多得，月入过万不是梦</span>
            </div>
          </div>
          <div class="benefit-item">
            <el-icon class="benefit-icon"><TrendCharts /></el-icon>
            <div class="benefit-info">
              <h4>晋升空间</h4>
              <span>优秀骑手可晋升站长</span>
            </div>
          </div>
          <div class="benefit-item">
            <el-icon class="benefit-icon"><CircleCheckFilled /></el-icon>
            <div class="benefit-info">
              <h4>保障完善</h4>
              <span>意外保险，配送有保障</span>
            </div>
          </div>
        </div>
        
        <!-- 装饰元素 -->
        <div class="decoration-icon decoration-1">🎯</div>
        <div class="decoration-icon decoration-2">⏱️</div>
        <div class="decoration-icon decoration-3">💪</div>
        <div class="decoration-icon decoration-4">🏅</div>
      </div>
    </div>
    
    <!-- 右侧注册表单 -->
    <div class="register-right">
      <div class="form-container">
        <div class="form-header">
          <h2>骑手注册申请</h2>
          <p>成为骑手，开启配送赚钱之旅</p>
        </div>
        
        <el-form :model="form" label-width="0" size="large" class="register-form">
          <el-form-item>
            <el-input 
              v-model="form.name" 
              placeholder="真实姓名" 
              :prefix-icon="User"
              class="custom-input"
            />
          </el-form-item>
          
          <el-form-item>
            <el-input 
              v-model="form.phone" 
              placeholder="手机号" 
              :prefix-icon="Iphone" 
              maxlength="11"
              class="custom-input"
            />
          </el-form-item>
          
          <el-form-item>
            <el-input 
              v-model="form.idCard" 
              placeholder="身份证号" 
              :prefix-icon="CreditCard"
              maxlength="18"
              class="custom-input"
            />
          </el-form-item>
          
          <el-form-item>
            <div class="code-row">
              <el-input 
                v-model="form.code" 
                placeholder="请输入验证码" 
                maxlength="6"
                class="custom-input"
              >
                <template #prefix>
                  <el-icon><Key /></el-icon>
                </template>
              </el-input>
              <el-button 
                :disabled="countdown > 0" 
                @click="sendCode"
                class="code-btn"
              >
                {{ countdown > 0 ? `${countdown}s 后重试` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>
          
          <el-form-item>
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="密码 (至少 6 位)" 
              :prefix-icon="Lock" 
              show-password
              class="custom-input"
            />
          </el-form-item>
          
          <el-alert 
            type="warning" 
            :closable="false" 
            class="tip-alert"
            show-icon
          >
            <template #title>
              <div class="alert-content">
                <strong>温馨提示：</strong>
                <ul>
                  <li>注册后需等待管理员审核通过后方可接单</li>
                  <li>审核结果将通过短信通知</li>
                  <li>请确保填写的信息真实有效</li>
                </ul>
              </div>
            </template>
          </el-alert>
          
          <el-form-item>
            <el-button 
              type="primary" 
              class="submit-btn" 
              :loading="loading" 
              @click="submit"
            >
              {{ loading ? '注册中...' : '立即注册' }}
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="form-footer">
          <p class="tip">已有账号？<router-link to="/login">立即登录</router-link></p>
          <p class="tip2">提示：注册后需等待管理员审核通过后方可接单</p>
        </div>
      </div>
    </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { sendCode as apiSendCode } from '@/api/auth'
import api from '@/api'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { 
  User, 
  Iphone, 
  CreditCard, 
  Key,
  Lock, 
  Clock, 
  Money, 
  TrendCharts, 
  CircleCheckFilled
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const countdown = ref(0)
const form = reactive({ 
  name: '', 
  phone: '', 
  idCard: '', 
  code: '', 
  password: '' 
})

async function sendCode() {
  if (!form.phone || !/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning('请输入正确手机号')
    return
  }
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
  }
}

async function submit() {
  if (!form.name || !form.phone || !form.idCard || !form.code || !form.password) {
    ElMessage.warning('请填写所有必填项')
    return
  }
  if (form.password.length < 6) {
    ElMessage.warning('密码至少 6 位')
    return
  }
  if (!/^\d{17}[\dXx]$/.test(form.idCard)) {
    ElMessage.warning('请输入正确的身份证号')
    return
  }
  
  loading.value = true
  try {
    const res = await api.post('/rider/register', {
      name: form.name,
      phone: form.phone,
      idCard: form.idCard,
      password: form.password
    })
    
    const user = res?.data || res
    if (user && user.token) {
      userStore.setUser(user, user.token)
      userStore.setRiderStatus(true)
      
      // 加载未读通知数量
      try {
        const { useNotificationStore } = await import('@/stores/notification')
        const notificationStore = useNotificationStore()
        const { getUnreadCount } = await import('@/api/notification')
        const res2 = await getUnreadCount()
        notificationStore.setUnreadCount(res2.count || 0)
      } catch (error) {
        console.error('加载未读通知数量失败:', error)
      }
      
      ElMessage.success('注册成功，请等待管理员审核')
      router.push('/')
    } else {
      throw new Error('未获取到 token')
    }
  } catch (error) {
    console.error('注册失败:', error)
    const errorMsg = error?.msg || error?.message || '注册失败，请稍后重试'
    ElMessage.error(errorMsg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.rider-register-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 60px); /* 减去导航栏高度 */
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

/* 主体内容区域 */
.rider-register-body {
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
  box-shadow: -8px 0 32px rgba(0, 0, 0, 0.08);
  overflow-y: auto;
}

.left-content {
  max-width: 500px;
  padding: 40px;
  color: white;
  position: relative;
  z-index: 1;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
}

.logo-icon {
  font-size: 48px;
}

.brand-logo h1 {
  font-size: 36px;
  margin: 0;
  font-weight: 700;
}

.slogan {
  font-size: 28px;
  margin: 0 0 16px 0;
  font-weight: 600;
  line-height: 1.4;
}

.description {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 40px;
  line-height: 1.6;
}

/* 骑手优势列表 */
.benefits {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.benefit-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  transition: all 0.3s;
}

.benefit-item:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateX(8px);
}

.benefit-icon {
  font-size: 32px;
  color: #ffd93d;
}

.benefit-info h4 {
  font-size: 18px;
  margin: 0 0 4px 0;
  font-weight: 600;
}

.benefit-info span {
  font-size: 14px;
  opacity: 0.9;
}

/* 装饰元素 */
.decoration-icon {
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

/* 右侧区域 */
.register-right {
  width: 500px;
  background: white;
  display: flex;
  flex-direction: column;
  box-shadow: -8px 0 32px rgba(0, 0, 0, 0.08);
}

.form-container {
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
  padding: 80px 40px;
}

.form-header {
  margin-bottom: 32px;
}

.form-header h2 {
  font-size: 28px;
  color: var(--text-primary);
  margin: 0 0 8px 0;
  font-weight: 700;
}

.form-header p {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
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
}

.code-row .custom-input {
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

/* 提示框 */
.tip-alert {
  margin-bottom: 24px;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, #fff5f0 0%, #ffe4d6 100%);
  border: 1px solid #ffd1b3;
}

.alert-content ul {
  margin: 8px 0 0 20px;
  padding: 0;
  line-height: 1.8;
}

.alert-content li {
  color: var(--text-secondary);
  font-size: 13px;
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  padding: 14px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, #ff6b35 0%, #ff8f65 100%);
  border: none;
  transition: all 0.3s;
  color: white !important;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(255, 107, 53, 0.3);
}

.submit-btn:active {
  transform: translateY(0);
}

/* 底部 */
.form-footer {
  text-align: center;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.tip {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 8px 0;
}

.tip a {
  color: var(--primary);
  font-weight: 600;
  text-decoration: none;
}

.tip2 {
  font-size: 13px;
  color: var(--text-muted);
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .rider-register-page {
    flex-direction: column;
  }
  
  .register-left {
    min-height: auto;
    padding: 40px 20px;
  }
  
  .register-right {
    width: 100%;
    padding: 40px 20px;
  }
}

@media (max-width: 768px) {
  .benefits {
    gap: 12px;
  }
  
  .benefit-item {
    padding: 12px;
  }
}
</style>
