<template>
  <div class="forgot-password-page">
    <!-- 主体内容区域 -->
    <div class="forgot-password-body">
      <!-- 左侧品牌展示 -->
      <div class="forgot-password-left">
        <div class="left-content">
          <div class="brand-logo">
            <span class="logo-icon">🍱</span>
            <h1>BZY 外卖</h1>
          </div>
          <h2 class="slogan">重置密码</h2>
          <p class="description">验证手机号，设置新密码</p>
          
          <!-- 重置流程 -->
          <div class="steps">
            <div class="step-item" :class="{ active: currentStep >= 1 }">
              <div class="step-number">1</div>
              <div class="step-info">
                <h4>验证手机号</h4>
                <p>输入注册手机号</p>
              </div>
            </div>
            <div class="step-item" :class="{ active: currentStep >= 2 }">
              <div class="step-number">2</div>
              <div class="step-info">
                <h4>验证身份</h4>
                <p>输入验证码</p>
              </div>
            </div>
            <div class="step-item" :class="{ active: currentStep >= 3 }">
              <div class="step-number">3</div>
              <div class="step-info">
                <h4>重置密码</h4>
                <p>设置新密码</p>
              </div>
            </div>
          </div>
          
          <!-- 装饰元素 -->
          <div class="decoration-food decoration-1">🎉</div>
          <div class="decoration-food decoration-2">🎁</div>
          <div class="decoration-food decoration-3">✨</div>
        </div>
      </div>
      
      <!-- 右侧表单 -->
      <div class="forgot-password-right">
        <div class="form-container">
          <div class="form-header">
            <h2>{{ formTitle }}</h2>
            <p>{{ formDescription }}</p>
          </div>
          
          <el-form :model="form" label-width="0" size="large" class="forgot-password-form">
            <!-- 步骤 1：输入手机号 -->
            <template v-if="currentStep === 1">
              <el-form-item>
                <el-input 
                  v-model="form.phone" 
                  placeholder="请输入注册手机号" 
                  maxlength="11"
                  class="custom-input"
                >
                  <template #prefix>
                    <el-icon><Iphone /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              
              <el-form-item>
                <el-button 
                  type="primary" 
                  class="submit-btn" 
                  :loading="loading"
                  @click="handlePhoneSubmit"
                >
                  下一步
                </el-button>
              </el-form-item>
            </template>
            
            <!-- 步骤 2：输入验证码 -->
            <template v-if="currentStep === 2">
              <el-form-item>
                <div class="phone-display">
                  <span class="phone-label">手机号：</span>
                  <span class="phone-value">{{ form.phone }}</span>
                  <el-link type="primary" @click="currentStep = 1" class="edit-link">修改</el-link>
                </div>
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
                    :disabled="countdown > 0 || !form.phone" 
                    @click="sendCode"
                    class="code-btn"
                    :loading="sendingCode"
                  >
                    {{ countdown > 0 ? countdown + 's' : '获取验证码' }}
                  </el-button>
                </div>
              </el-form-item>
              
              <el-form-item>
                <el-button 
                  type="primary" 
                  class="submit-btn" 
                  :loading="loading"
                  @click="handleCodeSubmit"
                >
                  下一步
                </el-button>
              </el-form-item>
            </template>
            
            <!-- 步骤 3：重置密码 -->
            <template v-if="currentStep === 3">
              <el-form-item>
                <div class="phone-display">
                  <span class="phone-label">手机号：</span>
                  <span class="phone-value">{{ form.phone }}</span>
                </div>
              </el-form-item>
              
              <el-form-item>
                <el-input 
                  v-model="form.password" 
                  type="password" 
                  placeholder="请输入新密码（至少 6 位）" 
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
                  v-model="form.confirmPassword" 
                  type="password" 
                  placeholder="请确认新密码" 
                  show-password
                  class="custom-input"
                >
                  <template #prefix>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              
              <el-form-item>
                <el-button 
                  type="primary" 
                  class="submit-btn" 
                  :loading="loading"
                  @click="handlePasswordReset"
                >
                  确认重置
                </el-button>
              </el-form-item>
            </template>
          </el-form>
          
          <div class="form-footer">
            <el-divider>或</el-divider>
            <p class="tip">
              已有账号？
              <router-link to="/login" class="link">立即登录</router-link>
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Iphone, Lock, Key, Message } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getCaptcha, sendCode as sendCodeApi, resetPassword } from '@/api/auth'

const router = useRouter()

const currentStep = ref(1)
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)

const form = reactive({
  phone: '',
  code: '',
  password: '',
  confirmPassword: ''
})

