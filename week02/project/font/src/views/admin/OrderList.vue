<template>
  <!-- 报修单管理页面容器 -->
  <div class="order-list">
    <el-card>
      <!-- 卡片头部：标题 + 批量删除按钮 -->
      <template #header>
        <div class="card-header">
          <span>📋 报修单管理</span>
          <el-button 
            type="danger" 
            size="small"
            @click="handleBatchDelete"
            :disabled="selectedOrders.length === 0"
          >
            批量删除
          </el-button>
        </div>
      </template>
      
      <!-- 搜索条件表单：状态、楼栋筛选 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="待维修" value="1" />
            <el-option label="已完成" value="2" />
            <el-option label="已取消" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼栋">
          <el-select v-model="searchForm.dormBuilding" placeholder="全部" clearable>
            <el-option label="A 栋" value="A 栋" />
            <el-option label="B 栋" value="B 栋" />
            <el-option label="C 栋" value="C 栋" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 报修单表格：多选、加载动画、操作列 -->
      <el-table 
        :data="orderList" 
        style="width: 100%" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <!-- 复选框列 -->
        <el-table-column type="selection" width="55" />

        <el-table-column prop="id" label="单号" width="80" />
        <el-table-column prop="dormBuilding" label="楼栋" width="80" />
        <el-table-column prop="dormRoom" label="房间" width="80" />
        
        <!-- 设备类型：数字转中文 -->
        <el-table-column prop="deviceType" label="设备类型" width="100">
          <template #default="{ row }">
            {{ getDeviceTypeName(row.deviceType) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="description" label="问题描述" min-width="200" />
        
        <!-- 状态：带颜色标签 -->
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <!-- 优先级 -->
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="{ row }">
            {{ getPriorityName(row.priority) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="创建时间" width="180" />
        
        <!-- 操作列：完成、查看、删除 -->
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === '1'" 
              type="success" 
              size="small"
              @click="handleComplete(row.id)"
            >
              完成
            </el-button>
            <el-button 
              type="primary" 
              size="small"
              @click="handleViewDetail(row.id)"
            >
              查看
            </el-button>
            <el-button 
              type="danger" 
              size="small"
              @click="handleDelete(row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
// 导入Vue响应式API
import { ref, reactive, onMounted } from 'vue'
// 导入路由：用于页面跳转
import { useRouter } from 'vue-router'
// 导入Element Plus提示框
import { ElMessage, ElMessageBox } from 'element-plus'
// 导入报修单接口
import { getAllOrders, updateOrderStatus, deleteOrder ,queryOrders} from '@/api/order'

// 报修单列表数据
const orderList = ref([])
// 加载状态
const loading = ref(true)
// 选中的报修单（用于批量删除）
const selectedOrders = ref([])
// 路由实例
const router = useRouter()

// 搜索条件
const searchForm = reactive({
  status: '',
  dormBuilding: ''
})

// ==================== 枚举映射：数字 → 中文 / 颜色 ====================
// 设备类型
const deviceTypeMap = {
  '1': '水龙头',
  '2': '马桶',
  '3': '电灯',
  '4': '窗户',
  '5': '门',
  '6': '床',
  '7': '水槽',
  '8': '电表',
  '9': '水表'
}

// 状态：名称 + 标签颜色
const statusMap = {
  '1': { name: '待维修', type: 'warning' },
  '2': { name: '已完成', type: 'success' },
  '3': { name: '已取消', type: 'info' }
}

// 优先级
const priorityMap = {
  '1': '普通',
  '2': '紧急',
  '3': '非常紧急'
}

// 工具方法：获取对应文本
const getDeviceTypeName = (type) => deviceTypeMap[type] || '未知'
const getStatusName = (status) => statusMap[status]?.name || '未知'
const getStatusType = (status) => statusMap[status]?.type || 'info'
const getPriorityName = (priority) => priorityMap[priority] || '未知'

// ==================== 加载所有报修单 ====================
const loadOrders = async () => {
  loading.value = true
  try {
    // 请求所有报修单
    const res = await getAllOrders()
    
    // 清空原有数据
    orderList.value.splice(0)
    const data = res.data || []
    // 追加新数据
    if (data.length) orderList.value.push(...data)

  } catch (error) {
    console.error('加载失败:', error)
  } finally {
    loading.value = false
  }
}

// ==================== 按条件搜索报修单 ====================
const handleSearch = async () => {
  try {
    // 调用条件查询接口
    const res = await queryOrders({
      status: searchForm.status || '',
      dormBuilding: searchForm.dormBuilding || ''
    })
    
    // 清空列表
    orderList.value.splice(0)
    const data = res.data || {}
    const list = data.list || []
    // 追加搜索结果
    if (list.length) orderList.value.push(...list)

  } catch (error) {
    console.error('查询失败:', error)
  }
}

// ==================== 重置搜索条件 ====================
const handleReset = () => {
  searchForm.status = ''
  searchForm.dormBuilding = ''
  // 重新加载全部数据
  loadOrders()
}

// ==================== 标记为已完成 ====================
const handleComplete = async (id) => {
  try {
    // 确认框
    await ElMessageBox.confirm('确定要标记为已完成吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用接口修改状态为 2（已完成）
    await updateOrderStatus(id, { status: 2 })
    ElMessage.success('操作成功')
    // 刷新列表
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
    }
  }
}

// ==================== 删除单条报修单 ====================
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除此报修单吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    
    await deleteOrder(id)
    ElMessage.success('删除成功')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// ==================== 跳转到详情页 ====================
const handleViewDetail = (id) => {
  router.push(`/admin/order/${id}`)
}

// ==================== 表格多选事件 ====================
const handleSelectionChange = (selection) => {
  // 保存选中的订单
  selectedOrders.value = selection
}

// ==================== 批量删除选中的报修单 ====================
const handleBatchDelete = async () => {
  if (selectedOrders.value.length === 0) return
  
  try {
    await ElMessageBox.confirm('确定要删除选中的报修单吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    
    // 提取选中的ID
    const ids = selectedOrders.value.map(order => order.id)
    // 循环删除
    for (const id of ids) {
      await deleteOrder(id)
    }
    
    ElMessage.success('批量删除成功')
    selectedOrders.value = []
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// ==================== 页面加载时获取数据 ====================
onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.order-list {
  max-width: 1400px;
  margin: 0 auto;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.search-form {
  margin-bottom: 20px;
}
</style>