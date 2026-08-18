<template>
  <div class="page">
    <h1>个人中心</h1>
    <el-card>
      <div class="user-info">
        <el-avatar :size="64">{{ (userStore.user?.nickname || 'U')[0] }}</el-avatar>
        <div>
          <h3>{{ userStore.user?.nickname }}</h3>
          <p>{{ userStore.user?.phone }}</p>
        </div>
      </div>
      <el-divider />
      <el-menu>
        <el-menu-item @click="$router.push('/user/orders')">
          <el-icon><List /></el-icon>
          <span>我的订单</span>
        </el-menu-item>
        <el-menu-item @click="$router.push('/user/address')">
          <el-icon><Location /></el-icon>
          <span>收货地址</span>
        </el-menu-item>
        <template v-if="userStore.isRider">
          <el-divider />
          <el-menu-item @click="$router.push('/rider/home')">
            <el-icon><Van /></el-icon>
            <span>骑手中心</span>
          </el-menu-item>
          <el-menu-item @click="$router.push('/rider/grab')">
            <el-icon><ShoppingCart /></el-icon>
            <span>抢单大厅</span>
          </el-menu-item>
          <el-menu-item @click="$router.push('/rider/delivery')">
            <el-icon><Van /></el-icon>
            <span>配送订单</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { List, Location, Van, ShoppingCart } from '@element-plus/icons-vue'

const userStore = useUserStore()

onMounted(async () => {
  console.log('个人中心加载，登录状态:', userStore.isLogin)
  console.log('token:', localStorage.getItem('token'))
  console.log('user:', localStorage.getItem('user'))
  
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    setTimeout(() => {
      location.hash = '#/login'
    }, 1000);
    return
  }
  
  try {
    await userStore.refreshProfile();
    console.log('用户信息加载成功:', userStore.user)
  } catch (error) {
    console.error('刷新用户信息失败:', error)
    ElMessage.error('用户信息加载失败，请重新登录')
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    setTimeout(() => {
      location.hash = '#/login'
    }, 1500);
    return
  }
  
  if (!userStore.user) {
    ElMessage.error('用户信息加载失败')
    return
  }

  // 检查骑手状态
  await userStore.checkRiderStatus()
})
</script>

<style scoped>
.user-info { display: flex; align-items: center; gap: 20px; }
</style>
