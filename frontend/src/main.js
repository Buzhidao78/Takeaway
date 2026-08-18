import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import App from './App.vue'
import router from './router'
import './style.css'

console.log('Router配置:', router.options); // 调试路由配置
console.log('当前路由:', router.currentRoute.value); // 调试当前路由

console.log('开始加载main.js')

console.log('创建Vue应用实例')
const app = createApp(App)

// 注册图标组件
console.log('开始注册图标组件')
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
console.log('图标组件注册完成')

// 全局错误处理
console.log('设置全局错误处理')
app.config.errorHandler = (err, instance, info) => {
  console.error('Vue全局错误:', err)
  console.error('错误信息:', info)
  console.error('错误堆栈:', err.stack)
  
  // 创建一个错误提示元素
  if (typeof document !== 'undefined') {
    let errorDiv = document.getElementById('vue-error-overlay')
    if (!errorDiv) {
      errorDiv = document.createElement('div')
      errorDiv.id = 'vue-error-overlay'
      errorDiv.style.cssText = `
        position: fixed;
        top: 10px;
        right: 10px;
        background: #fee;
        color: #900;
        padding: 10px;
        border: 1px solid #fcc;
        border-radius: 4px;
        z-index: 10000;
        max-width: 400px;
        font-family: Arial, sans-serif;
        font-size: 14px;
      `
      document.body.appendChild(errorDiv)
    }
    errorDiv.innerHTML = `<strong>应用错误:</strong> ${err.message || '未知错误'}<br><small>详情请查看控制台</small>`
  }
}

console.log('开始使用插件')
app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })
console.log('插件使用完成')

console.log('开始挂载应用')
app.mount('#app')
console.log('应用已挂载')
