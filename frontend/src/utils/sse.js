// SSE工具函数
class SSEManager {
  constructor() {
    this.eventSource = null
    this.callbacks = new Map()
  }

  // 创建SSE连接
  connect(url, options = {}) {
    this.disconnect()

    this.eventSource = new EventSource(url, options)

    // 监听消息事件
    this.eventSource.onmessage = (event) => {
      this.handleMessage(event)
    }

    // 监听错误事件
    this.eventSource.onerror = (error) => {
      console.error('SSE连接错误:', error)
      this.disconnect()
    }

    return this.eventSource
  }

  // 处理消息
  handleMessage(event) {
    try {
      const data = JSON.parse(event.data)
      const { type, payload } = data

      // 触发对应的回调函数
      if (this.callbacks.has(type)) {
        this.callbacks.get(type).forEach(callback => {
          callback(payload)
        })
      }
    } catch (error) {
      console.error('SSE消息解析错误:', error)
    }
  }

  // 注册事件监听器
  on(eventType, callback) {
    if (!this.callbacks.has(eventType)) {
      this.callbacks.set(eventType, [])
    }
    this.callbacks.get(eventType).push(callback)
  }

  // 移除事件监听器
  off(eventType, callback) {
    if (this.callbacks.has(eventType)) {
      const callbacks = this.callbacks.get(eventType)
      const index = callbacks.indexOf(callback)
      if (index > -1) {
        callbacks.splice(index, 1)
      }
    }
  }

  // 断开连接
  disconnect() {
    if (this.eventSource) {
      this.eventSource.close()
      this.eventSource = null
    }
    this.callbacks.clear()
  }

  // 获取连接状态
  get connected() {
    return this.eventSource !== null && this.eventSource.readyState === EventSource.OPEN
  }
}

// 创建全局SSE管理器实例
export const sseManager = new SSEManager()

// 批量导入进度监听
export const useBatchImportProgress = () => {
  const connectBatchImport = (importId) => {
    const url = `/api/literature/batch-import/progress/${importId}`
    return sseManager.connect(url)
  }

  const onProgressUpdate = (callback) => {
    sseManager.on('progress', callback)
  }

  const onFileComplete = (callback) => {
    sseManager.on('file_complete', callback)
  }

  const onFileError = (callback) => {
    sseManager.on('file_error', callback)
  }

  const onComplete = (callback) => {
    sseManager.on('complete', callback)
  }

  const onError = (callback) => {
    sseManager.on('error', callback)
  }

  return {
    connectBatchImport,
    onProgressUpdate,
    onFileComplete,
    onFileError,
    onComplete,
    onError
  }
}

export default sseManager