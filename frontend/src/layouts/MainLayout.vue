<template>
  <div class="main-layout">
    <header class="header">
      <PageHeader title="文档助手" subtitle="一个智能的文献管理工具" />
    </header>
    <div class="container">
      <aside class="sidebar">
        <Sidebar />
      </aside>
      <main class="main-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import PageHeader from '@/components/PageHeader.vue';
import Sidebar from '@/components/Sidebar.vue';
</script>

<style lang="scss" scoped>
.main-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;

  .header {
    flex-shrink: 0;
  }

  .container {
    display: flex;
    flex-grow: 1;
    overflow: hidden;
  }

  .sidebar {
    flex-shrink: 0;
    width: 240px;
    background-color: #f5f7fa;
    border-right: 1px solid #e6e6e6;
  }

  .main-content {
    flex-grow: 1;
    padding: 20px;
    overflow-y: auto;
  }
}
</style>