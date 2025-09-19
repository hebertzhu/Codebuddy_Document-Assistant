<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useLiteratureStore } from '@/stores/literature'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'

const route = useRoute()
const literatureStore = useLiteratureStore()

const literature = ref(null)
const loading = ref(false)

// 获取文献详情
const fetchLiteratureDetail = async () => {
  loading.value = true
  try {
    const id = route.params.id
    literature.value = await literatureStore.getLiteratureDetail(id)
  } catch (error) {
    ElMessage.error('获取文献详情失败')
  } finally {
    loading.value = false
  }
}

// 下载文献
const handleDownload = async () => {
  try {
    await literatureStore.downloadLiterature(literature.value.id)
    ElMessage.success('下载成功')
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

// 渲染Markdown内容
const renderMarkdown = (content) => {
  if (!content) return ''
  return marked(content)
}

// 格式化文件大小
const formatFileSize = (size) => {
  if (!size) return ''
  const units = ['B', 'KB', 'MB', 'GB']
  let index = 0
  let formattedSize = size
  
  while (formattedSize >= 1024 && index < units.length - 1) {
    formattedSize /= 1024
    index++
  }
  
  return `${formattedSize.toFixed(1)} ${units[index]}`
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  fetchLiteratureDetail()
})
</script>

<template>
  <div class="literature-detail">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="container">
        <h1>文献详情</h1>
        <p class="subtitle">{{ literature?.title }}</p>
      </div>
    </div>

    <div class="container" v-loading="loading">
      <div v-if="literature" class="detail-content">
        <!-- 基本信息卡片 -->
        <div class="info-card card">
          <div class="info-header">
            <h3>基本信息</h3>
            <el-button type="primary" @click="handleDownload">
              <el-icon><Download /></el-icon>下载原文
            </el-button>
          </div>
          
          <div class="info-grid">
            <div class="info-item">
              <span class="label">标题：</span>
              <span class="value">{{ literature.title }}</span>
            </div>
            <div class="info-item">
              <span class="label">文件名：</span>
              <span class="value">{{ literature.originalFileName }}</span>
            </div>
            <div class="info-item">
              <span class="label">文件类型：</span>
              <span class="value">{{ literature.fileType }}</span>
            </div>
            <div class="info-item">
              <span class="label">文件大小：</span>
              <span class="value">{{ formatFileSize(literature.fileSize) }}</span>
            </div>
            <div class="info-item">
              <span class="label">分类：</span>
              <span class="value">{{ literature.category || '未分类' }}</span>
            </div>
            <div class="info-item">
              <span class="label">作者：</span>
              <span class="value">{{ literature.author || '未知' }}</span>
            </div>
            <div class="info-item">
              <span class="label">出版年份：</span>
              <span class="value">{{ literature.publishYear || '未知' }}</span>
            </div>
            <div class="info-item">
              <span class="label">标签：</span>
              <span class="value">
                <el-tag
                  v-for="tag in literature.tags?.split(',')"
                  :key="tag"
                  size="small"
                  style="margin-right: 4px;"
                >
                  {{ tag.trim() }}
                </el-tag>
              </span>
            </div>
            <div class="info-item">
              <span class="label">上传时间：</span>
              <span class="value">{{ formatTime(literature.createTime) }}</span>
            </div>
            <div class="info-item">
              <span class="label">更新时间：</span>
              <span class="value">{{ formatTime(literature.updateTime) }}</span>
            </div>
          </div>
        </div>

        <!-- 文献描述 -->
        <div v-if="literature.description" class="description-card card">
          <h3>文献描述</h3>
          <div class="description-content">
            {{ literature.description }}
          </div>
        </div>

        <!-- 阅读指南 -->
        <div v-if="literature.readingGuide" class="guide-card card">
          <h3>AI阅读指南</h3>
          <div class="guide-content" v-html="renderMarkdown(literature.readingGuide)"></div>
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-card card">
          <el-empty description="暂无阅读指南" />
        </div>
      </div>

      <!-- 加载失败 -->
      <div v-else-if="!loading" class="error-card card">
        <el-empty description="文献不存在或加载失败" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.literature-detail {
  min-height: 100vh;
  background-color: var(--bg-secondary);
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: flex-start;
}

.label {
  font-weight: 600;
  color: var(--text-primary);
  min-width: 80px;
}

.value {
  color: var(--text-secondary);
  word-break: break-all;
}

.description-content,
.guide-content {
  line-height: 1.8;
  color: var(--text-secondary);
}

.guide-content {
  :deep(h1) {
    font-size: 1.8rem;
    color: var(--primary-color);
    margin: 1.5rem 0 1rem;
  }

  :deep(h2) {
    font-size: 1.5rem;
    color: var(--primary-color);
    margin: 1.2rem 0 0.8rem;
  }

  :deep(h3) {
    font-size: 1.2rem;
    color: var(--primary-color);
    margin: 1rem 0 0.6rem;
  }

  :deep(p) {
    margin: 0.5rem 0;
  }

  :deep(ul), :deep(ol) {
    margin: 0.5rem 0;
    padding-left: 1.5rem;
  }

  :deep(li) {
    margin: 0.25rem 0;
  }

  :deep(strong) {
    color: var(--primary-color);
  }
}

.empty-card,
.error-card {
  text-align: center;
  padding: 40px;
}
</style>