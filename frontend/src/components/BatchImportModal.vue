<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useLiteratureStore } from '@/stores/literature'

const props = defineProps({
  visible: Boolean
})

const emit = defineEmits(['update:visible', 'success'])

const literatureStore = useLiteratureStore()

const form = reactive({
  files: [],
  apiKey: ''
})

const loading = ref(false)
const progress = ref(0)
const uploadStatus = ref('idle') // idle, uploading, completed, error
const eventSource = ref(null)

// 处理文件选择
const handleFileChange = (files) => {
  form.files = files.map(file => file.raw)
}

// 处理批量导入
const handleBatchImport = async () => {
  if (form.files.length === 0) {
    ElMessage.warning('请选择文献文件')
    return
  }

  if (form.files.length > 16) {
    ElMessage.warning('一次最多导入16个文件')
    return
  }

  if (!form.apiKey.trim()) {
    ElMessage.warning('请输入API密钥')
    return
  }

  loading.value = true
  progress.value = 0
  uploadStatus.value = 'uploading'

  try {
    // 创建SSE连接监听进度
    setupSSEConnection()

    await literatureStore.batchImportLiterature(form.files, form.apiKey, handleProgress)
    
    uploadStatus.value = 'completed'
    ElMessage.success('批量导入完成')
    emit('success')
    handleClose()
  } catch (error) {
    uploadStatus.value = 'error'
    // 错误信息已在store中处理
  } finally {
    if (eventSource.value) {
      eventSource.value.close()
    }
    loading.value = false
  }
}

// 设置SSE连接
const setupSSEConnection = () => {
  // 这里需要实现SSE连接来实时获取处理进度
  // 由于SSE需要后端支持，这里先使用模拟进度
  simulateProgress()
}

// 模拟进度（实际项目中应该通过SSE获取真实进度）
const simulateProgress = () => {
  let currentProgress = 0
  const interval = setInterval(() => {
    if (currentProgress < 100) {
      currentProgress += 5
      progress.value = currentProgress
    } else {
      clearInterval(interval)
    }
  }, 500)
}

// 处理进度更新
const handleProgress = (percent) => {
  progress.value = percent
}

// 关闭模态框
const handleClose = () => {
  form.files = []
  form.apiKey = ''
  progress.value = 0
  uploadStatus.value = 'idle'
  loading.value = false
  
  if (eventSource.value) {
    eventSource.value.close()
    eventSource.value = null
  }
  
  emit('update:visible', false)
}

// 文件上传前的验证
const beforeUpload = (file) => {
  const allowedTypes = ['.pdf', '.doc', '.docx', '.md', '.txt']
  const fileExtension = file.name.substring(file.name.lastIndexOf('.')).toLowerCase()
  
  if (!allowedTypes.includes(fileExtension)) {
    ElMessage.error('仅支持PDF、Word、Markdown文件')
    return false
  }

  if (file.size > 50 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过50MB')
    return false
  }

  return true
}

// 获取状态文本
const getStatusText = () => {
  switch (uploadStatus.value) {
    case 'uploading':
      return '处理中...'
    case 'completed':
      return '导入完成'
    case 'error':
      return '导入失败'
    default:
      return '等待开始'
  }
}
</script>

<template>
  <el-dialog
    :model-value="visible"
    title="批量导入文献"
    width="600px"
    :before-close="handleClose"
    destroy-on-close
  >
    <div class="batch-import-content">
      <!-- 文件选择区域 -->
      <div class="upload-section">
        <el-upload
          multiple
          :auto-upload="false"
          :before-upload="beforeUpload"
          :on-change="handleFileChange"
          :show-file-list="true"
          accept=".pdf,.doc,.docx,.md,.txt"
        >
          <el-button type="primary">选择多个文件</el-button>
          <template #tip>
            <div class="el-upload__tip">
              最多选择16个文件，支持PDF、Word、Markdown文件，每个文件最大50MB
            </div>
          </template>
        </el-upload>
      </div>

      <!-- API密钥输入 -->
      <div class="api-key-section">
        <el-input
          v-model="form.apiKey"
          type="password"
          placeholder="请输入Kimi AI API密钥"
          show-password
        />
        <div class="form-tip">
          前往 <a href="https://platform.moonshot.cn/" target="_blank">Moonshot平台</a> 获取API密钥
        </div>
      </div>

      <!-- 进度显示 -->
      <div v-if="uploadStatus !== 'idle'" class="progress-section">
        <div class="progress-info">
          <span class="status-text">{{ getStatusText() }}</span>
          <span class="progress-percent">{{ progress }}%</span>
        </div>
        <el-progress
          :percentage="progress"
          :status="
            uploadStatus === 'completed' ? 'success' : 
            uploadStatus === 'error' ? 'exception' : undefined
          "
          :stroke-width="16"
        />
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button
        type="primary"
        :loading="loading"
        :disabled="form.files.length === 0 || !form.apiKey"
        @click="handleBatchImport"
      >
        {{ loading ? '处理中...' : '开始批量导入' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<style lang="scss" scoped>
.batch-import-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-tip {
  font-size: 12px;
  color: $text-secondary;
  margin-top: 4px;

  a {
    color: $primary-color;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}

.progress-section {
  .progress-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;

    .status-text {
      font-size: 14px;
      color: $text-primary;
    }

    .progress-percent {
      font-size: 14px;
      font-weight: 600;
      color: $primary-color;
    }
  }
}
</style>