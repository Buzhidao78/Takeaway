<template>
  <div class="page">
    <h1>配送设置</h1>
    
    <el-card>
      <h3>基础配置</h3>
      
      <el-form :model="config" label-width="140px" style="max-width: 600px">
        <el-form-item label="基础配送费">
          <el-input-number 
            v-model="config.baseFee" 
            :min="0" 
            :max="100" 
            :precision="2"
            :step="0.5"
            style="width: 200px"
          />
          <span class="form-tip">元（建议 3-8 元）</span>
        </el-form-item>
        
        <el-form-item label="满免门槛">
          <el-input-number 
            v-model="config.freeDeliveryThreshold" 
            :min="0" 
            :max="1000" 
            :precision="2"
            :step="5"
            style="width: 200px"
          />
          <span class="form-tip">元（订单满此金额免基础配送费）</span>
        </el-form-item>
        
        <el-form-item label="最低起送金额">
          <el-input-number 
            v-model="config.minOrderAmount" 
            :min="0" 
            :max="1000" 
            :precision="2"
            :step="5"
            style="width: 200px"
          />
          <span class="form-tip">元</span>
        </el-form-item>
        
        <el-form-item label="启用配送">
          <el-switch v-model="configEnabled" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="saveConfig">保存配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-card style="margin-top: 20px" class="zone-card">
      <div class="zone-header">
        <h3>配送区域管理</h3>
        <el-button type="primary" @click="showAddZoneDialog">添加区域</el-button>
      </div>
      
      <el-table :data="zones" style="width: 100%" v-loading="loading">
        <el-table-column prop="province" label="省" min-width="100" align="center" />
        <el-table-column prop="city" label="市" min-width="100" align="center" />
        <el-table-column prop="district" label="区/县" min-width="100" align="center" />
        <el-table-column prop="zoneLabel" label="区域标签" min-width="120" align="center" />
        <el-table-column prop="additionalFee" label="附加费" min-width="100" align="center">
          <template #default="{ row }">
            ¥{{ row.additionalFee }}
          </template>
        </el-table-column>
        <el-table-column prop="minOrderAmount" label="最低金额" min-width="100" align="center">
          <template #default="{ row }">
            ¥{{ row.minOrderAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="isAvailable" label="可配送" min-width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isAvailable ? 'success' : 'danger'" size="small">
              {{ row.isAvailable ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" header-align="center">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button link type="primary" @click="editZone(row)" class="action-btn">编辑</el-button>
              <el-button link type="danger" @click="deleteZone(row.id)" class="action-btn">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 添加/编辑区域对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle"
      width="500px"
    >
      <el-form :model="zoneForm" label-width="100px">
        <el-form-item label="所在地区">
          <el-cascader
            v-model="selectedArea"
            :options="areaOptions"
            :props="{ value: 'value', label: 'label', children: 'children' }"
            style="width: 300px"
            @change="onAreaChange"
          />
        </el-form-item>
        
        <el-form-item label="区域标签">
          <el-input 
            v-model="zoneForm.zoneLabel" 
            placeholder="如：校内、校外-A 区"
            style="width: 200px"
          />
          <span class="form-tip">（可选）</span>
        </el-form-item>
        
        <el-form-item label="附加费">
          <el-input-number 
            v-model="zoneForm.additionalFee" 
            :min="0" 
            :max="100" 
            :precision="2"
            :step="0.5"
            style="width: 150px"
          />
          <span class="form-tip">元</span>
        </el-form-item>
        
        <el-form-item label="最低金额">
          <el-input-number 
            v-model="zoneForm.minOrderAmount" 
            :min="0" 
            :max="1000" 
            :precision="2"
            :step="5"
            style="width: 150px"
          />
          <span class="form-tip">元</span>
        </el-form-item>
        
        <el-form-item label="可配送">
          <el-switch v-model="zoneForm.isAvailable" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveZone">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMerchantConfig, saveMerchantConfig, getDeliveryZones, saveDeliveryZone, deleteDeliveryZone } from '@/api/merchant'
import { areaData } from '@/utils/area-data'

const config = ref({
  baseFee: 5.00,
  freeDeliveryThreshold: 50.00,
  minOrderAmount: 20.00,
  isEnabled: 1
})

const configEnabled = computed({
  get: () => config.value.isEnabled === 1,
  set: (val) => {
    config.value.isEnabled = val ? 1 : 0
  }
})

const zones = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('添加区域')
const selectedArea = ref([])

const zoneForm = ref({
  province: '',
  city: '',
  district: '',
  zoneLabel: '',
  additionalFee: 0,
  minOrderAmount: 0,
  isAvailable: 1
})

// 省市区数据
const areaOptions = ref([])

// 加载配置
const loadConfig = async () => {
  try {
    const res = await getMerchantConfig()
    if (res) {
      config.value = res
    }
  } catch (e) {
    console.error('加载配置失败', e)
  }
}

// 加载区域列表
const loadZones = async () => {
  loading.value = true
  try {
    const res = await getDeliveryZones()
    zones.value = res || []
  } catch (e) {
    console.error('加载区域列表失败', e)
  } finally {
    loading.value = false
  }
}

// 保存配置
const saveConfig = async () => {
  try {
    await saveMerchantConfig(config.value)
    ElMessage.success('保存成功')
    loadConfig()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

// 显示添加区域对话框
const showAddZoneDialog = () => {
  dialogTitle.value = '添加区域'
  zoneForm.value = {
    province: '',
    city: '',
    district: '',
    zoneLabel: '',
    additionalFee: 0,
    minOrderAmount: 0,
    isAvailable: 1
  }
  selectedArea.value = []
  dialogVisible.value = true
}

// 编辑区域
const editZone = (zone) => {
  dialogTitle.value = '编辑区域'
  zoneForm.value = { 
    ...zone,
    isAvailable: zone.isAvailable === 1
  }
  selectedArea.value = [zone.province, zone.city, zone.district]
  dialogVisible.value = true
}

// 删除区域
const deleteZone = (id) => {
  ElMessageBox.confirm('确定要删除该配送区域吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    lockScroll: false
  }).then(async () => {
    try {
      await deleteDeliveryZone(id)
      ElMessage.success('删除成功')
      loadZones()
    } catch (e) {
      ElMessage.error('删除失败')
    }
  })
}

// 地区选择变化
const onAreaChange = (value) => {
  if (value && value.length >= 3) {
    zoneForm.value.province = value[0]
    zoneForm.value.city = value[1]
    zoneForm.value.district = value[2]
  }
}

// 保存区域
const saveZone = async () => {
  if (!zoneForm.value.province || !zoneForm.value.city || !zoneForm.value.district) {
    ElMessage.warning('请选择所在地区')
    return
  }
  
  // 将布尔值转换为 Integer 类型
  const data = {
    ...zoneForm.value,
    isAvailable: zoneForm.value.isAvailable ? 1 : 0,
    additionalFee: Number(zoneForm.value.additionalFee),
    minOrderAmount: Number(zoneForm.value.minOrderAmount)
  }
  
  console.log('保存区域数据:', data)
  
  try {
    await saveDeliveryZone(data)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadZones()
  } catch (e) {
    console.error('保存失败详情:', e)
    ElMessage.error('保存失败：' + (e.message || '未知错误'))
  }
}

// 加载省市区数据
const loadAreaData = () => {
  // 简化版省市区数据，实际项目应该使用完整的省市区数据
  areaOptions.value = areaData
}

onMounted(async () => {
  loadAreaData()
  await loadConfig()
  await loadZones()
})
</script>

<style scoped>
.zone-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.form-tip {
  margin-left: 8px;
  color: #999;
  font-size: 12px;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
}

.action-buttons .action-btn {
  padding: 0 !important;
  margin: 0 !important;
  width: 48px !important;
  min-width: 48px !important;
  max-width: 48px !important;
}

.zone-card {
  margin: 0;
}

.zone-card :deep(.el-card__body) {
  padding: 20px;
}

.zone-card :deep(.el-table) {
  width: 100%;
}

.zone-card :deep(.el-table th) {
  background-color: #fafafa;
  color: #666;
  font-weight: 600;
}

.zone-card :deep(.el-table td) {
  padding: 12px 0;
}
</style>
