<template>
  <div class="page">
    <h1>确认订单</h1>
    
    <div class="order-confirm-container">
      <!-- 左侧信息 -->
      <div class="left-section">
        <!-- 收货地址 -->
        <el-card class="address-card">
          <div class="card-header">
            <h3>收货地址</h3>
            <el-button text type="primary" @click="$router.push('/user/address')">管理地址</el-button>
          </div>
          
          <div v-if="selectedAddress" class="address-info" @click="showAddressDialog = true">
            <div class="address-detail">
              <div class="contact">
                <span class="name">{{ selectedAddress.contactName }}</span>
                <span class="phone">{{ formatPhone(selectedAddress.contactPhone) }}</span>
              </div>
              <div class="address">
                {{ selectedAddress.fullAddress }}
              </div>
              <div v-if="selectedAddress.zoneLabel" class="zone-tag">
                <el-tag size="small">{{ selectedAddress.zoneLabel }}</el-tag>
              </div>
            </div>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
          
          <div v-else class="no-address" @click="showAddressDialog = true">
            <el-empty :image-size="60" description="请选择收货地址" />
          </div>
        </el-card>
        
        <!-- 订单备注 -->
        <el-card style="margin-top: 16px">
          <h3>订单备注</h3>
          <el-input 
            v-model="remark" 
            type="textarea" 
            :rows="2"
            placeholder="选填：如不要香菜、少辣等"
          />
        </el-card>
      </div>
      
      <!-- 右侧信息 -->
      <div class="right-section">
        <el-card>
          <h3>商品清单</h3>
          
          <div class="items-list">
            <div v-for="item in items" :key="item.id" class="item">
              <div class="item-info">
                <span class="name">{{ item.dishName }}</span>
                <span class="quantity">x{{ item.quantity }}</span>
              </div>
              <div class="item-price">
                ¥{{ (item.price * item.quantity).toFixed(2) }}
              </div>
            </div>
          </div>
          
          <el-divider />
          
          <div class="price-row">
            <span>商品金额</span>
            <span>¥{{ goodsAmount.toFixed(2) }}</span>
          </div>
          
          <div v-if="packagingFee > 0" class="price-row">
            <span>包装费</span>
            <span>¥{{ packagingFee.toFixed(2) }}</span>
          </div>
          
          <div v-if="deliveryFee > 0" class="price-row">
            <span>配送费</span>
            <span>¥{{ deliveryFee.toFixed(2) }}</span>
          </div>
          
          <div v-if="deliveryFee === 0 && deliveryMessage" class="price-row free-delivery">
            <span>配送费</span>
            <span>¥0.00 <el-tag type="success" size="small">{{ deliveryMessage }}</el-tag></span>
          </div>
          
          <el-divider />
          
          <div class="total-row">
            <span>实付款</span>
            <span class="total-amount">¥{{ totalAmount.toFixed(2) }}</span>
          </div>
          
          <div v-if="!canDeliver && deliveryMessage" class="delivery-error">
            <el-alert type="error" :closable="false" show-icon>
              {{ deliveryMessage }}
            </el-alert>
          </div>
          
          <el-button 
            type="primary" 
            size="large" 
            :loading="loading"
            :disabled="!canDeliver"
            @click="submit"
            style="width: 100%; margin-top: 16px"
          >
            提交订单
          </el-button>
        </el-card>
      </div>
    </div>
    
    <!-- 地址选择对话框 -->
    <el-dialog v-model="showAddressDialog" title="选择收货地址" width="600px">
      <div class="address-dialog-content">
        <div 
          v-for="addr in addresses" 
          :key="addr.id" 
          class="address-option"
          :class="{ selected: selectedAddress?.id === addr.id }"
          @click="selectAddress(addr)"
        >
          <div class="address-option-content">
            <div class="contact-info">
              <span class="name">{{ addr.contactName }}</span>
              <span class="phone">{{ formatPhone(addr.contactPhone) }}</span>
            </div>
            <div class="address-detail">
              {{ addr.fullAddress }}
            </div>
            <div class="address-tags">
              <el-tag v-if="addr.isDefault" type="success" size="small">默认</el-tag>
              <el-tag v-if="addr.zoneLabel" size="small">{{ addr.zoneLabel }}</el-tag>
            </div>
          </div>
          <el-icon v-if="selectedAddress?.id === addr.id" class="selected-icon"><Check /></el-icon>
        </div>
        
        <div v-if="addresses.length === 0" class="no-address">
          <el-empty description="暂无收货地址" />
          <el-button type="primary" @click="goToAddAddress">新增地址</el-button>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showAddressDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmAddress">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { addressList } from '@/api/address'
