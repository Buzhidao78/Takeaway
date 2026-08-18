<template>
  <div class="page">
    <h1>收货地址</h1>
    
    <el-card>
      <div class="address-toolbar">
        <el-button type="primary" @click="showAddDialog">
          <el-icon><Plus /></el-icon>
          新增地址
        </el-button>
        <span class="address-count">共 {{ addresses.length }} 个地址</span>
      </div>
      
      <el-empty v-if="addresses.length === 0" description="暂无收货地址" />
      
      <div v-else class="address-list">
        <div v-for="addr in addresses" :key="addr.id" class="address-item" :class="{ default: addr.isDefault }">
          <div class="address-main">
            <div class="address-header">
              <div class="contact-section">
                <el-icon class="user-icon"><User /></el-icon>
                <span class="name">{{ addr.contactName }}</span>
                <span class="phone">{{ formatPhone(addr.contactPhone) }}</span>
              </div>
              <div class="address-tags">
                <el-tag v-if="addr.isDefault" type="success" size="small" effect="plain">默认</el-tag>
                <el-tag v-if="addr.zoneLabel" type="info" size="small" effect="plain">{{ addr.zoneLabel }}</el-tag>
                <el-tag v-if="addr.addressType" type="warning" size="small" effect="plain">{{ addressTypeMap[addr.addressType] }}</el-tag>
              </div>
            </div>
            
            <div class="address-detail">
              <el-icon class="location-icon"><Location /></el-icon>
              <span>{{ addr.fullAddress }}</span>
            </div>
          </div>
          
          <div class="address-actions">
            <div class="action-buttons">
              <el-button 
                text 
                type="primary" 
                @click="edit(addr)"
                class="action-btn"
              >
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button 
                text 
                type="danger" 
                @click="del(addr)"
                class="action-btn"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
              <el-button 
                text
                type="info"
                @click="setDefault(addr)" 
                v-if="!addr.isDefault"
                class="action-btn"
              >
                <el-icon><Star /></el-icon>
                设为默认
              </el-button>
              <el-button 
                text
                type="warning"
                v-else
                @click="handleDefaultAddress"
                class="action-btn"
              >
                <el-icon class="filled-star"><Star /></el-icon>
                默认地址
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-card>
    
    <el-dialog v-model="dialogVisible" :title="editing ? '编辑地址' : '新增地址'" width="650px">
      <el-form :model="form" label-width="90px" label-position="left">
        <el-form-item label="联系人" required>
          <el-input v-model="form.contactName" placeholder="请输入联系人姓名" prefix-icon="User" />
        </el-form-item>
        
        <el-form-item label="手机号" required>
          <el-input v-model="form.contactPhone" placeholder="请输入手机号" maxlength="11" prefix-icon="Iphone" />
        </el-form-item>
        
        <el-form-item label="所在地区" required>
          <el-cascader
            v-model="selectedArea"
            :options="areaOptions"
            :props="{ value: 'value', label: 'label', children: 'children' }"
            placeholder="请选择省市区"
            style="width: 100%"
            @change="onAreaChange"
          />
        </el-form-item>
        
        <el-form-item label="街道/乡镇">
          <el-input v-model="form.street" placeholder="如：粤海街道、路名" />
        </el-form-item>
        
        <el-form-item label="详细地址" required>
          <el-input 
            v-model="form.detailAddress" 
            type="textarea" 
            :rows="3"
            placeholder="如：路名、门牌号、楼栋、房号等详细信息"
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="区域标签">
              <el-select v-model="form.zoneLabel" placeholder="选择区域标签" style="width: 100%">
                <el-option label="校内" value="校内" />
                <el-option label="校外-A 区" value="校外-A 区" />
                <el-option label="校外-B 区" value="校外-B 区" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="地址类型">
              <el-select v-model="form.addressType" placeholder="选择地址类型" style="width: 100%">
                <el-option label="家" :value="1" />
                <el-option label="公司" :value="2" />
                <el-option label="学校" :value="3" />
                <el-option label="其他" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="设为默认">
          <el-switch v-model="formIsDefault" :active-value="1" :inactive-value="0" active-text="默认地址" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">
          <el-icon><Check /></el-icon>
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { addressList, saveAddress, deleteAddress } from '@/api/address'
import { ElMessage } from 'element-plus'
import { areaData } from '@/utils/area-data'
import { Plus, User, Location, Edit, Star, Delete, Check, Iphone } from '@element-plus/icons-vue'

