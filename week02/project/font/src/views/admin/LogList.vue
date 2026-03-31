<template>
  <!-- 操作日志页面容器 -->
  <div class="log-list">
    <el-card>
      <!-- 卡片头部：标题 + 批量删除按钮 -->
      <template #header>
        <div class="card-header">
          <span>📝 操作日志</span>
          <!-- 批量删除按钮：未选择任何日志时禁用 -->
          <el-button 
            type="danger" 
            size="small"
            @click="handleBatchDelete"
            :disabled="selectedLogs.length === 0"
          >
            批量删除
          </el-button>
        </div>
      </template>
      
      <!-- 搜索条件表单：用户账号、操作结果、时间范围 -->
      <el-form :model="searchForm" :inline="true" class="search-form">
        <!-- 用户账号输入框 -->
        <el-form-item label="用户账号">
          <el-input 
            v-model="searchForm.userAccount" 
            placeholder="请输入账号" 
            clearable 
            style="width: 150px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        
        <!-- 操作结果下拉框：成功/失败 -->
        <el-form-item label="操作结果">
          <el-select 
            v-model="searchForm.result" 
            placeholder="全部" 
            clearable 
            style="width: 100px"
          >
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="2" />
          </el-select>
        </el-form-item>
        
        <!-- 时间范围选择器 -->
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 240px"
          />
        </el-form-item>
        
        <!-- 查询 + 重置按钮 -->
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 日志表格：支持多选、加载动画、表头样式 -->
      <el-table 
        :data="logList" 
        style="width: 100%" 
        v-loading="loading"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
        @selection-change="handleSelectionChange"
      >
        <!-- 多选列 -->
        <el-table-column type="selection" width="55" />
        <!-- 日志ID -->
        <el-table-column prop="id" label="ID" width="80" />
        
        <!-- 用户账号：以00开头显示黄色标签 -->
        <el-table-column prop="userAccount" label="用户账号" width="140">
          <template #default="{ row }">
            <el-tag :type="row.userAccount.startsWith('00') ? 'warning' : ''">
              {{ row.userAccount }}
            </el-tag>
          </template>
        </el-table-column>
        
        <!-- 操作内容 -->
        <el-table-column prop="operation" label="操作内容" min-width="180" />
        
        <!-- 请求接口：超出显示省略号 -->
        <el-table-column 
          prop="requestUri" 
          label="请求接口" 
          width="220"
          show-overflow-tooltip
        />
        
        <!-- 请求参数：超出显示省略号 -->
        <el-table-column 
          prop="requestParams" 
          label="请求参数" 
          min-width="260"
          show-overflow-tooltip
        />
        
        <!-- 操作结果：成功=绿色，失败=红色 -->
        <el-table-column prop="result" label="结果" width="90">
          <template #default="{ row }">
            <el-tag :type="row.result === '1' ? 'success' : 'danger'">
              {{ row.result === '1' ? '✓ 成功' : '✗ 失败' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <!-- IP地址 -->
        <el-table-column prop="ipAddress" label="IP 地址" width="140" />
        
        <!-- 操作时间：格式化显示 -->
        <el-table-column prop="createTime" label="操作时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 统计信息：总记录数、当前页、每页条数 -->
      <div class="statistics" v-if="logList.length > 0">
        <el-statistic title="总记录数" :value="total" />
        <el-statistic title="当前页" :value="pageNum" />
        <el-statistic title="每页条数" :value="pageSize" />
      </div>
      
      <!-- 分页组件 -->
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        class="pagination"
      />
    </el-card>
  </div>
</template>

<script setup>
// 导入Vue响应式API
import { ref, reactive, onMounted } from 'vue'
// 导入Element Plus提示组件
import { ElMessage, ElMessageBox } from 'element-plus'
// 导入图标
import { Search, Refresh } from '@element-plus/icons-vue'
// 导入日志接口：获取日志、删除日志
import { getOperationLogs, deleteLogs } from '@/api/log'

// ==================== 响应式数据 ====================
// 日志列表数据
const logList = ref([])
// 总记录数
const total = ref(0)
// 当前页码
const pageNum = ref(1)
// 每页显示条数
const pageSize = ref(10)
// 加载状态
const loading = ref(true)
// 选中的日志（用于批量删除）
const selectedLogs = ref([])

// 搜索表单数据（响应式对象）
const searchForm = reactive({
  userAccount: '',
  result: null,
  dateRange: []
})

// ==================== 工具函数 ====================
/**
 * 格式化时间为本地日期字符串
 */
const formatDateTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// ==================== 核心业务方法 ====================
/**
 * 加载操作日志列表
 * 携带分页 + 搜索条件请求后端接口
 */
const loadLogs = async () => {
  loading.value = true
  try {
    // 封装请求参数
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      userAccount: searchForm.userAccount || undefined,
      result: searchForm.result !== null ? searchForm.result : undefined,
      startTime: searchForm.dateRange?.[0] || undefined,
      endTime: searchForm.dateRange?.[1] || undefined
    }
    
    // 请求接口
    const res = await getOperationLogs(params)
    // 赋值日志列表
    logList.value = res.data.list
    // 赋值总条数
    total.value = res.data.total
  } catch (error) {
    console.error('加载日志失败:', error)
  } finally {
    // 关闭loading
    loading.value = false
  }
}

/**
 * 查询按钮：重置页码为1，重新加载数据
 */
const handleSearch = () => {
  pageNum.value = 1
  loadLogs()
}

/**
 * 重置按钮：清空搜索条件，重置页码，重新加载
 */
const handleReset = () => {
  searchForm.userAccount = ''
  searchForm.result = null
  searchForm.dateRange = []
  pageNum.value = 1
  loadLogs()
}

/**
 * 切换页码
 */
const handlePageChange = (page) => {
  pageNum.value = page
  loadLogs()
}

/**
 * 切换每页条数
 */
const handleSizeChange = (size) => {
  pageSize.value = size
  pageNum.value = 1
  loadLogs()
}

/**
 * 表格多选事件：记录选中的日志
 */
const handleSelectionChange = (selection) => {
  selectedLogs.value = selection
}

/**
 * 批量删除选中的日志
 */
const handleBatchDelete = async () => {
  // 未选择直接返回
  if (selectedLogs.value.length === 0) return
  
  try {
    // 确认删除
    await ElMessageBox.confirm('确定要删除选中的操作日志吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    
    // 提取选中日志的ID数组
    const logIds = selectedLogs.value.map(log => log.id)
    // 调用删除接口
    await deleteLogs(logIds)
    
    // 提示成功并刷新列表
    ElMessage.success('批量删除成功')
    selectedLogs.value = []
    loadLogs()
  } catch (error) {
    // 点击取消不报错
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// ==================== 生命周期 ====================
// 页面挂载完成后立即加载日志数据
onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.log-list {
  max-width: 1600px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.search-form {
  margin-bottom: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 4px;
}

.statistics {
  display: flex;
  gap: 30px;
  margin-top: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa !important;
}

:deep(.el-tag--success) {
  background-color: #f0f9eb;
}

:deep(.el-tag--danger) {
  background-color: #fef0f0;
}
</style>