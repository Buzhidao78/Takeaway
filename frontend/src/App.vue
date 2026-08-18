<template>
  <div id="app-container">
    <!-- 添加全局错误显示 -->
    <div v-if="globalError" class="global-error">
      <p>全局错误：{{ globalError }}</p>
      <button @click="clearError">清除错误</button>
    </div>
    
    <ErrorBoundary>
      <Layout>
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" :key="$route.fullPath" />
          </transition>
        </router-view>
      </Layout>
    </ErrorBoundary>
  </div>
</template>

<script setup>
import ErrorBoundary from '@/components/ErrorBoundary.vue'
import Layout from '@/components/Layout.vue'
import { ref, computed, onErrorCaptured, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const isAuthPage = computed(() => {
  return ['/login', '/register', '/merchant-register', '/rider-register'].includes(route.path)
})

const globalError = ref(null)

onErrorCaptured((err) => {
  console.error('全局捕获错误:', err)
  globalError.value = err.message || '未知错误'
})

function clearError() {
  globalError.value = null
}

// 监听路由变化，清理认证页面的样式
watch(() => route.path, (newPath, oldPath) => {
  const authPages = ['/login', '/register', '/merchant-register', '/rider-register']
  const isLeavingAuthPage = authPages.includes(oldPath)
  const isEnteringNonAuthPage = !authPages.includes(newPath)
  
  if (isLeavingAuthPage && isEnteringNonAuthPage) {
    // 清理认证页面的样式
    document.body.style.overflow = ''
  }
})
</script>

<style>
html {
  scrollbar-gutter: stable;
}

body {
  scrollbar-gutter: stable;
}

#app-container {
  height: 100%;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.loading-fallback {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  font-size: 16px;
  color: #666;
}

.global-error {
  position: fixed;
  top: 10px;
  right: 10px;
  background: #ffe6e6;
  border: 1px solid #ff4444;
  padding: 10px;
  border-radius: 4px;
  z-index: 9999;
}

/* 修复 Element Plus 对话框遮罩层覆盖问题 */
.el-overlay {
  position: fixed !important;
  inset: 0 !important;  /* 覆盖所有方向，包括 scrollbar-gutter 预留空间 */
  width: 100% !important;
  height: 100% !important;
  margin: 0 !important;
  padding: 0 !important;
  z-index: 2000 !important;
}

.el-overlay-dialog {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>