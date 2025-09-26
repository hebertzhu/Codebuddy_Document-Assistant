<template>
  <div class="page-header">
    <div class="container">
      <h1>{{ title }}</h1>
      <p class="subtitle">{{ subtitle }}</p>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  subtitle: {
    type: String,
    default: ''
  }
})
</script>

<style lang="scss" scoped>
.page-header {
  background-color: #f5f7fa;
  padding: 40px 0;
  text-align: center;
  margin-bottom: 30px;

  .container {
    max-width: 1200px;
    margin: 0 auto;
  }

  h1 {
    font-size: 36px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 10px;
  }

  .subtitle {
    font-size: 18px;
    color: #606266;
  }
}
</style>