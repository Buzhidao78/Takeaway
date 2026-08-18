<template>
  <div class="page">
    <h1 class="page-title">
      <el-icon><ShoppingCart /></el-icon>
      购物车
    </h1>

    <el-card v-if="!items.length" class="empty-card" shadow="hover">
      <el-empty 
        description="购物车空空如也，快去挑选美食吧～" 
        :image-size="200"
      >
        <el-button type="primary" @click="$router.push('/stores')">去逛一逛</el-button>
      </el-empty>
    </el-card>

    <el-card v-else class="cart-card" shadow="hover">
      <div class="cart-items">
        <div v-for="item in items" :key="item.id" class="cart-item">
          <div class="item-image">
            <el-image 
              v-if="item.dishImage" 
              :src="getImageUrl(item.dishImage)" 
              fit="cover" 
              class="item-img"
              :preview-src-list="[getImageUrl(item.dishImage)]"
            />
            <div v-else class="no-image-placeholder">
              <el-icon :size="32"><Food /></el-icon>
            </div>
          </div>
          
          <div class="item-info">
            <h3 class="item-name">{{ item.dishName || '菜品' }}</h3>
            <div class="item-meta">
              <span class="item-price">¥{{ item.price }}</span>
            </div>
          </div>

          <div class="item-quantity">
            <el-input-number 
              v-model="item.quantity" 
              :min="1" 
              :max="99" 
              size="large"
              @change="updateQty(item)"
            />
          </div>

          <div class="item-subtotal">
            <span class="subtotal-label">小计：</span>
            <span class="subtotal-price">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
          </div>

          <div class="item-actions">
            <el-button 
              type="danger" 
              link 
              size="large"
              @click="remove(item)"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </div>
      </div>

      <el-divider />

      <div class="cart-footer">
        <div class="cart-total">
          <span class="total-label">合计：</span>
          <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
        </div>
        <el-button 
          type="primary" 
          size="large"
          class="checkout-btn"
          @click="goConfirm"
        >
          <el-icon><ShoppingCart /></el-icon>
          去结算
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { updateCartQty, removeCart } from '@/api/cart'
import { useCartStore } from '@/stores/cart'
import { ElMessage } from 'element-plus'
import { ShoppingCart, Delete, Food } from '@element-plus/icons-vue'

const router = useRouter()
const cartStore = useCartStore()
const items = computed(() => cartStore.items)
const storeId = computed(() => items.value[0]?.storeId)
const totalCount = computed(() => items.value.reduce((sum, item) => sum + item.quantity, 0))
const totalPrice = computed(() => items.value.reduce((sum, item) => sum + item.price * item.quantity, 0))

onMounted(async () => {
  await cartStore.fetchCart()
})

async function updateQty(item) {
  try {
    await updateCartQty(item.id, item.quantity)
    await cartStore.fetchCart()
    ElMessage.success('数量已更新')
  } catch (e) {
    ElMessage.error('更新失败')
  }
}

async function remove(item) {
  try {
    await removeCart(item.id)
    items.value = items.value.filter(x => x.id !== item.id)
    await cartStore.fetchCart()
    ElMessage.success('删除成功')
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

function goConfirm() {
  if (storeId.value) router.push({ path: '/order/confirm', query: { storeId: storeId.value } })
}

function getImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return `${import.meta.env.VITE_API_BASE_URL || ''}${url}`
}
</script>

<style scoped>
.page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 24px;
}

.page-title .el-icon {
  color: var(--primary);
  font-size: 32px;
}

.empty-card,
.cart-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid #ebeef5;
  background: #fff;
}

.cart-items {
  margin-bottom: 0;
}

.cart-item {
  display: grid;
  grid-template-columns: 100px 1fr auto auto auto;
  gap: 20px;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.cart-item:last-child {
  border-bottom: none;
}

.item-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
}

.item-img {
  width: 100%;
  height: 100%;
  cursor: pointer;
  transition: transform 0.3s;
}

.item-img:hover {
  transform: scale(1.05);
}

.no-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e0e0e0 100%);
  color: #999;
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.item-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  line-height: 1.5;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.item-price {
  font-size: 18px;
  font-weight: 600;
  color: var(--primary);
}

.item-quantity {
  display: flex;
  align-items: center;
}

.item-subtotal {
  text-align: right;
}

.subtotal-label {
  font-size: 14px;
  color: var(--text-secondary);
  margin-right: 8px;
}

.subtotal-price {
  font-size: 20px;
  font-weight: 600;
  color: var(--primary);
}

.item-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
}

.cart-total {
  display: flex;
  align-items: center;
  gap: 8px;
}

.total-label {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.total-price {
  font-size: 28px;
  font-weight: 700;
  color: var(--primary);
}

.checkout-btn {
  font-size: 18px;
  padding: 12px 40px;
  border-radius: 25px;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .cart-item {
    grid-template-columns: 80px 1fr;
    gap: 12px;
  }
  
  .item-image {
    width: 80px;
    height: 80px;
  }
  
  .item-quantity,
  .item-subtotal,
  .item-actions {
    grid-column: 1 / -1;
  }
  
  .item-quantity {
    justify-content: flex-start;
  }
  
  .item-subtotal {
    text-align: left;
  }
  
  .item-actions {
    justify-content: flex-end;
  }
  
  .cart-footer {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .cart-total {
    justify-content: center;
  }
  
  .checkout-btn {
    width: 100%;
  }
}
</style>