import { createOrder, payOrder } from '@/api/order'
import { useCartStore } from '@/stores/cart'
import { useNotificationStore } from '@/stores/notification'
import { ElMessage } from 'element-plus'
import { ArrowRight, Check } from '@element-plus/icons-vue'
import api from '@/api/index'

const route = useRoute()
const router = useRouter()
const storeId = Number(route.query.storeId)
const notificationStore = useNotificationStore()

const items = ref([])
const addresses = ref([])
const selectedAddress = ref(null)
const remark = ref('')
const loading = ref(false)
const showAddressDialog = ref(false)
const cartStore = useCartStore()

// 费用相关
const goodsAmount = ref(0)
const packagingFee = ref(0)
const deliveryFee = ref(0)
const totalAmount = ref(0)
const canDeliver = ref(true)
const deliveryMessage = ref('')

// 格式化手机号
const formatPhone = (phone) => {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 计算配送费
const calculateDeliveryFee = async () => {
  if (!selectedAddress.value || !storeId) {
    console.log('计算配送费跳过：selectedAddress=', selectedAddress.value, 'storeId=', storeId)
    return
  }
  
  try {
    // 计算商品总金额
    const goodsTotal = items.value.reduce((sum, item) => sum + (item.price || 0) * (item.quantity || 0), 0)
    
    console.log('计算配送费请求参数：', {
      storeId: Number(storeId),
      province: selectedAddress.value.province,
      city: selectedAddress.value.city,
      district: selectedAddress.value.district,
      zoneLabel: selectedAddress.value.zoneLabel,
      goodsAmount: goodsTotal
    })
    
    // 调用后端 API 计算配送费
    const res = await api.post('/merchant/delivery/fee/calculate', {
      storeId: Number(storeId),
      province: selectedAddress.value.province,
      city: selectedAddress.value.city,
      district: selectedAddress.value.district,
      zoneLabel: selectedAddress.value.zoneLabel,
      goodsAmount: goodsTotal
    })
    
    console.log('计算配送费响应：', res)
    
    if (res && res.canDeliver) {
      deliveryFee.value = res.deliveryFee || 0
      canDeliver.value = true
      deliveryMessage.value = deliveryFee.value === 0 ? '免配送费' : ''
    } else {
      deliveryFee.value = 0
      canDeliver.value = false
      deliveryMessage.value = res?.message || '该地址不支持配送'
    }
  } catch (e) {
    console.error('计算配送费失败:', e)
    deliveryFee.value = 0
    canDeliver.value = false
    deliveryMessage.value = '配送费计算失败，请稍后重试'
  }
}

// 计算总金额
const calculateTotal = () => {
  goodsAmount.value = items.value.reduce((sum, item) => sum + (item.price || 0) * (item.quantity || 0), 0)
  
  // 包装费（暂时固定为 1 元，后续可由商家设置）
  packagingFee.value = 1
  
  totalAmount.value = goodsAmount.value + packagingFee.value + deliveryFee.value
}

onMounted(async () => {
  await cartStore.fetchCart()
  const addrRes = await addressList()
  
  const all = Array.isArray(cartStore.items) ? cartStore.items : []
  items.value = all.filter(i => i.storeId === storeId)
  addresses.value = Array.isArray(addrRes) ? addrRes : []
  
  // 默认选中默认地址
  if (addresses.value.length) {
    selectedAddress.value = addresses.value.find(a => a.isDefault) || addresses.value[0]
  }
  
  // 计算费用
  calculateTotal()
  
  // 计算配送费
  if (selectedAddress.value) {
    await calculateDeliveryFee()
    // 重新计算总金额（包含配送费）
    calculateTotal()
  }
})

// 选择地址
const selectAddress = (addr) => {
  selectedAddress.value = addr
}

// 确认地址
const confirmAddress = async () => {
  showAddressDialog.value = false
  
  // 重新计算配送费
  if (selectedAddress.value) {
    await calculateDeliveryFee()
    calculateTotal()
  }
}

// 前往新增地址
const goToAddAddress = () => {
  router.push('/user/address')
  showAddressDialog.value = false
}

async function submit() {
  if (!selectedAddress.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  
  if (!canDeliver.value) {
    ElMessage.warning('该地址不支持配送')
    return
  }
  
  loading.value = true
  try {
    // 创建订单时传递配送费信息
    const orderData = {
      storeId,
      addressId: selectedAddress.value.id,
      remark: remark.value,
      packagingFee: packagingFee.value,
      deliveryFee: deliveryFee.value
    }
    
    const res = await createOrder(orderData)
    const orderId = res?.id || res?.data?.id
    
    // 乐观更新：下单成功后立即增加通知角标（不等 WebSocket 推送）
    notificationStore.incrementUnreadCount()
    
    // 调用支付接口，获取支付宝表单
    const payRes = await payOrder(orderId)
    
    // 如果返回的是支付宝表单 HTML，直接打开新窗口
    if (payRes && typeof payRes === 'string' && payRes.includes('<form')) {
      // 创建临时 div 并插入表单
      const div = document.createElement('div')
      div.innerHTML = payRes
      document.body.appendChild(div)
      
      // 提交表单跳转到支付宝
      const form = div.querySelector('form')
      if (form) {
        form.submit()
      }
      
      // 清理临时 div
      setTimeout(() => {
        document.body.removeChild(div)
      }, 1000)
    } else {
      // 模拟支付成功（开发环境）
      ElMessage.success('支付成功')
      router.push('/user/orders')
      cartStore.fetchCart()
    }
  } catch (e) {
    console.error('提交订单失败:', e)
    ElMessage.error(e?.message || '提交失败')
  } finally {
    loading.value = false
  }
}

async function payOrderSuccess(orderId) {
  await api.post(`/order/${orderId}/pay/success`)
}
</script>

<style scoped>
.order-confirm-container {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.left-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.address-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.address-info:hover {
  border-color: var(--primary);
  background: #f0f9ff;
}

.contact {
  margin-bottom: 8px;
}

.contact .name {
  font-weight: 600;
  margin-right: 16px;
}

.address {
  color: #666;
  line-height: 1.6;
}

.zone-tag {
  margin-top: 8px;
}

.arrow-icon {
  font-size: 20px;
  color: #999;
}

.no-address {
  cursor: pointer;
}

.items-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-info {
  display: flex;
  gap: 12px;
  align-items: center;
}

.item-info .quantity {
  color: #999;
}

.price-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  color: #666;
}

.price-row.free-delivery {
  color: #67c23a;
}

.total-row {
  display: flex;
  justify-content: space-between;
  padding: 16px 0;
  font-size: 18px;
  font-weight: 600;
}

.total-amount {
  color: #ff6b35;
}

.delivery-error {
  margin-top: 16px;
}

.address-dialog-content {
  max-height: 400px;
  overflow-y: auto;
}

.address-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.address-option:hover {
  border-color: var(--primary);
  background: #f0f9ff;
}

.address-option.selected {
  border-color: var(--primary);
  background: #f0f9ff;
}

.address-option-content {
  flex: 1;
}

.contact-info {
  margin-bottom: 8px;
}

.contact-info .name {
  font-weight: 600;
  margin-right: 16px;
}

.address-detail {
  color: #666;
  line-height: 1.6;
  margin-bottom: 8px;
}

.address-tags {
  display: flex;
  gap: 8px;
}

.selected-icon {
  font-size: 24px;
  color: var(--primary);
}

.no-address {
  text-align: center;
  padding: 20px;
}
</style>
