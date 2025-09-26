<template>
  <div class="sidebar-container">
    <el-menu
      default-active="/"
      class="el-menu-vertical-demo"
      @open="handleOpen"
      @close="handleClose"
      router
    >
      <el-menu-item index="/">
        <el-icon><icon-menu /></el-icon>
        <span>文献管理</span>
      </el-menu-item>
      <el-menu-item index="/settings">
        <el-icon><setting /></el-icon>
        <span>设置</span>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup>
import { Menu as IconMenu, Setting } from '@element-plus/icons-vue';

const handleOpen = (key, keyPath) => {
  console.log(key, keyPath);
};
const handleClose = (key, keyPath) => {
  console.log(key, keyPath);
};
</script>

<style lang="scss" scoped>
.sidebar-container {
  height: 100%;
}

.el-menu-vertical-demo {
  height: 100%;
}
</style>