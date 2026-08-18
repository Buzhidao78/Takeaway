<template>
  <div v-if="!hasError">
    <slot />
  </div>
  <div v-else class="error-boundary">
    <h3>页面加载出错</h3>
    <p>{{ errorMessage }}</p>
    <details v-if="errorDetails" style="margin-top: 10px;">
      <summary>错误详情</summary>
      <pre>{{ errorDetails }}</pre>
    </details>
    <button @click="resetError">重新加载</button>
  </div>
</template>

<script setup>
import { ref, onErrorCaptured } from 'vue'

const hasError = ref(false)
const errorMessage = ref('')
const errorDetails = ref('')

onErrorCaptured((err) => {
  hasError.value = true
  errorMessage.value = err.message || '未知错误'
  errorDetails.value = err.stack || '无堆栈信息'
  console.error('组件错误:', err)
  return false // 阻止错误继续冒泡
})

function resetError() {
  hasError.value = false
  errorMessage.value = ''
  errorDetails.value = ''
}
</script>

<style scoped>
.error-boundary {
  padding: 20px;
  text-align: center;
  background-color: #f8f9fa;
  border-radius: 8px;
  margin: 20px;
}
.error-boundary h3 {
  color: #dc3545;
}
.error-boundary button {
  margin-top: 10px;
  padding: 8px 16px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
</style>