<!-- src/views/student/OrderDetail.vue -->
<template>
  <!-- 报修单详情页面容器 -->
  <div class="order-detail">
    <el-card>
      <!-- 头部标题 + 返回按钮 -->
      <template #header>
        <div class="card-header">
          <span>📋 报修单详情</span>
          <el-button @click="router.back()">返回</el-button>
        </div>
      </template>
      
      <!-- 详情展示表格（带加载动画） -->
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
            {{ priorityTextMap[order.priority] || '未知' }}
          </el-tag>
        </el-descriptions-item>
        
        <!-- 状态：带颜色标签 -->
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(order.status)">
            {{ statusTextMap[order.status] || '未知' }}
          </el-tag>
        </el-descriptions-item>
        
        <el-descriptions-item label="创建时间">{{ order.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ order.updateTime }}</el-descriptions-item>
        
        <!-- 问题描述：支持展开/收起 -->
        <el-descriptions-item label="问题描述" :span="2">
          <div 
            :class="['description-text', { 'full-description': showFullDescription }]"
            @click="showFullDescription = !showFullDescription"
          >
            {{ order.description }}
            <span class="toggle-text">{{ showFullDescription ? ' (收起)' : ' (展开)' }}</span>
          </div>
        </el-descriptions-item>
        
        <!-- 上传图片列表：支持预览 -->
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
      
      <!-- 操作按钮：只有待维修状态才显示 -->
      <div class="action-buttons" v-if="order.status === '1'">
        <el-button type="danger" @click="handleCancel">取消报修</el-button>
      </div>

      <div class="action-buttons" v-if="order.status === '1'">
        <el-button type="primary" @click="toEdit">修改报修单</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

// 报修单接口：详情、取消
import { getOrderDetail, cancelOrder } from '@/api/order'

const router = useRouter()
const route = useRoute()

// 加载状态
const loading = ref(true)
// 报修单详情数据
const order = ref({})
// 控制问题描述展开/收起
const showFullDescription = ref(false)

// ==================== 枚举映射：后端数字 → 前端显示 ====================
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

// 优先级：颜色 + 文字分开管理
const priorityColorMap = {
  '1': 'info',
  '2': 'warning',
  '3': 'danger'
}
const priorityTextMap = {
  '1': '普通',
  '2': '紧急',
  '3': '非常紧急'
}

// 状态：颜色 + 文字分开管理
const statusColorMap = {
  '1': 'warning',
  '2': 'success',
  '3': 'info'
}
const statusTextMap = {
  '1': '待维修',
  '2': '已完成',
  '3': '已取消'
}

// 跳转到修改页面
const toEdit = () => {
  router.push(`/student/edit-order/${order.value.id}`)
}

// 获取标签类型（确保返回合法的 Element UI 颜色）
const getPriorityType = (priority) => priorityColorMap[priority] || 'info'
const getStatusType = (status) => statusColorMap[status] || 'info'

// ==================== 加载报修单详情 ====================
const loadOrderDetail = async () => {
  loading.value = true
  try {
    const res = await getOrderDetail(route.params.id)
    order.value = res.data
  } catch (error) {
    console.error('加载失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// ==================== 取消报修单 ====================
const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定要取消此报修单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await cancelOrder(order.value.id)
    ElMessage.success('取消成功')
    loadOrderDetail() // 重新加载详情
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消失败:', error)
    }
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadOrderDetail()
})
</script>

<style scoped>
/* 问题描述：省略号 / 展开 */
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
}
</style>