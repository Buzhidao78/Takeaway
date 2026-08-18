<template>
  <div class="page">
    <h1>菜品管理</h1>
    <el-button type="primary" @click="editDish()" style="margin-bottom:20px">新增菜品</el-button>
    <el-table :data="dishesList" v-loading="loading">
      <el-table-column prop="name" label="名称" />
      <el-table-column label="图片" width="100">
        <template #default="{ row }">
          <div v-if="!row.image" class="no-image-placeholder">
            <el-icon :size="24"><Picture /></el-icon>
            <span>暂无图片</span>
          </div>
          <el-image 
            v-else
            :src="getImageUrl(row.image)" 
            fit="cover" 
            style="width: 60px; height: 60px; cursor: pointer; border-radius: 4px;"
            @click="openImageViewer(row.image)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="price" label="价格">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="{ row }"> {{ row.status === 1 ? '上架' : '下架' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button link @click="editDish(row)">编辑</el-button>
          <el-button link type="danger" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" :title="form.id?'编辑':'新增'" width="600">
      <el-form :model="form" label-width="80">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="选择分类" style="width:100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="原价"><el-input-number v-model="form.originPrice" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="上架" /></el-form-item>
        <el-form-item label="图片">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :file-list="imagesFileList"
            :on-change="handleImagesChange"
            :on-remove="handleImagesRemove"
            :on-success="handleImagesSuccess"
            :on-error="handleImageError"
            list-type="picture-card"
            :limit="9"
            multiple
            :on-preview="handlePreview"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="tip">可上传多张菜品图片，最多 9 张（第一张将作为主图展示）</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { Plus, Picture } from '@element-plus/icons-vue'
import { dishes as fetchDishes, saveDish, deleteDish, categories as fetchCategories } from '@/api/merchant'
import { ElMessage } from 'element-plus'

const dishesList = ref([])
const categories = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const form = reactive({ id: null, name: '', categoryId: null, price: 0, originPrice: null, description: '', status: 1, image: '', images: '' })
const imagesFileList = ref([])
const uploadUrl = '/api/file/upload'
const uploadHeaders = {
  Authorization: 'Bearer ' + (localStorage.getItem('token') || '')
}

