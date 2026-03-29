<template>
  <!-- 报修单高级查询页面容器 -->
  <div class="order-query">
    <el-card>
      <!-- 卡片头部标题 -->
      <template #header>
        <div class="card-header">
          <span>🔍 报修单查询</span>
        </div>
      </template>
      
      <!-- 高级搜索表单：支持多条件组合查询 -->
      <el-form :model="searchForm" :inline="true" class="search-form">
        <!-- 报修单号 -->
        <el-form-item label="报修单号">
          <el-input v-model="searchForm.id" placeholder="请输入单号" clearable style="width: 150px" />
        </el-form-item>
                
        <!-- 楼栋筛选 -->
        <el-form-item label="楼栋">
          <el-select v-model="searchForm.dormBuilding" placeholder="全部" clearable style="width: 120px">
            <el-option label="A 栋" value="A 栋" />
            <el-option label="B 栋" value="B 栋" />
            <el-option label="C 栋" value="C 栋" />
            <el-option label="D 栋" value="D 栋" />
          </el-select>
        </el-form-item>
        
        <!-- 房间号 -->
        <el-form-item label="房间号">
          <el-input v-model="searchForm.dormRoom" placeholder="请输入房间" clearable style="width: 100px" />
        </el-form-item>
        
        <!-- 设备类型筛选：下拉选择，后端用数字表示 -->
        <el-form-item label="设备类型">
          <el-select v-model="searchForm.deviceType" placeholder="全部" clearable style="width: 120px">
            <el-option label="水龙头" value="1" />
            <el-option label="马桶" value="2" />
            <el-option label="电灯" value="3" />
            <el-option label="窗户" value="4" />
            <el-option label="门" value="5" />
            <el-option label="床" value="6" />
            <el-option label="水槽" value="7" />
            <el-option label="电表" value="8" />
            <el-option label="水表" value="9" />
          </el-select>
        </el-form-item>
        
        <!-- 状态筛选：待维修/已完成/已取消 -->
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 100px">
            <el-option label="待维修" value="1" />
            <el-option label="已完成" value="2" />
            <el-option label="已取消" value="3" />
          </el-select>
        </el-form-item>
        
        <!-- 优先级筛选 -->
        <el-form-item label="优先级">
          <el-select v-model="searchForm.priority" placeholder="全部" clearable style="width: 100px">
            <el-option label="普通" value="1" />
            <el-option label="紧急" value="2" />
            <el-option label="非常紧急" value="3" />
          </el-select>
        </el-form-item>
        
        <!-- 时间范围筛选：创建时间区间 -->
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        
        <!-- 查询 + 重置按钮 -->
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 查询结果表格：展示符合条件的报修单 -->
      <el-table :data="orderList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="单号" width="80" />
        <el-table-column prop="dormBuilding" label="楼栋" width="80" />
        <el-table-column prop="dormRoom" label="房间" width="80" />
        
        <!-- 设备类型：数字转中文显示 -->
        <el-table-column prop="deviceType" label="设备类型" width="100">
          <template #default="{ row }">
            {{ getDeviceTypeName(row.deviceType) }}
          </template>
        </el-table-column>
        
        <!-- 状态：带颜色标签显示 -->
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <!-- 优先级：数字转中文 -->
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="{ row }">
            {{ getPriorityName(row.priority) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="创建时间" width="180" />
        
        <!-- 操作列：查看详情、标记完成 -->
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewDetail(row.id)">详情</el-button>
            <el-button v-if="row.status === '1'" type="success" size="small" @click="handleComplete(row.id)">完成</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页组件：支持切换页码、每页条数 -->
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup>
// 导入 Vue 响应式 API
import { ref, reactive, onMounted } from 'vue'
// 导入路由，用于跳转到详情页
import { useRouter } from 'vue-router'
// 导入消息提示
import { ElMessage } from 'element-plus'
// 导入接口：查询报修单、修改报修单状态
import { queryOrders, updateOrderStatus } from '@/api/order'

const router = useRouter()

// ==================== 分页与列表数据 ====================
// 查询结果列表
const orderList = ref([])
// 总记录数
const total = ref(0)
// 当前页码
const pageNum = ref(1)
// 每页条数
const pageSize = ref(10)
// 加载状态
const loading = ref(true)

// ==================== 搜索表单数据（全部初始化为空字符串）====================
const searchForm = reactive({
  id: '',                // 报修单号
  studentAccount: '',    // 学生账号
  dormBuilding: '',      // 楼栋
  dormRoom: '',          // 房间号
  deviceType: '',        // 设备类型
  status: '',            // 状态
  priority: '',          // 优先级
  dateRange: []          // 时间范围
})

// ==================== 枚举映射：后端数字 → 前端中文 / 颜色 ====================
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

// 工具方法：获取中文名称 / 标签类型
const getDeviceTypeName = (type) => deviceTypeMap[type] || '未知'
const getStatusName = (status) => statusMap[status]?.name || '未知'
const getStatusType = (status) => statusMap[status]?.type || 'info'
const getPriorityName = (priority) => priorityMap[priority] || '未知'

// ==================== 核心：加载查询数据 ====================
const loadOrders = async () => {
  loading.value = true
  try {
    // 构造请求参数：空值不传
    const data = {
      id: searchForm.id || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      studentAccount: searchForm.studentAccount || undefined,
      dormBuilding: searchForm.dormBuilding || undefined,
      dormRoom: searchForm.dormRoom || undefined,
      deviceType: searchForm.deviceType || undefined,
      status: searchForm.status || undefined,
      priority: searchForm.priority || undefined,
      startTime: searchForm.dateRange?.[0] || undefined,
      endTime: searchForm.dateRange?.[1] || undefined
    }

    // 调用后端查询接口
    const res = await queryOrders(data)
    // 赋值列表数据
    orderList.value = res.data?.list || []
    // 赋值总条数
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询失败')
  } finally {
    // 关闭加载
    loading.value = false
  }
}

// ==================== 查询按钮：重置页码为1，重新查询 ====================
const handleSearch = () => {
  pageNum.value = 1
  loadOrders()
}

// ==================== 重置按钮：清空所有条件，重新查询 ====================
const handleReset = () => {
  searchForm.id = ''
  searchForm.studentAccount = ''
  searchForm.dormBuilding = ''
  searchForm.dormRoom = ''
  searchForm.deviceType = ''
  searchForm.status = ''
  searchForm.priority = ''
  searchForm.dateRange = []
  handleSearch()
}

// ==================== 分页事件 ====================
// 每页条数改变
const handleSizeChange = () => loadOrders()
// 页码改变
const handlePageChange = () => loadOrders()

// ==================== 跳转到报修单详情页 ====================
const handleViewDetail = (id) => router.push(`/admin/order/${id}`)

// ==================== 标记为已完成 ====================
const handleComplete = async (id) => {
  try {
    // 调用接口修改状态为 2（已完成）
    await updateOrderStatus(id, { status: 2 })
    ElMessage.success('操作成功')
    // 刷新列表
    loadOrders()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

// ==================== 页面加载时自动查询 ====================
onMounted(() => loadOrders())
</script>

<style scoped>
.order-query { max-width: 1400px; margin: 0 auto; }
.card-header { font-size: 18px; font-weight: bold; }
.search-form { margin-bottom: 20px; }
</style>