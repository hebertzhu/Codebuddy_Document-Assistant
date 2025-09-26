<template>
  <el-card class="literature-card" shadow="hover">
    <div class="card-header">
      <h3 class="title">{{ literature.title }}</h3>
      <div class="actions">
        <el-button size="small" @click="$emit('download', literature.id, literature.originalFileName)">
          下载
        </el-button>
        <el-button size="small" type="primary" @click="$emit('view', literature.id)">
          查看
        </el-button>
        <el-button size="small" type="danger" @click="$emit('delete', literature.id, literature.title)">
          删除
        </el-button>
      </div>
    </div>
    <div class="card-body">
      <div class="info-item">
        <span class="label">分类:</span>
        <span class="value">{{ literature.category }}</span>
      </div>
      <div class="info-item">
        <span class="label">作者:</span>
        <span class="value">{{ literature.author }}</span>
      </div>
      <div class="info-item">
        <span class="label">年份:</span>
        <span class="value">{{ literature.publishYear }}</span>
      </div>
      <div class="info-item">
        <span class="label">大小:</span>
        <span class="value">{{ literature.fileSizeReadable }}</span>
      </div>
      <div class="info-item">
        <span class="label">上传时间:</span>
        <span class="value">{{ literature.createTime }}</span>
      </div>
    </div>
  </el-card>
</template>

<script setup>
defineProps({
  literature: {
    type: Object,
    required: true
  }
})

defineEmits(['download', 'view', 'delete'])
</script>

<style lang="scss" scoped>
.literature-card {
  margin-bottom: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;

    .title {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  .card-body {
    .info-item {
      display: flex;
      margin-bottom: 8px;

      .label {
        width: 80px;
        color: #606266;
      }

      .value {
        color: #303133;
      }
    }
  }
}
</style>