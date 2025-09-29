<template>
  <div class="main-layout">
    <TopNav />
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
import Sidebar from '@/components/Sidebar.vue';
import TopNav from '@/components/TopNav.vue';
</script>

<style lang="scss" scoped>
.main-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;

  .container {
    display: flex;
    flex-grow: 1;
    overflow: hidden;
    padding-top: 56px; // account for fixed top nav
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