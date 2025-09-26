<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Upload } from '@element-plus/icons-vue';
import { useLiteratureStore } from '@/stores/literature';

import LiteratureCard from '@/components/LiteratureCard.vue';
import UploadModal from '@/components/UploadModal.vue';
import BatchImportModal from '@/components/BatchImportModal.vue';

const literatureStore = useLiteratureStore();

// 搜索表单
const searchForm = reactive({
  category: '',
  description: '',
  readingGuide: '',
  tags: '',
});

// 模态框可见性
const uploadModalVisible = ref(false);
const batchImportModalVisible = ref(false);

// 组件挂载后加载文献列表
onMounted(() => {
  literatureStore.fetchLiteratureList();
});

// 处理搜索
const handleSearch = () => {
  literatureStore.setSearchParams(searchForm);
  literatureStore.fetchLiteratureList(1); // 搜索后回到第一页
};

// 处理重置
const handleReset = () => {
  searchForm.category = '';
  searchForm.description = '';
  searchForm.readingGuide = '';
  searchForm.tags = '';
  literatureStore.setSearchParams({});
  literatureStore.fetchLiteratureList(1);
};

// 处理分页
const handleCurrentChange = (page) => {
  literatureStore.fetchLiteratureList(page);
};

// 处理上传成功
const handleUploadSuccess = () => {
  uploadModalVisible.value = false;
  ElMessage.success('文献上传成功');
  literatureStore.fetchLiteratureList(); // 刷新列表
};

// 处理批量导入成功
const handleBatchImportSuccess = () => {
  batchImportModalVisible.value = false;
  ElMessage.success('批量导入任务已创建');
  literatureStore.fetchLiteratureList(); // 刷新列表
};

// 处理下载
const handleDownload = (literature) => {
  // 实现下载逻辑
  console.log('下载文献:', literature);
};

// 处理查看详情
const handleViewDetail = (literature) => {
  // 实现查看详情逻辑
  console.log('查看详情:', literature);
};

// 处理删除
const handleDelete = (literature) => {
  ElMessageBox.confirm(`确定删除文献 "${literature.title}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      literatureStore.deleteLiterature(literature.id);
    })
    .catch(() => {
      // 取消删除
    });
};
</script>

<template>
  <div class="home-view">
    <div class="container">
      <div class="action-bar">
        <el-button type="primary" @click="uploadModalVisible = true">
          <el-icon><Plus /></el-icon>导入文献
        </el-button>
        <el-button @click="batchImportModalVisible = true">
          <el-icon><Upload /></el-icon>批量导入
        </el-button>
      </div>

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

      <div class="literature-list">
        <div class="list-header">
          <h3>文献列表</h3>
          <span class="total-count">共 {{ literatureStore.totalCount }} 篇文献</span>
        </div>

        <div v-if="literatureStore.loading" v-loading="literatureStore.loading" class="loading-spinner">
        </div>

        <div v-else-if="literatureStore.literatureList.length > 0" class="card-grid">
          <LiteratureCard
            v-for="item in literatureStore.literatureList"
            :key="item.id"
            :literature="item"
            @download="handleDownload"
            @view="handleViewDetail"
            @delete="handleDelete"
          />
        </div>

        <div v-else class="empty-state">
          <p>暂无文献</p>
        </div>

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

    <UploadModal
      v-model:visible="uploadModalVisible"
      @success="handleUploadSuccess"
    />

    <BatchImportModal
      v-model:visible="batchImportModalVisible"
      @success="handleBatchImportSuccess"
    />
  </div>
</template>

<style lang="scss" scoped>
.home-view {
  padding: 24px;
  background-color: $bg-primary;
  color: $text-primary;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.action-bar {
  margin-bottom: 24px;
  display: flex;
  gap: 12px;
}

.card {
  background-color: $bg-secondary;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.search-card {
  margin-bottom: 24px;

  .search-header {
    margin-bottom: 20px;
    h3 {
      font-size: 18px;
      font-weight: 600;
      color: $text-primary;
    }
  }
}

.literature-list {
  .list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h3 {
      font-size: 18px;
      font-weight: 600;
      color: $text-primary;
    }

    .total-count {
      font-size: 14px;
      color: $text-secondary;
    }
  }

  .loading-spinner {
    text-align: center;
    padding: 40px;
  }

  .card-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;
  }

  .empty-state {
    text-align: center;
    padding: 40px;
    color: $text-secondary;
  }

  .pagination {
    margin-top: 24px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>