const formTitle = computed(() => {
  const titles = {
    1: '验证手机号',
    2: '验证身份',
    3: '重置密码'
  }
  return titles[currentStep.value]
})

const formDescription = computed(() => {
  const descriptions = {
    1: '请输入注册时使用的手机号',
    2: '已向您的手机发送验证码',
    3: '请设置新的登录密码'
  }
  return descriptions[currentStep.value]
})

// 发送验证码
async function sendCode() {
  if (!form.phone || !/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  
  sendingCode.value = true
  countdown.value = 60
  
  try {
    await sendCodeApi(form.phone, 'resetPassword')
    ElMessage.success('验证码已发送，请注意查收')
    
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
        sendingCode.value = false
      }
    }, 1000)
  } catch (error) {
    console.error('发送验证码失败:', error)
    ElMessage.error(error?.msg || '发送验证码失败')
    sendingCode.value = false
    countdown.value = 0
  }
}

// 步骤 1：提交手机号
async function handlePhoneSubmit() {
  if (!form.phone || !/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  
  loading.value = true
  try {
    // TODO: 验证手机号是否已注册
    await new Promise(resolve => setTimeout(resolve, 500))
    currentStep.value = 2
    ElMessage.success('手机号验证通过')
  } catch (error) {
    console.error('手机号验证失败:', error)
    ElMessage.error('手机号验证失败')
  } finally {
    loading.value = false
  }
}

// 步骤 2：提交验证码
async function handleCodeSubmit() {
  if (!form.code || form.code.length !== 6) {
    ElMessage.warning('请输入 6 位验证码')
    return
  }
  
  loading.value = true
  try {
    // TODO: 验证验证码
    await new Promise(resolve => setTimeout(resolve, 500))
    currentStep.value = 3
    ElMessage.success('验证通过')
  } catch (error) {
    console.error('验证码验证失败:', error)
    ElMessage.error('验证码错误')
  } finally {
    loading.value = false
  }
}

// 步骤 3：重置密码
async function handlePasswordReset() {
  if (!form.password || form.password.length < 6) {
    ElMessage.warning('密码至少 6 位')
    return
  }
  
  if (form.password !== form.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  
  loading.value = true
  try {
    const res = await resetPassword(form.phone, form.password, form.code)
    console.log('密码重置成功响应:', res)
    
    ElMessage.success('密码重置成功')
    setTimeout(() => {
      router.push('/login')
    }, 1500)
  } catch (error) {
    console.error('密码重置失败详情:', error)
    console.error('错误响应:', error.response)
    console.error('错误数据:', error.response?.data)
    ElMessage.error(error?.msg || error?.message || '密码重置失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.forgot-password-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 60px);
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.forgot-password-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.forgot-password-left {
  flex: 1;
  background: linear-gradient(135deg, #ff6b35 0%, #ff8f65 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.forgot-password-right {
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

.steps {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-top: 40px;
}

.step-item {
  display: flex;
  align-items: center;
  gap: 16px;
  opacity: 0.5;
  transition: all 0.3s;
}

.step-item.active {
  opacity: 1;
}

.step-number {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 18px;
  flex-shrink: 0;
}

.step-item.active .step-number {
  background: white;
  color: #ff6b35;
}

.step-info h4 {
  font-size: 16px;
  margin: 0 0 4px 0;
}

.step-info p {
  font-size: 14px;
  margin: 0;
  opacity: 0.8;
}

.form-container {
  padding: 80px 40px;
}

.form-header {
  margin-bottom: 40px;
}

.form-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px 0;
}

.form-header p {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.forgot-password-form {
  margin-bottom: 24px;
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

.phone-display {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: var(--radius-md);
  margin-bottom: 24px;
}

.phone-label {
  color: #666;
  font-size: 14px;
}

.phone-value {
  color: #333;
  font-weight: 600;
  font-size: 14px;
}

.edit-link {
  margin-left: auto;
  font-size: 14px;
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

.submit-btn {
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

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(255, 107, 53, 0.3);
  background: linear-gradient(135deg, #ff8f65 0%, #ffa080 100%);
}

.submit-btn:active {
  transform: translateY(0);
}

.form-footer {
  text-align: center;
}

.tip {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.link {
  color: #ff6b35;
  font-weight: 600;
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
}

.decoration-food {
  position: absolute;
  font-size: 80px;
  opacity: 0.2;
  animation: float 6s ease-in-out infinite;
}

.decoration-1 {
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.decoration-2 {
  bottom: 20%;
  left: 20%;
  animation-delay: 2s;
}

.decoration-3 {
  top: 30%;
  right: 15%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(10deg);
  }
}
</style>
