import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

// 本地持久化键名
const STORAGE_KEY = 'doc-assistant.settings'

function loadFromStorage() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch (_) {
    return null
  }
}

function saveToStorage(payload) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(payload))
  } catch (_) {
    // 忽略存储异常
  }
}

export const useSettingsStore = defineStore('settings', () => {
  // 统一的 API 密钥管理：provider -> key
  const apiKeys = ref({})
  // 当前激活的 AI 提供商，用于统一调用
  const activeAiProvider = ref('')

  // 初始化：从本地存储加载
  const initial = loadFromStorage()
  if (initial) {
    apiKeys.value = initial.apiKeys || {}
    activeAiProvider.value = initial.activeAiProvider || ''
  }

  // 持久化
  watch([apiKeys, activeAiProvider], () => {
    saveToStorage({
      apiKeys: apiKeys.value,
      activeAiProvider: activeAiProvider.value
    })
  }, { deep: true })

  // Actions
  const setApiKey = (provider, key) => {
    if (!provider) return
    apiKeys.value[provider] = key || ''
  }

  const removeApiKey = (provider) => {
    if (!provider) return
    delete apiKeys.value[provider]
    if (activeAiProvider.value === provider) {
      activeAiProvider.value = ''
    }
  }

  const setActiveProvider = (provider) => {
    activeAiProvider.value = provider || ''
  }

  const getActiveKey = () => {
    if (!activeAiProvider.value) return ''
    return apiKeys.value[activeAiProvider.value] || ''
  }

  const listProviders = () => Object.keys(apiKeys.value)

  return {
    apiKeys,
    activeAiProvider,
    setApiKey,
    removeApiKey,
    setActiveProvider,
    getActiveKey,
    listProviders
  }
})