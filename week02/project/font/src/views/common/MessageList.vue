<template>
  <!-- 消息中心页面最外层容器 -->
  <div class="message-list">
    <el-card>
      <!-- 卡片头部：标题 + 功能按钮区 -->
      <template #header>
        <div class="card-header">
          <!-- 左侧：返回按钮 + 标题 -->
          <div class="header-left">
            <el-button type="primary" size="small" @click="handleBack">
              <el-icon><ArrowLeft /></el-icon>
              返回首页
            </el-button>
            <span class="title">📢 消息中心</span>
          </div>

          <!-- 右侧：批量操作按钮 -->
          <div class="header-actions">
            <!-- 全部已读：未读数量为0时禁用 -->
            <el-button type="success" size="small" @click="handleMarkAllRead" :disabled="unreadCount === 0">
              <el-icon><Checked /></el-icon>
              全部已读
            </el-button>
            <!-- 批量已读：未选择时禁用 -->
            <el-button type="primary" size="small" @click="handleBatchMarkRead" :disabled="selectedIds.length === 0">
              <el-icon><Checked /></el-icon>
              批量已读 ({{ selectedIds.length }})
            </el-button>
            <!-- 批量删除：未选择时禁用 -->
            <el-button type="danger" size="small" @click="handleBatchDelete" :disabled="selectedIds.length === 0">
              <el-icon><Delete /></el-icon>
              批量删除 ({{ selectedIds.length }})
            </el-button>
            <!-- 手动刷新 -->
            <el-button @click="loadMessages">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 统计信息卡片：总消息、未读、今日、已读 -->
      <el-descriptions :column="4" border class="stats">
        <el-descriptions-item label="总消息数">
          <el-tag type="info">{{ stats.totalCount }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="未读消息">
          <el-tag type="danger" v-if="stats.unreadCount > 0">{{ stats.unreadCount }}</el-tag>
          <el-tag v-else>0</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="今日消息">
          <el-tag type="warning">{{ stats.todayCount }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="已读消息">
          <el-tag type="success">{{ stats.totalCount - stats.unreadCount }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      
      <!-- 筛选条件：消息类型、阅读状态 -->
      <el-form :inline="true" class="filter-form">
        <el-form-item label="消息类型">
          <el-select v-model="filterForm.type" placeholder="全部" clearable style="width: 120px">
            <el-option label="系统消息" value="1" />
            <el-option label="报修通知" value="2" />
            <el-option label="系统公告" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="阅读状态">
          <el-select v-model="filterForm.isRead" placeholder="全部" clearable style="width: 100px">
            <el-option label="未读" :value="2" />
            <el-option label="已读" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">筛选</el-button>
          <el-button @click="handleResetFilter">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 消息表格：支持多选、加载动画、未读高亮 -->
      <el-table 
        :data="messageList" 
        style="width: 100%" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
        :row-class-name="getRowClassName"
      >
        <!-- 复选框列 -->
        <el-table-column type="selection" width="50" />
        
        <el-table-column prop="id" label="ID" width="70" />
        
        <!-- 消息类型：带颜色标签 -->
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)">
              {{ getTypeName(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <!-- 标题：未读加粗高亮 -->
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <span :class="{ 'unread-title': row.isRead === '0' }">
              {{ row.title }}
            </span>
          </template>
        </el-table-column>
        
        <!-- 内容：超出自动省略 -->
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
        
        <!-- 阅读状态：已读=绿色，未读=红色 -->
        <el-table-column prop="isRead" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isRead === '1' ? 'success' : 'danger'" size="small">
              {{ row.isRead === '1' ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <!-- 发送时间：格式化显示 -->
        <el-table-column prop="createTime" label="时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        
        <!-- 操作列：标为已读 + 删除 -->
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="success" 
              size="small" 
              @click="handleMarkRead(row.id)"
              :disabled="row.isRead === '1'"
            >
              标为已读
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
      
      <!-- 分页组件 -->
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        class="pagination"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Checked, Delete, Refresh, ArrowLeft } from '@element-plus/icons-vue'

// 消息相关接口
import { 
  getMessages, 
  getUnreadCount, 
  getMessageStats,
  markAsRead, 
  markAllAsRead, 
  deleteMessage,
  deleteMessages,
  markBatchAsRead
} from '@/api/message'

// 获取用户信息（判断角色：管理员/学生）
import { getUserInfo } from '../../utils/auth'

const router = useRouter()

// ==================== 表格与分页数据 ====================
const messageList = ref([])    // 消息列表
const total = ref(0)           // 总条数
const pageNum = ref(1)         // 当前页
const pageSize = ref(10)       // 每页条数
const loading = ref(true)      // 加载状态
const unreadCount = ref(0)     // 未读消息总数
const selectedIds = ref([])    // 批量选择的消息ID

// ==================== 统计信息 ====================
const stats = ref({
  totalCount: 0,    // 总消息数
  unreadCount: 0,   // 未读数
  todayCount: 0     // 今日消息数
})

// ==================== 筛选条件 ====================
const filterForm = reactive({
  type: '',       // 消息类型
  isRead: null    // 阅读状态
})

// ==================== 工具函数 ====================
// 格式化时间
const formatDateTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 消息类型 数字 → 中文
const getTypeName = (type) => {
  const typeMap = {
    '1': '系统消息',
    '2': '报修通知',
    '3': '系统公告'
  }
  return typeMap[type] || '未知'
}

// 消息类型 → 标签颜色
const getTypeTag = (type) => {
  const tagMap = {
    '1': 'info',
    '2': 'warning',
    '3': 'success'
  }
  return tagMap[type] || 'info'
}

// 未读消息行高亮
const getRowClassName = ({ row }) => {
  return row.isRead === '0' ? 'unread-row' : ''
}

// ==================== 核心：加载消息列表 ====================
const loadMessages = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      type: filterForm.type || undefined,
      isRead: filterForm.isRead !== null ? filterForm.isRead : undefined
    }
    
    const res = await getMessages(params)
    messageList.value = res.data.list
    total.value = res.data.total
    
    // 同时加载统计数据
    await loadStats()
  } catch (error) {
    console.error('加载消息失败:', error)
    ElMessage.error('加载消息失败')
  } finally {
    loading.value = false
  }
}

// 加载统计数据
const loadStats = async () => {
  try {
    const [unreadRes, statsRes] = await Promise.all([
      getUnreadCount(),
      getMessageStats()
    ])
    unreadCount.value = unreadRes.data
    stats.value = statsRes.data
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

// ==================== 筛选与重置 ====================
const handleFilter = () => {
  pageNum.value = 1
  loadMessages()
}

const handleResetFilter = () => {
  filterForm.type = ''
  filterForm.isRead = null
  pageNum.value = 1
  loadMessages()
}

// ==================== 分页事件 ====================
const handlePageChange = (page) => {
  pageNum.value = page
  loadMessages()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  pageNum.value = 1
  loadMessages()
}

// ==================== 表格多选事件 ====================
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// ==================== 单条操作 ====================
// 标为已读
const handleMarkRead = async (id) => {
  try {
    await markAsRead(id)
    ElMessage.success('标记成功')
    loadMessages()
  } catch (error) {
    console.error('标记失败:', error)
  }
}

// 删除单条
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除此消息吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteMessage(id)
    ElMessage.success('删除成功')
    loadMessages()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// ==================== 批量操作 ====================
// 全部已读
const handleMarkAllRead = async () => {
  try {
    await ElMessageBox.confirm('确定要标记全部消息为已读吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    await markAllAsRead()
    ElMessage.success('全部标记成功')
    loadMessages()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('标记失败:', error)
    }
  }
}

// 批量标记已读
const handleBatchMarkRead = async () => {
  try {
    await markBatchAsRead(selectedIds.value)
    ElMessage.success('批量标记成功')
    selectedIds.value = []
    loadMessages()
  } catch (error) {
    console.error('标记失败:', error)
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条消息吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    
    await deleteMessages(selectedIds.value)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    loadMessages()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// ==================== 返回首页（根据角色跳转）====================
const handleBack = () => {
  if(getUserInfo().role === '2'){
    router.push('/admin/Dashboard')
  }else{
    router.push('/student/Dashboard')
  }
}

// ==================== 生命周期：自动刷新 ====================
let refreshTimer = null

onMounted(() => {
  loadMessages()
  // 每30秒自动刷新消息
  refreshTimer = setInterval(loadMessages, 30000)
})

onUnmounted(() => {
  // 页面销毁时清除定时器
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}
.title {
  font-size: 18px;
  font-weight: bold;
}
.message-list {
  max-width: 1400px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.stats {
  margin-bottom: 20px;
}

.filter-form {
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 未读消息行高亮 */
:deep(.unread-row) {
  background-color: #f0f9eb !important;
}

/* 未读标题加粗 */
.unread-title {
  font-weight: bold;
  color: #409EFF;
}

/* 表格行 hover 效果 */
:deep(.el-table__row:hover) {
  background-color: #f5f7fa !important;
}
</style>