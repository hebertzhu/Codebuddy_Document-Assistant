<template>
  <div class="settings-view">
    <div class="card">
      <h2>设置</h2>

      <div class="section">
        <h3>API 密钥管理</h3>
        <p class="desc">集中管理第三方 AI 服务的密钥。密钥仅保存在本机浏览器的本地存储，建议生产环境改为服务端安全存储。</p>

        <el-form :model="form" label-width="120px" class="api-form">
          <el-form-item label="服务提供商">
            <el-select v-model="form.provider" placeholder="请选择提供商" style="width: 280px">
              <el-option v-for="opt in providerOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="API 密钥">
            <el-input v-model="form.key" placeholder="粘贴你的密钥" show-password style="max-width: 480px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :disabled="!form.provider || !form.key" @click="saveKey">保存/更新密钥</el-button>
            <el-button @click="resetForm">清空</el-button>
          </el-form-item>
        </el-form>

        <div class="list-header">
          <h4>已保存的密钥</h4>
          <el-tag type="info">当前激活：{{ settings.activeAiProvider || '未选择' }}</el-tag>
        </div>

        <el-table :data="rows" v-if="rows.length" class="keys-table">
          <el-table-column prop="provider" label="提供商" width="160" />
          <el-table-column label="密钥">
            <template #default="{ row }">
              <span class="masked">{{ maskKey(row.key) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="row.provider === settings.activeAiProvider ? 'success' : 'info'">
                {{ row.provider === settings.activeAiProvider ? '已激活' : '未激活' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="260">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="setActive(row.provider)">设为激活</el-button>
              <el-button size="small" @click="prefill(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="removeKey(row.provider)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-else class="empty-tip">尚未保存任何密钥</div>
      </div>
    </div>
  </div>
  
</template>

<script setup>
import { reactive, computed } from 'vue'
import { useSettingsStore } from '@/stores/settings'
import { ElMessage } from 'element-plus'

const settings = useSettingsStore()

const providerOptions = [
  { label: 'OpenAI', value: 'openai' },
  { label: 'Kimi（Moonshot）', value: 'kimi' },
  { label: 'Anthropic Claude', value: 'anthropic' },
  { label: 'DeepSeek', value: 'deepseek' }
]

const form = reactive({
  provider: '',
  key: ''
})

const rows = computed(() => {
  return settings.listProviders().map(p => ({ provider: p, key: settings.apiKeys[p] }))
})

const saveKey = () => {
  if (!form.provider || !form.key) return
  settings.setApiKey(form.provider, form.key)
  ElMessage.success('密钥已保存')
}

const removeKey = (provider) => {
  settings.removeApiKey(provider)
  ElMessage.success('已删除密钥')
}

const setActive = (provider) => {
  settings.setActiveProvider(provider)
  ElMessage.success(`已将 ${provider} 设为激活提供商`)
}

const prefill = (row) => {
  form.provider = row.provider
  form.key = row.key
}

const resetForm = () => {
  form.provider = ''
  form.key = ''
}

const maskKey = (key) => {
  if (!key) return ''
  const len = key.length
  if (len <= 8) return '*'.repeat(len)
  return key.slice(0, 4) + '*'.repeat(Math.max(0, len - 8)) + key.slice(-4)
}
</script>

<style lang="scss" scoped>
.settings-view {
  padding: 24px;
  background-color: $bg-primary;
  color: $text-primary;
}

.card {
  background-color: $bg-secondary;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.section {
  margin-top: 8px;
}

.desc {
  color: $text-secondary;
  margin: 8px 0 16px;
}

.api-form {
  margin-bottom: 16px;
}

.list-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 8px 0 12px;
}

.keys-table {
  width: 100%;
}

.masked {
  font-family: monospace;
}

.empty-tip {
  color: $text-secondary;
}
</style>