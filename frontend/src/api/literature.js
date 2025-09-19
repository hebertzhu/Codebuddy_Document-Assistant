import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 可以在这里添加token等
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    const message = error.response?.data?.message || '请求失败'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

// 文献API
export const literatureApi = {
  // 获取文献列表
  getLiteratureList: (page, size, category, description, readingGuide, tags) => {
    return api.get('/literature/list', {
      params: {
        page,
        size,
        category,
        description,
        readingGuide,
        tags
      }
    })
  },

  // 上传文献
  uploadLiterature: (formData) => {
    return api.post('/literature/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 批量导入
  batchImportLiterature: (formData, onProgress) => {
    return api.post('/literature/batch-import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          onProgress(percent)
        }
      }
    })
  },

  // 下载文献
  downloadLiterature: (id) => {
    return api.get(`/literature/download/${id}`, {
      responseType: 'blob'
    })
  },

  // 获取文献详情
  getLiteratureDetail: (id) => {
    return api.get(`/literature/${id}`)
  }
}

export default api