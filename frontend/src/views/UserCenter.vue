<template>
  <div class="user-center">
    <el-container>
      <el-header>
        <h1>🎯 学习中心</h1>
        <div class="header-right">
          <span class="username">{{ user?.nickname || user?.username }}</span>
          <el-button @click="handleLogout" type="danger" plain size="small">退出登录</el-button>
        </div>
      </el-header>
      
      <el-container>
        <!-- 侧边栏导航 -->
        <el-aside width="200px">
          <el-menu
            :default-active="activeMenu"
            router
            class="side-menu"
          >
            <el-menu-item index="/">
              <el-icon><User /></el-icon>
              <span>个人信息</span>
            </el-menu-item>
            
            <el-menu-item index="/courses">
              <el-icon><Reading /></el-icon>
              <span>课程学习</span>
            </el-menu-item>
            
            <el-menu-item index="/random">
              <el-icon><Refresh /></el-icon>
              <span>随机刷题</span>
            </el-menu-item>
            
            <el-menu-item index="/review">
              <el-icon><Collection /></el-icon>
              <span>错题本</span>
            </el-menu-item>
            
            <el-menu-item index="/stats">
              <el-icon><DataLine /></el-icon>
              <span>学习统计</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        
        <!-- 主内容区 -->
        <el-main>
          <!-- 个人信息卡片 -->
          <el-card v-if="$route.path === '/'">
            <template #header>
              <span>📋 个人信息</span>
            </template>
            
            <el-descriptions :column="2" border>
              <el-descriptions-item label="用户ID">{{ user?.id }}</el-descriptions-item>
              <el-descriptions-item label="用户名">{{ user?.username }}</el-descriptions-item>
              <el-descriptions-item label="昵称">{{ user?.nickname }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ user?.email || '未设置' }}</el-descriptions-item>
              <el-descriptions-item label="手机">{{ user?.phone || '未设置' }}</el-descriptions-item>
              <el-descriptions-item label="性别">{{ genderText }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="user?.status === 1 ? 'success' : 'danger'">
                  {{ user?.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="注册时间">{{ user?.createdAt }}</el-descriptions-item>
            </el-descriptions>
            
            <el-divider />
            
            <el-button type="primary" @click="showEditDialog = true">
              ✏️ 编辑信息
            </el-button>
          </el-card>
          
          <!-- 学习入口卡片 -->
          <div v-if="$route.path === '/'" class="quick-entry">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-card shadow="hover" class="entry-card" @click="$router.push('/courses')">
                  <div class="entry-icon">📚</div>
                  <div class="entry-title">课程学习</div>
                  <div class="entry-desc">13个分类 · 113道题目</div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card shadow="hover" class="entry-card" @click="$router.push('/random')">
                  <div class="entry-icon">🎲</div>
                  <div class="entry-title">随机刷题</div>
                  <div class="entry-desc">随机抽取题目练习</div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card shadow="hover" class="entry-card" @click="$router.push('/stats')">
                  <div class="entry-icon">📊</div>
                  <div class="entry-title">学习统计</div>
                  <div class="entry-desc">查看学习进度</div>
                </el-card>
              </el-col>
            </el-row>
          </div>
          
          <!-- 子路由内容 -->
          <router-view />
        </el-main>
      </el-container>
    </el-container>
    
    <!-- 编辑对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑个人信息" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="手机">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="editForm.gender">
            <el-radio :label="0">未知</el-radio>
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUpdate" :loading="updating">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Reading, Refresh, Collection, DataLine } from '@element-plus/icons-vue'
import { userApi } from '../api'

const router = useRouter()
const route = useRoute()
const user = ref(null)
const showEditDialog = ref(false)
const updating = ref(false)

const activeMenu = computed(() => route.path)

const editForm = reactive({
  nickname: '',
  email: '',
  phone: '',
  gender: 0
})

const genderText = computed(() => {
  const genders = ['未知', '男', '女']
  return genders[user.value?.gender || 0]
})

const loadUser = async () => {
  try {
    user.value = await userApi.getInfo()
    Object.assign(editForm, {
      nickname: user.value.nickname,
      email: user.value.email,
      phone: user.value.phone,
      gender: user.value.gender
    })
  } catch (error) {
    ElMessage.error('获取用户信息失败')
    router.push('/login')
  }
}

const handleUpdate = async () => {
  updating.value = true
  try {
    user.value = await userApi.updateInfo(editForm)
    showEditDialog.value = false
    ElMessage.success('更新成功')
  } catch (error) {
    ElMessage.error(error.message || '更新失败')
  } finally {
    updating.value = false
  }
}

const handleLogout = () => {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('user')
  ElMessage.success('已退出登录')
  router.push('/login')
}

onMounted(() => {
  loadUser()
})
</script>

<style scoped>
.user-center {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.el-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.el-header h1 {
  margin: 0;
  font-size: 20px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.username {
  font-size: 14px;
}

.side-menu {
  height: 100%;
  border-right: none;
}

.el-aside {
  background: #fff;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
}

.el-main {
  padding: 20px;
  background: #f5f7fa;
}

.quick-entry {
  margin-top: 20px;
}

.entry-card {
  text-align: center;
  padding: 20px 0;
  cursor: pointer;
  transition: transform 0.2s;
}

.entry-card:hover {
  transform: translateY(-5px);
}

.entry-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.entry-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.entry-desc {
  font-size: 12px;
  color: #999;
}

:deep(.el-menu-item) {
  font-size: 14px;
}

:deep(.el-menu-item.is-active) {
  background-color: #ecf5ff;
}
</style>