async function load() {
  loading.value = true
  try {
    const [d, c] = await Promise.all([fetchDishes(), fetchCategories()])
    dishesList.value = Array.isArray(d) ? d : []
    categories.value = Array.isArray(c) ? c : []
    if (categories.value.length && !form.categoryId) form.categoryId = categories.value[0].id
  } catch (e) {
    console.error('加载菜品失败:', e)
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function editDish(row) {
  if (row) {
    // 先清空文件列表
    imagesFileList.value = []
    
    // 打开对话框
    dialogVisible.value = true
    
    // 在对话框打开后初始化图片文件列表
    nextTick(() => {
      // 合并主图和详情图
      const allImages = []
      
      // 添加主图（如果有）
      if (row.image) {
        // 确保 URL 格式正确，用于显示
        const imageUrl = row.image.startsWith('/api') ? row.image : '/api' + row.image
        allImages.push({
          name: 'image',
          url: imageUrl,
          status: 'done',
          uid: Date.now()
        })
        console.log('添加主图:', imageUrl, '原始数据:', row.image)
      }
      
      // 添加详情图
      if (row.images && typeof row.images === 'string') {
        const imageUrls = row.images.split(',').filter(url => url && url.trim())
        console.log('详情图原始数据:', row.images)
        console.log('分割后的 URLs:', imageUrls)
        const processedUrls = imageUrls.map(url => {
          // 确保 URL 格式正确，用于显示
          return url.startsWith('/api') ? url : '/api' + url
        })
        processedUrls.forEach((url, idx) => {
          allImages.push({
            name: `images-${idx}`,
            url: url,
            status: 'done',
            uid: Date.now() + idx + 1
          })
        })
        console.log('处理后的 URLs:', processedUrls)
      }
      
      imagesFileList.value = allImages
      console.log('初始化后的文件列表:', imagesFileList.value, '总数:', imagesFileList.value.length)
      
      // 最后再赋值 form，避免覆盖
      Object.assign(form, row)
      console.log('表单数据:', form)
    })
  } else {
    if (!categories.value.length) {
      ElMessage.warning('请先添加菜品分类')
      return
    }
    Object.assign(form, { id: null, name: '', categoryId: categories.value[0].id, price: 0, originPrice: null, description: '', status: 1, image: '', images: '' })
    imagesFileList.value = []
    dialogVisible.value = true
  }
}

async function del(row) {
  try {
    await deleteDish(row.id)
    ElMessage.success('已删除')
    load()
  } catch (e) {
    console.error('删除失败:', e)
    ElMessage.error(e?.message || '删除失败')
  }
}

const handleImagesChange = (file, files) => {
  imagesFileList.value = files
  console.log('文件列表变化:', files)
}

const handleImagesRemove = (file, files) => {
  imagesFileList.value = files
  console.log('移除文件后的列表:', files)
  
  // 从当前文件列表中获取所有已上传成功的图片 URL
  const urls = files
    .map(f => {
      // 优先使用 file.url（已存在的图片）
      if (f.url) {
        // 去掉 /api 前缀，保存原始路径
        return f.url.startsWith('/api') ? f.url.replace('/api', '') : f.url
      }
      // 或者使用 file.response?.data（新上传的图片）
      if (f.response?.data) {
        return f.response.data
      }
      return null
    })
    .filter(Boolean)
  
  console.log('提取的图片 URLs:', urls)
  
  // 第一张作为主图，其余的作为详情图
  if (urls.length > 0) {
    form.image = urls[0]
    form.images = urls.slice(1).join(',')
  } else {
    form.image = ''
    form.images = ''
  }
  console.log('更新后的 form.image:', form.image)
  console.log('更新后的 form.images:', form.images)
}

const handleImagesSuccess = (response, file, files) => {
  console.log('图片上传响应:', response, '文件:', file, '文件列表:', files)
  try {
    const res = typeof response === 'string' ? JSON.parse(response) : response
    console.log('解析后的响应:', res)
    if (res.code === 0 || res.code === 200) {
      // 从当前文件列表中获取所有已上传成功的图片 URL
      const urls = files
        .map(f => {
          // 优先使用 file.response?.data（刚上传的图片）
          if (f.response) {
            const r = typeof f.response === 'string' ? JSON.parse(f.response) : f.response
            return r?.data
          }
          // 或者使用 file.url（已存在的图片）
          if (f.url) {
            return f.url.startsWith('/api') ? f.url.replace('/api', '') : f.url
          }
          return null
        })
        .filter(Boolean)
      
      console.log('提取的图片 URLs:', urls)
      
      // 第一张作为主图，其余的作为详情图
      if (urls.length > 0) {
        form.image = urls[0]
        form.images = urls.slice(1).join(',')
      } else {
        form.image = ''
        form.images = ''
      }
      console.log('更新后的 form.image:', form.image)
      console.log('更新后的 form.images:', form.images)
    }
  } catch (e) {
    console.error('解析上传响应失败:', e)
  }
}

const handleImageError = (err, file, files) => {
  console.error('图片上传失败:', err)
  ElMessage.error('图片上传失败，请重试')
}

// 打开图片预览
const handlePreview = (file) => {
  console.log('预览文件:', file)
  const url = file.url || (file.response?.data)
  if (url) {
    const imageUrl = url.startsWith('/api') ? url : '/api' + url
    console.log('预览 URL:', imageUrl)
    // 使用 window.open 在新窗口打开图片
    window.open(imageUrl, '_blank')
  }
}

const openImageViewer = (url) => {
  if (!url) {
    ElMessage.warning('没有图片')
    return
  }
  const imageUrl = url.startsWith('/api') ? url : '/api' + url
  window.open(imageUrl, '_blank')
}

const getImageUrl = (path) => {
  if (!path) return '/placeholder.png'
  if (path.startsWith('http')) return path
  // 确保路径有 /api 前缀用于代理
  const url = path.startsWith('/api') ? path : '/api' + path
  return url
}

async function save() {
  console.log('保存菜品数据:', form)
  try {
    await saveDish(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } catch (e) {
    console.error('保存失败:', e)
    ElMessage.error(e?.message || '保存失败')
  }
}

onMounted(load)
</script>

<style scoped>
.tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

/* 暂无图片占位符 */
.no-image-placeholder {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 11px;
  gap: 2px;
}

/* 优化图片上传组件样式 */
:deep(.el-upload-list--picture-card) {
  margin-top: 0;
}

:deep(.el-upload-list__item) {
  transition: all 0.3s;
}

:deep(.el-upload-list__item:hover) {
  transform: translateY(-2px);
}
</style>
