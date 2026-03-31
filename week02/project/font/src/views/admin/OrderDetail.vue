<template>
  <!-- 报修单详情页面容器 -->
  <div class="order-detail">
    <el-card>
      <!-- 卡片头部：标题 + 返回按钮 -->
      <template #header>
        <div class="card-header">
          <span>📋 报修单详情</span>
          <el-button @click="router.back()">返回</el-button>
        </div>
      </template>
      
      <!-- 详情展示列表：2列布局，带边框，加载动画 -->
      <el-descriptions :column="2" border v-loading="loading">
        <el-descriptions-item label="报修单号">{{ order.id }}</el-descriptions-item>
        <el-descriptions-item label="学生账号">{{ order.studentAccount }}</el-descriptions-item>
        <el-descriptions-item label="宿舍楼栋">{{ order.dormBuilding }}</el-descriptions-item>
        <el-descriptions-item label="房间号">{{ order.dormRoom }}</el-descriptions-item>
        
        <!-- 设备类型：数字转中文 -->
        <el-descriptions-item label="设备类型">{{ deviceTypeMap[order.deviceType] || '未知' }}</el-descriptions-item>
        
        <!-- 优先级：带颜色标签 -->
        <el-descriptions-item label="优先级">
          <el-tag :type="getPriorityType(order.priority)">
            {{ priorityNameMap[order.priority] || '未知' }}
          </el-tag>
        </el-descriptions-item>
        
        <!-- 状态：带颜色标签 -->
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(order.status)">
            {{ statusNameMap[order.status] || '未知' }}
          </el-tag>
        </el-descriptions-item>
        
        <el-descriptions-item label="创建时间">{{ order.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ order.updateTime }}</el-descriptions-item>
        
        <!-- 问题描述：可展开/收起 -->
        <el-descriptions-item label="问题描述" :span="2">
          <div 
            :class="['description-text', { 'full-description': showFullDescription }]"
            @click="showFullDescription = !showFullDescription"
          >
            {{ order.description }}
            <span class="toggle-text">{{ showFullDescription ? ' (收起)' : ' (展开)' }}</span>
          </div>
        </el-descriptions-item>
        
        <!-- 上传图片列表：有图片才显示 -->
        <el-descriptions-item label="上传图片" :span="2" v-if="order.images && order.images.length > 0">
          <div class="image-list">
            <el-image
              v-for="(img, index) in order.images"
              :key="index"
              :src="img"
              :preview-src-list="order.images"
              class="image-item"
              fit="cover"
            />
          </div>
        </el-descriptions-item>
      </el-descriptions>
      
      <!-- 操作按钮：只有状态为 待处理(1) 时才显示 -->
      <div class="action-buttons" v-if="order.status === '1'">
        <el-button type="success" @click="handleComplete">标记为完成</el-button>
        <el-button type="danger" @click="handleDelete">删除报修单</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
// 导入Vue响应式与生命周期
import { ref, onMounted } from 'vue'
// 导入路由：返回、获取当前路由参数
import { useRouter, useRoute } from 'vue-router'
// 导入消息提示、确认框
import { ElMessage, ElMessageBox } from 'element-plus'
// 导入报修单接口：获取详情、修改状态、删除
import { getOrderDetailAdmin, updateOrderStatus, deleteOrder } from '@/api/order'

const router = useRouter()
const route = useRoute()

// 加载状态
const loading = ref(true)
// 报修单详情数据
const order = ref({})

// ==================== 枚举映射：把后端数字 → 中文 + 颜色 ====================
// 设备类型映射
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

// 优先级标签颜色
const priorityTypeMap = {
  '1': 'info',
  '2': 'warning',
  '3': 'danger'
}

// 优先级名称
const priorityNameMap = {
  '1': '普通',
  '2': '紧急',
  '3': '非常紧急'
}

// 控制问题描述展开/收起
let showFullDescription = ref(false)

// 状态标签颜色
const statusTypeMap = {
  '1': 'warning',
  '2': 'success',
  '3': 'info'
}

// 状态名称
const statusNameMap = {
  '1': '待处理',
  '2': '已完成',
  '3': '已取消'
}

// 根据优先级获取标签类型
const getPriorityType = (priority) => priorityTypeMap[priority] || 'info'
// 根据状态获取标签类型
const getStatusType = (status) => statusTypeMap[status] || 'info'

// ==================== 加载报修单详情 ====================
const loadOrderDetail = async () => {
  loading.value = true
  try {
    // 从路由参数中获取 id，请求详情接口
    const res = await getOrderDetailAdmin(route.params.id)
    order.value = res.data
  } catch (error) {
    console.error('加载失败:', error)
  } finally {
    loading.value = false
  }
}

// ==================== 标记为完成 ====================
const handleComplete = async () => {
  try {
    // 弹出确认框
    await ElMessageBox.confirm('确定要标记为已完成吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用接口，将状态改为 2（已完成）
    await updateOrderStatus(order.value.id, { status: 2 })
    ElMessage.success('操作成功')
    // 重新加载详情
    loadOrderDetail()
  } catch (error) {
    // 取消操作不报错
    if (error !== 'cancel') {
      console.error('操作失败:', error)
    }
  }
}

// ==================== 删除报修单 ====================
const handleDelete = async () => {
  try {
    // 确认删除
    await ElMessageBox.confirm('确定要删除此报修单吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    
    // 调用删除接口
    await deleteOrder(order.value.id)
    ElMessage.success('删除成功')
    // 删除成功后返回上一页
    router.back()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// ==================== 生命周期：页面加载时获取详情 ====================
onMounted(() => {
  loadOrderDetail()
})
</script>

<style scoped>
/* 问题描述：超出省略，点击展开 */
.description-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  cursor: pointer;
  position: relative;
}
 
.description-text.full-description {
  white-space: normal;
  overflow: visible;
  text-overflow: clip;
}
 
.toggle-text {
  color: #409EFF;
  font-size: 12px;
  margin-left: 5px;
}

.order-detail {
  max-width: 900px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.image-list {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.image-item {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  cursor: pointer;
}

.action-buttons {
  margin-top: 20px;
  text-align: center;
  display: flex;
  gap: 10px;
  justify-content: center;
}
</style>