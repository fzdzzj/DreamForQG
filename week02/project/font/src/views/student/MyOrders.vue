<template>
  <!-- 我的报修单页面容器 -->
  <div class="my-orders">
    <el-card>
      <!-- 卡片标题 + 创建报修单按钮 -->
      <template #header>
        <div class="card-header">
          <span>📋 我的报修单</span>
          <el-button type="primary" @click="router.push('/student/create-order')">
            创建报修单
          </el-button>
        </div>
      </template>
      
      <!-- 报修单表格 -->
      <el-table :data="orderList" style="width: 100%" v-loading="loading">
        <!-- 报修单ID -->
        <el-table-column prop="id" label="单号" width="80" />
        
        <!-- 设备类型：数字转中文 -->
        <el-table-column prop="deviceType" label="设备类型" width="100">
          <template #default="{ row }">
            {{ getDeviceTypeName(row.deviceType) }}
          </template>
        </el-table-column>
        
        <!-- 问题描述 -->
        <el-table-column prop="description" label="问题描述" min-width="200" />
        
        <!-- 状态：带不同颜色标签 -->
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <!-- 优先级：普通/紧急/非常紧急 -->
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="{ row }">
            {{ getPriorityName(row.priority) }}
          </template>
        </el-table-column>
        
        <!-- 创建时间 -->
        <el-table-column prop="createTime" label="创建时间" width="180" />
        
        <!-- 操作栏：取消 + 查看详情 -->
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <!-- 只有待维修的报修单才能取消 -->
            <el-button 
              v-if="row.status === '1' " 
              type="danger" 
              size="small"
              @click="handleCancel(row.id)"
            >
              取消
            </el-button>
            
            <!-- 查看详情按钮 -->
            <el-button 
              type="primary" 
              size="small"
              @click="handleViewDetail(row.id)"
            >
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 空数据提示 -->
      <div v-if="orderList.length === 0 && !loading" class="empty">
        <el-empty description="暂无报修单" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

// 报修单接口：获取我的报修单、取消报修单
import { getMyOrders, cancelOrder } from '@/api/order'

const router = useRouter()

// 报修单列表数据
const orderList = ref([])
// 加载状态
const loading = ref(true)

// ==================== 枚举映射：后端数字 → 前端中文 ====================
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

// 报修状态：名称 + 标签颜色
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

// ==================== 工具函数 ====================
const getDeviceTypeName = (type) => deviceTypeMap[type] || '未知'
const getStatusName = (status) => statusMap[status]?.name || '未知'
const getStatusType = (status) => statusMap[status]?.type || 'info'
const getPriorityName = (priority) => priorityMap[priority] || '未知'

// ==================== 加载我的报修单列表 ====================
const loadOrders = async () => {
  loading.value = true
  try {
    const res = await getMyOrders()
    orderList.value = res.data || []
  } catch (error) {
    console.error('加载失败:', error)
  } finally {
    loading.value = false
  }
}

// ==================== 取消报修单 ====================
const handleCancel = async (id) => {
  try {
    // 取消确认框
    await ElMessageBox.confirm('确定要取消此报修单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用取消接口
    await cancelOrder(id)
    ElMessage.success('取消成功')
    // 重新加载列表
    loadOrders()
  } catch (error) {
    // 用户取消操作不提示错误
    if (error !== 'cancel') {
      console.error('取消失败:', error)
    }
  }
}

// ==================== 查看报修单详情 ====================
const handleViewDetail = (id) => {
  router.push(`/student/order-detail/${id}`)
}

// 页面加载时自动获取列表
onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.my-orders {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty {
  padding: 40px 0;
  text-align: center;
}
</style>