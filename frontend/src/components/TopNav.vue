<template>
  <div class="top-nav">
    <div class="left">
      <div class="brand" @click="$router.push('/')">
        <span class="logo-dot"></span>
        <span class="brand-text">文档助手</span>
      </div>
    </div>
    <div class="center">
      <el-input
        v-model="search"
        placeholder="搜索文献、标签或作者..."
        class="global-search"
        clearable
        @keyup.enter="onSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>
    <div class="right">
      <el-tooltip content="通知" placement="bottom">
        <el-badge :value="notifCount" :max="99" class="action-item">
          <el-button text circle @click="openNotifications">
            <el-icon><Bell /></el-icon>
          </el-button>
        </el-badge>
      </el-tooltip>

      <el-tooltip content="帮助" placement="bottom">
        <el-button text class="action-item" @click="openHelp">
          <el-icon><QuestionFilled /></el-icon>
        </el-button>
      </el-tooltip>

      <el-dropdown class="action-item" @command="onUserCommand">
        <span class="el-dropdown-link">
          <el-avatar :size="28" src=""/>
          <el-icon class="caret"><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人中心</el-dropdown-item>
            <el-dropdown-item command="settings">设置</el-dropdown-item>
            <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Bell, ArrowDown, Search, QuestionFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const search = ref('')
const notifCount = ref(0)

const onSearch = () => {
  if (!search.value) return
  // TODO: 接入 useSearchStore 的联想/搜索行为
  ElMessage.info(`搜索：${search.value}`)
}

const openNotifications = () => {
  ElMessage.info('打开通知面板（占位）')
}

const openHelp = () => {
  window.open('https://github.com/your-org/your-docs', '_blank')
}

const onUserCommand = (cmd) => {
  if (cmd === 'settings') {
    // 跳转到设置页
    window.location.hash = '#/settings'
  } else if (cmd === 'profile') {
    ElMessage.info('打开个人中心（占位）')
  } else if (cmd === 'logout') {
    ElMessage.success('已退出（占位）')
  }
}
</script>

<style lang="scss" scoped>
.top-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: $bg-secondary;
  border-bottom: 1px solid #eee;
  z-index: 1000;
}

.left {
  display: flex;
  align-items: center;
}

.brand {
  display: flex;
  align-items: center;
  cursor: pointer;
  .logo-dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    background: $primary-color;
    margin-right: 8px;
  }
  .brand-text {
    font-weight: 700;
    color: $text-primary;
  }
}

.center {
  flex: 1;
  max-width: 560px;
  padding: 0 16px;
}

.global-search :deep(.el-input__wrapper) {
  border-color: transparent;
  box-shadow: none;
  background-color: #f7f7f7;
}

.right {
  display: flex;
  align-items: center;
}

.action-item {
  margin-left: 8px;
}

.caret {
  margin-left: 6px;
  color: #999;
}
</style>