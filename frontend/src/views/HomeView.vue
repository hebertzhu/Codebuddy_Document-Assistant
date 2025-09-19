<script setup>
import { ref, onMounted, computed } from 'vue'
import { useLiteratureStore } from '@/stores/literature'
import { ElMessage, ElMessageBox } from 'element-plus'
import UploadModal from '@/components/UploadModal.vue'
import BatchImportModal from '@/components/BatchImportModal.vue'

const literatureStore = useLiteratureStore()

// 搜索条件
const searchForm = ref({
  category: '',
  description: '',
  readingGuide: '',
  tags: ''
})

// 模态框控制
const uploadModalVisible = ref(false)
const batchImportModalVisible = ref(false)

// 初始化加载数据
onMounted(() => {
  literatureStore.fetchLiteratureList()
})

// 处理分页变化
const handleCurrentChange = (page) => {
  literatureStore.fetchLiteratureList(page, literatureStore.pageSize)
}

// 处理搜索
const handleSearch = () => {
  literatureStore.updateFilters(searchForm.value)
}

// 重置搜索
const handleReset = () => {
  searchForm.value = {
    category: '',
    description: '',
    readingGuide: '',
    tags: ''
  }
  literatureStore.resetFilters()
}

// 处理上传成功
const handleUploadSuccess = () => {
  uploadModalVisible.value = false
  ElMessage.success('文献上传成功')
  literatureStore.fetchLiteratureList()
}

// 处理批量导入成功
const handleBatchImportSuccess = () => {
  batchImportModalVisible.value = false
  ElMessage.success('批量导入完成')
  literatureStore.fetchLiteratureList()
}

// 下载文献
const handleDownload = async (id, fileName) => {
  try {
    await literatureStore.downloadLiterature(id)
    ElMessage.success('下载成功')
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

// 查看详情
const handleViewDetail = (id) => {
  window.open(`/literature/${id}`, '_blank')
}

// 删除文献
const handleDelete = async (id, title) => {
  try {
    await ElMessageBox.confirm(`确定要删除文献"${title}"吗？`, '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // 这里需要实现删除API调用
    ElMessage.success('删除成功')
    literatureStore.fetchLiteratureList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}
</script>

<template>
  <div class="home-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="container">
        <h1>文献库</h1>
        <p class="subtitle">智能管理您的学术文献资源</p>
      </div>
    </div>

    <div class="container">
      <!-- 操作工具栏 -->
      <div class="action-bar">
        <el-button type="primary" @click="uploadModalVisible = true">
          <el-icon><Plus /></el-icon>导入文献
        </el-button>
        <el-button @click="batchImportModalVisible = true">
          <el-icon><Upload /></el-icon>批量导入
        </el-button>
      </div>

      <!-- 搜索筛选区域 -->
      <div class="search-card card">
        <div class="search-header">
          <h3>文献筛选</h3>
        </div>
        <el-form :model="searchForm" label-width="80px">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-form-item label="分类">
                <el-input v-model="searchForm.category" placeholder="输入分类关键词" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="描述">
                <el-input v-model="searchForm.description" placeholder="输入描述关键词" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="指南">
                <el-input v-model="searchForm.readingGuide" placeholder="输入指南关键词" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="标签">
                <el-input v-model="searchForm.tags" placeholder="输入标签关键词" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 文献列表 -->
      <div class="literature-list card">
        <div class="list-header">
          <h3>文献列表</h3>
          <span class="total-count">共 {{ literatureStore.totalCount }} 篇文献</span>
        </div>

        <el-table
          :data="literatureStore.literatureList"
          v-loading="literatureStore.loading"
          style="width: 100%"
        >
          <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
          <el-table-column prop="category" label="分类" width="120" />
          <el-table-column prop="author" label="作者" width="120" />
          <el-table-column prop="publishYear" label="年份" width="80" />
          <el-table-column prop="fileSizeReadable" label="大小" width="80" />
          <el-table-column prop="createTime" label="上传时间" width="150" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="handleDownload(row.id, row.originalFileName)">
                下载
              </el-button>
              <el-button size="small" type="primary" @click="handleViewDetail(row.id)">
                查看
              </el-button>
              <el-button size="small" type="danger" @click="handleDelete(row.id, row.title)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="literatureStore.currentPage"
            v-model:page-size="literatureStore.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="literatureStore.totalCount"
            layout="total, sizes, prev, pager, next, jumper"
            @current-change="handleCurrentChange"
            @size-change="(size) => literatureStore.fetchLiteratureList(1, size)"
          />
        </div>
      </div>
    </div>

    <!-- 上传模态框 -->
    <UploadModal
      v-model:visible="uploadModalVisible"
      @success="handleUploadSuccess"
    />

    <!-- 批量导入模态框 -->
    <BatchImportModal
      v-model:visible="batchImportModalVisible"
      @success="handleBatchImportSuccess"
    />
  </div>
</template>

<style scoped>
.home-view {
  min-height: 100vh;
  background-color: var(--bg-secondary);
}

.action-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 12px;
}

.search-card {
  margin-bottom: 20px;
}

.search-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.total-count {
  color: var(--text-secondary);
  font-size: 14px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-table .cell) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>