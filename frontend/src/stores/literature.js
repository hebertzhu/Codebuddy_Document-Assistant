import { defineStore } from 'pinia'
import { ref } from 'vue'
import { literatureApi } from '@/api/literature'
import { useBatchImportProgress } from '@/utils/sse'

export const useLiteratureStore = defineStore('literature', () => {
  // 状态
  const literatureList = ref([])
  const totalCount = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)
  const loading = ref(false)
  const error = ref(null)
  
  // 筛选条件
  const filters = ref({
    category: '',
    description: '',
    readingGuide: '',
    tags: ''
  })

  // 获取文献列表
  const fetchLiteratureList = async (page = 1, size = 10) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await literatureApi.getLiteratureList(
        page,
        size,
        filters.value.category,
        filters.value.description,
        filters.value.readingGuide,
        filters.value.tags
      )
      
      literatureList.value = response.data.records || []
      totalCount.value = response.data.total
      currentPage.value = page
      pageSize.value = size
    } catch (err) {
      error.value = err.response?.data?.message || '获取文献列表失败'
      console.error('获取文献列表失败:', err)
    } finally {
      loading.value = false
    }
  }

  // 设置搜索参数
  const setSearchParams = (params) => {
    filters.value = { ...filters.value, ...params };
  };

  // 删除文献
  const deleteLiterature = async (id) => {
    loading.value = true;
    error.value = null;
    try {
      await literatureApi.deleteLiterature(id);
      await fetchLiteratureList(currentPage.value, pageSize.value);
    } catch (err) {
      error.value = err.response?.data?.message || '删除文献失败';
      console.error('删除文献失败:', err);
    } finally {
      loading.value = false;
    }
  };

  // 重置筛选条件
  const resetFilters = () => {
    filters.value = {
      category: '',
      description: '',
      readingGuide: '',
      tags: ''
    }
    fetchLiteratureList(1, pageSize.value)
  }

  // 上传文献
  const uploadLiterature = async (file) => {
    loading.value = true
    error.value = null
    
    try {
      const formData = new FormData()
      formData.append('file', file)
      
      const response = await literatureApi.uploadLiterature(formData)
      await fetchLiteratureList(currentPage.value, pageSize.value)
      return response.data
    } catch (err) {
      error.value = err.response?.data?.message || '文献上传失败'
      console.error('文献上传失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 批量导入
  const batchImportLiterature = async (files, onProgress) => {
    loading.value = true
    error.value = null
    
    try {
      const formData = new FormData()
      files.forEach(file => formData.append('files', file))
      // 创建批量导入任务，获得任务ID
      const { importId } = await literatureApi.startBatchImport(formData)

      // 连接SSE获取进度
      const {
        connectBatchImport,
        onProgressUpdate,
        onFileComplete,
        onFileError,
        onComplete,
        onError
      } = useBatchImportProgress()

      if (onProgress) {
        onProgressUpdate((payload) => {
          const current = payload?.current || 0
          const total = payload?.total || files.length
          const percent = total > 0 ? Math.round((current * 100) / total) : 0
          onProgress(percent)
        })
      }

      onFileComplete(() => {
        // 可扩展：收集完成的文件信息
      })

      onFileError((payload) => {
        console.error('批量导入文件错误:', payload)
      })

      await new Promise((resolve, reject) => {
        onComplete(async () => {
          try {
            await fetchLiteratureList(currentPage.value, pageSize.value)
            resolve()
          } catch (e) {
            reject(e)
          }
        })
        onError((err) => {
          reject(err)
        })
        connectBatchImport(importId)
      })
      
      return { importId }
    } catch (err) {
      error.value = err.response?.data?.message || '批量导入失败'
      console.error('批量导入失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 下载文献
  const downloadLiterature = async (id) => {
    try {
      const response = await literatureApi.downloadLiterature(id)
      
      // 创建下载链接
      const blob = new Blob([response.data])
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      
      // 从响应头获取文件名
      const contentDisposition = response.headers['content-disposition']
      let fileName = 'document'
      if (contentDisposition) {
        const fileNameMatch = contentDisposition.match(/filename="?(.+)"?/)
        if (fileNameMatch && fileNameMatch[1]) {
          fileName = fileNameMatch[1]
        }
      }
      
      link.download = fileName
      document.body.appendChild(link)
      link.click()
      link.remove()
      window.URL.revokeObjectURL(url)
    } catch (err) {
      error.value = err.response?.data?.message || '下载文献失败'
      console.error('下载文献失败:', err)
      throw err
    }
  }

  // 获取文献详情
  const getLiteratureDetail = async (id) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await literatureApi.getLiteratureDetail(id)
      return response.data
    } catch (err) {
      error.value = err.response?.data?.message || '获取文献详情失败'
      console.error('获取文献详情失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  return {
    // 状态
    literatureList,
    totalCount,
    currentPage,
    pageSize,
    loading,
    error,
    filters,
    
    // 方法
    fetchLiteratureList,
    resetFilters,
    uploadLiterature,
    batchImportLiterature,
    downloadLiterature,
    getLiteratureDetail
  }
})