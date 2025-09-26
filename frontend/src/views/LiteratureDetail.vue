<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useLiteratureStore } from '@/stores/literature'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import PageHeader from '@/components/PageHeader.vue'

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
    <PageHeader :title="literature?.title || '文献详情'" />

    <div class="container" v-loading="loading">
      <div v-if="literature" class="detail-layout">
        <!-- 左侧内容 -->
        <div class="main-content">
          <!-- 文献描述 -->
          <el-card v-if="literature.description" class="description-card">
            <template #header>
              <h3>文献描述</h3>
            </template>
            <div class="description-content">
              {{ literature.description }}
            </div>
          </el-card>

          <!-- 阅读指南 -->
          <el-card v-if="literature.readingGuide" class="guide-card">
            <template #header>
              <h3>阅读指南</h3>
            </template>
            <div class="guide-content" v-html="renderMarkdown(literature.readingGuide)"></div>
          </el-card>
        </div>

        <!-- 右侧信息栏 -->
        <div class="sidebar">
          <el-card class="info-card">
            <template #header>
              <h3>基本信息</h3>
            </template>
            <div class="info-grid">
              <div class="info-item">
                <span class="label">文件名:</span>
                <span class="value">{{ literature.originalFileName }}</span>
              </div>
              <div class="info-item">
                <span class="label">文件类型:</span>
                <span class="value">{{ literature.fileType }}</span>
              </div>
              <div class="info-item">
                <span class="label">文件大小:</span>
                <span class="value">{{ formatFileSize(literature.fileSize) }}</span>
              </div>
              <div class="info-item">
                <span class="label">分类:</span>
                <span class="value">{{ literature.category || '未分类' }}</span>
              </div>
              <div class="info-item">
                <span class="label">作者:</span>
                <span class="value">{{ literature.author || '未知' }}</span>
              </div>
              <div class="info-item">
                <span class="label">出版年份:</span>
                <span class="value">{{ literature.publishYear || '未知' }}</span>
              </div>
              <div class="info-item">
                <span class="label">标签:</span>
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
                <span class="label">上传时间:</span>
                <span class="value">{{ formatTime(literature.createTime) }}</span>
              </div>
              <div class="info-item">
                <span class="label">更新时间:</span>
                <span class="value">{{ formatTime(literature.updateTime) }}</span>
              </div>
            </div>
            <el-button type="primary" @click="handleDownload" class="download-btn">
              <el-icon><Download /></el-icon>下载原文
            </el-button>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>

.literature-detail {
  min-height: 100vh;
  background-color: $bg-secondary;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
  }

  .detail-layout {
    display: grid;
    grid-template-columns: 1fr 350px;
    gap: 20px;
  }

  .main-content {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .description-card, .guide-card, .info-card {
    .description-content, .guide-content {
      line-height: 1.6;
    }
  }

  .info-grid {
    .info-item {
      display: flex;
      margin-bottom: 12px;

      .label {
        width: 80px;
        color: $text-secondary;
        flex-shrink: 0;
      }

      .value {
        color: $text-primary;
        word-break: break-all;
      }
    }
  }

  .download-btn {
    width: 100%;
    margin-top: 20px;
  }
}
</style>