const addressTypeMap = {
  1: '家',
  2: '公司',
  3: '学校',
  4: '其他'
}

const addresses = ref([])
const dialogVisible = ref(false)
const editing = ref(false)
const selectedArea = ref([])

const form = reactive({
  id: null,
  contactName: '',
  contactPhone: '',
  province: '',
  city: '',
  district: '',
  street: '',
  zoneLabel: '',
  addressType: 1,
  detailAddress: '',
  fullAddress: '',
  isDefault: 0
})

const formIsDefault = computed({
  get: () => form.isDefault,
  set: (val) => { form.isDefault = val }
})

const areaOptions = ref([])

// 格式化手机号
const formatPhone = (phone) => {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 加载地址列表
async function load() {
  try {
    const res = await addressList()
    addresses.value = Array.isArray(res) ? res : []
  } catch (e) {
    console.error('加载地址失败:', e)
    ElMessage.error(e?.message || '加载失败')
  }
}

// 显示新增对话框
function showAddDialog() {
  editing.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑地址
function edit(addr) {
  editing.value = true
  // 手动映射字段，确保字段名正确
  form.id = addr.id
  form.contactName = addr.contactName || ''
  form.contactPhone = addr.contactPhone || ''
  form.province = addr.province || ''
  form.city = addr.city || ''
  form.district = addr.district || ''
  form.street = addr.street || ''
  form.zoneLabel = addr.zoneLabel || ''
  form.addressType = addr.addressType || 1
  form.fullAddress = addr.fullAddress || ''
  form.isDefault = addr.isDefault || 0
  
  // 解析详细地址：从完整地址中提取街道后面的部分
  if (addr.fullAddress) {
    const streetIndex = addr.street ? addr.fullAddress.indexOf(addr.street) : -1
    if (streetIndex >= 0 && addr.street) {
      // 详细地址 = 完整地址 - 省市区街道
      const streetEndIndex = streetIndex + addr.street.length
      form.detailAddress = addr.fullAddress.substring(streetEndIndex)
    } else {
      // 如果没有街道信息，尝试从省市区后面截取
      const districtIndex = addr.district ? addr.fullAddress.indexOf(addr.district) : -1
      if (districtIndex >= 0 && addr.district) {
        form.detailAddress = addr.fullAddress.substring(districtIndex + addr.district.length)
      } else {
        form.detailAddress = addr.fullAddress
      }
    }
  } else {
    form.detailAddress = ''
  }
  
  // 设置地区选择器
  selectedArea.value = [addr.province, addr.city, addr.district]
  dialogVisible.value = true
}

// 设为默认
async function setDefault(addr) {
  try {
    addr.isDefault = 1
    await saveAddress(addr)
    ElMessage.success('设置成功')
    load()
  } catch (e) {
    ElMessage.error(e?.message || '设置失败')
  }
}

// 点击默认地址提示
function handleDefaultAddress() {
  ElMessage.info('该地址已是默认地址')
}

// 删除地址
async function del(addr) {
  try {
    await deleteAddress(addr.id)
    ElMessage.success('已删除')
    load()
  } catch (e) {
    ElMessage.error(e?.message || '删除失败')
  }
}

// 地区选择变化
const onAreaChange = (value) => {
  if (value && value.length >= 3) {
    form.province = value[0]
    form.city = value[1]
    form.district = value[2]
  }
}

// 保存地址
async function save() {
  if (!form.contactName || !form.contactPhone) {
    ElMessage.warning('请填写联系人和手机号')
    return
  }
  
  if (!form.province || !form.city || !form.district) {
    ElMessage.warning('请选择所在地区')
    return
  }
  
  if (!form.detailAddress) {
    ElMessage.warning('请填写详细地址')
    return
  }
  
  // 构建完整地址
  form.fullAddress = `${form.province}${form.city}${form.district}${form.street || ''}${form.detailAddress}`
  
  try {
    await saveAddress(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    resetForm()
    load()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  }
}

// 重置表单
function resetForm() {
  form.id = null
  form.contactName = ''
  form.contactPhone = ''
  form.province = ''
  form.city = ''
  form.district = ''
  form.street = ''
  form.zoneLabel = ''
  form.addressType = 1
  form.detailAddress = ''
  form.fullAddress = ''
  form.isDefault = 0
  selectedArea.value = []
}

// 加载省市区数据
onMounted(() => {
  areaOptions.value = areaData
  load()
})
</script>

<style scoped>
.address-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.address-count {
  color: #999;
  font-size: 14px;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.address-item {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 16px 20px;
  background: #fff;
  transition: all 0.3s;
  display: flex;
  justify-content: space-between;
  align-items: stretch;
  gap: 16px;
}

.address-item.default {
  border-color: var(--el-color-primary);
  background: linear-gradient(135deg, #f0f9ff 0%, #fff 100%);
}

.address-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.address-main {
  flex: 1;
  min-width: 0;
}

.address-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 12px;
}

.contact-section {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.user-icon {
  color: var(--el-color-primary);
  font-size: 18px;
  flex-shrink: 0;
}

.contact-section .name {
  font-weight: 600;
  font-size: 15px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.contact-section .phone {
  color: #666;
  font-size: 14px;
  flex-shrink: 0;
}

.address-tags {
  display: flex;
  gap: 6px;
  flex-wrap: nowrap;
  flex-shrink: 0;
}

.address-detail {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  color: #666;
  line-height: 1.6;
  font-size: 14px;
}

.location-icon {
  color: var(--el-color-primary);
  margin-top: 2px;
  flex-shrink: 0;
}

.address-actions {
  display: flex;
  align-items: stretch;
  padding-left: 20px;
  border-left: 1px solid #e0e0e0;
  flex-shrink: 0;
  min-width: 120px;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 100px;
}

.action-buttons .el-button,
.action-buttons .action-btn {
  justify-content: flex-start !important;
  padding: 8px 12px !important;
  font-size: 14px;
  transition: all 0.3s;
  min-height: 32px;
  width: 100%;
  text-align: left !important;
  box-sizing: border-box;
  display: inline-flex !important;
  align-items: center !important;
  margin: 0 !important;
}

.action-buttons .el-button:hover,
.action-buttons .action-btn:hover {
  background-color: #f5f7fa;
  transform: translateX(4px);
}

/* 默认地址的五角星填充黄色 */
.filled-star {
  color: #e6a23c !important;
  margin-right: 4px;
}

.filled-star svg {
  fill: #e6a23c !important;
}

.action-buttons .default-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  font-size: 13px;
  opacity: 0.8;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .address-item {
    flex-direction: column;
    gap: 12px;
  }
  
  .address-actions {
    width: 100%;
    padding-left: 0;
    padding-top: 12px;
    border-left: none;
    border-top: 1px dashed #e0e0e0;
    min-width: auto;
  }
  
  .action-buttons {
    flex-direction: row;
    flex-wrap: wrap;
    width: 100%;
    justify-content: center;
    align-items: center;
  }
  
  .action-buttons .el-button {
    flex: 1;
    min-width: 70px;
    justify-content: center;
    text-align: center;
  }
  
  .action-buttons .default-tag {
    margin: 0 auto;
  }
}

.form-tip {
  margin-left: 8px;
  color: #999;
  font-size: 12px;
}

/* 对话框样式优化 */
:deep(.el-dialog__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #e0e0e0;
}

:deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
}

:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-dialog__footer) {
  padding: 12px 20px;
  border-top: 1px solid #e0e0e0;
}
</style>
