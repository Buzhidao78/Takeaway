<template>
  <el-dialog v-model="visible" :title="isAdditionalReview ? '商品追评' : '商品评价'" width="600px" @close="handleClose">
    <div class="product-info" v-if="orderItem">
      <img :src="getImageUrl(orderItem.dishImage)" alt="商品图片" />
      <div class="info">
        <div class="name">{{ orderItem.dishName }}</div>
        <div class="price">¥{{ orderItem.price }}</div>
      </div>
    </div>

    <el-alert
      v-if="isAdditionalReview"
      title="您已对该商品进行过评价，本次提交将作为追评内容"
      type="warning"
      :closable="false"
      style="margin-bottom: 16px"
    />

    <el-form :model="form" label-width="80px">
      <el-form-item label="评分">
        <el-rate v-model="form.rating" :colors="['#99A9BF', '#F7BA2A', '#FF9900']" />
      </el-form-item>
      <el-form-item label="评价">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="4"
          :placeholder="isAdditionalReview ? '请写下您的追评感受...' : '请写下您的用餐感受...'"
        />
      </el-form-item>
      <el-form-item label="图片">
        <el-upload
          action="/api/file/uploads"
          name="files"
          :file-list="fileList"
          :on-change="handleFileChange"
          :on-remove="handleRemove"
          :on-success="handleSuccess"
          :on-error="handleError"
          list-type="picture-card"
          :limit="5"
          multiple
          :headers="uploadHeaders"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">提交</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { submitProductReview } from '@/api/productReview'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: Boolean,
  orderItem: Object,
  orderId: Number,
  storeId: Number
})

const emit = defineEmits(['update:modelValue', 'submitted'])

const visible = ref(false)
const submitting = ref(false)
const fileList = ref([])
const imageUrls = ref([])
const isAdditionalReview = ref(false)

// 上传请求头，携带认证 token
const uploadHeaders = {
  Authorization: 'Bearer ' + (localStorage.getItem('token') || '')
}

const form = ref({
  rating: 5,
  content: ''
})

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.orderItem?.reviewed) {
    // 已评价，打开追评对话框
    isAdditionalReview.value = true
  } else {
    isAdditionalReview.value = false
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
  if (!val) {
    resetForm()
  }
})

const resetForm = () => {
  form.value = {
    rating: 5,
    content: ''
  }
  fileList.value = []
  imageUrls.value = []
}

// 获取图片 URL，确保有 /api 前缀
const getImageUrl = (path) => {
  if (!path) return '/placeholder.png'
  if (path.startsWith('http')) return path
  return path.startsWith('/api') ? path : '/api' + path
}

const handleFileChange = (file, files) => {
  fileList.value = files
}

const handleRemove = (file, files) => {
  fileList.value = files
}

const handleSuccess = (response, file, files) => {
  console.log('图片上传成功，响应:', response, '文件:', file, '所有文件:', files)
  // 兼容不同的响应格式
  const code = response.code || response.status
  if (code === 0 || code === 200) {
    // 更新所有图片 URL
    imageUrls.value = files
      .filter(f => {
        const resp = f.response || f
        const respCode = resp.code || resp.status
        return respCode === 0 || respCode === 200
      })
      .map(f => {
        const resp = f.response || f
        return resp.data || resp.url
      })
    console.log('图片 URLs:', imageUrls.value)
  }
}

const handleError = () => {
  ElMessage.error('图片上传失败')
}

const handleSubmit = async () => {
  if (!form.value.content.trim()) {
    ElMessage.warning('请输入评价内容')
    return
  }

  console.log('提交评价，图片 URLs:', imageUrls.value)

  submitting.value = true
  try {
    const data = {
      orderItemId: props.orderItem.id,
      orderId: props.orderId,
      storeId: props.storeId,
      dishId: props.orderItem.dishId,
      rating: form.value.rating,
      content: form.value.content,
      images: imageUrls.value.join(',')
    }
    
    console.log('提交数据:', data)
    await submitProductReview(data)
    ElMessage.success('评价成功')
    visible.value = false
    emit('submitted')
  } catch (error) {
    ElMessage.error('评价失败：' + (error.response?.data?.msg || error.message))
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  resetForm()
}
</script>

<style scoped>
.product-info {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: #f5f5f5;
  border-radius: 8px;
  margin-bottom: 20px;
}

.product-info img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.info {
  flex: 1;
}

.name {
  font-weight: bold;
  margin-bottom: 8px;
}

.price {
  color: #ff6b35;
  font-weight: bold;
}

:deep(.el-upload-list__item) {
  transition: all 0.3s;
}

:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
}

:deep(.el-upload-list__item) {
  width: 100px;
  height: 100px;
}
</style>
