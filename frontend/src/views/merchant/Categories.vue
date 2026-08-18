<template>
  <div class="page">
    <h1>分类管理</h1>
    <el-button type="primary" @click="editCategory()" style="margin-bottom:20px">新增分类</el-button>
    <el-table :data="categories" v-loading="loading">
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="sort" label="排序" width="100" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button link @click="editCategory(row)">编辑</el-button>
          <el-button link type="danger" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" :title="form.id?'编辑':'新增'" width="500">
      <el-form :model="form" label-width="80">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { categories as fetchCategories, saveCategory, deleteCategory } from '@/api/merchant'
import { ElMessage } from 'element-plus'

const categories = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const form = reactive({ id: null, name: '', sort: 0 })

async function load() {
  loading.value = true
  try {
    categories.value = await fetchCategories()
  } catch (e) {
    console.error('加载分类失败:', e)
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function editCategory(row) {
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, { id: null, name: '', sort: 0 })
  }
  dialogVisible.value = true
}

async function del(row) {
  try {
    await deleteCategory(row.id)
    ElMessage.success('已删除')
    load()
  } catch (e) {
    console.error('删除失败:', e)
    ElMessage.error(e?.message || '删除失败')
  }
}

async function save() {
  try {
    await saveCategory(form)
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
.page {
  padding: 20px;
}
h1 {
  margin-bottom: 20px;
}
</style>
