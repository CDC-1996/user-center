<template>
  <div class="user-center">
    <el-container>
      <el-header>
        <h1>用户中心</h1>
        <el-button @click="handleLogout" type="danger" plain>退出登录</el-button>
      </el-header>
      
      <el-main>
        <el-card>
          <template #header>
            <span>个人信息</span>
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
            编辑信息
          </el-button>
        </el-card>
      </el-main>
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
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '../api'

const router = useRouter()
const user = ref(null)
const showEditDialog = ref(false)
const updating = ref(false)

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
  background: #fff;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.el-header h1 {
  margin: 0;
  font-size: 20px;
}

.el-main {
  padding: 20px;
}
</style>
