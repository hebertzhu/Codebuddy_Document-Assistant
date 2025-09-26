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
  file: null,
  apiKey: ''
})

const loading = ref(false)
const fileList = ref([])

// 处理文件选择
const handleFileChange = (file) => {
  form.file = file.raw
  fileList.value = [file]
}

// 处理上传
const handleUpload = async () => {
  if (!form.file) {
    ElMessage.warning('请选择文献文件')
    return
  }

  if (!form.apiKey.trim()) {
    ElMessage.warning('请输入API密钥')
    return
  }

  loading.value = true

  try {
    await literatureStore.uploadLiterature(form.file, form.apiKey)
    ElMessage.success('文献上传成功')
    emit('success')
    handleClose()
  } catch (error) {
    // 错误信息已在store中处理
  } finally {
    loading.value = false
  }
}

// 关闭模态框
const handleClose = () => {
  form.file = null
  form.apiKey = ''
  fileList.value = []
  loading.value = false
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
</script>

<template>
  <el-dialog
    :model-value="visible"
    title="导入文献"
    width="500px"
    :before-close="handleClose"
    destroy-on-close
  >
    <el-form :model="form" label-width="80px">
      <el-form-item label="文献文件" required>
        <el-upload
          v-model:file-list="fileList"
          :auto-upload="false"
          :before-upload="beforeUpload"
          :on-change="handleFileChange"
          :show-file-list="true"
          accept=".pdf,.doc,.docx,.md,.txt"
        >
          <el-button type="primary">选择文件</el-button>
          <template #tip>
            <div class="el-upload__tip">
              支持PDF、Word、Markdown文件，最大50MB
            </div>
          </template>
        </el-upload>
      </el-form-item>

      <el-form-item label="API密钥" required>
        <el-input
          v-model="form.apiKey"
          type="password"
          placeholder="请输入Kimi AI API密钥"
          show-password
        />
        <div class="form-tip">
          前往 <a href="https://platform.moonshot.cn/" target="_blank">Moonshot平台</a> 获取API密钥
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button
        type="primary"
        :loading="loading"
        :disabled="!form.file || !form.apiKey"
        @click="handleUpload"
      >
        {{ loading ? '处理中...' : '开始导入' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<style lang="scss" scoped>
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
</style>