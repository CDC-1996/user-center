<template>
  <div class="course-detail">
    <!-- 返回按钮 -->
    <div class="back-nav">
      <el-button @click="goBack" text>
        <el-icon><ArrowLeft /></el-icon>
        返回课程列表
      </el-button>
    </div>

    <el-row :gutter="20">
      <!-- 主内容区 -->
      <el-col :span="18">
        <!-- 课程信息 -->
        <el-card class="course-info-card">
          <div class="course-header">
            <div class="course-title-section">
              <el-tag :type="getDifficultyType(course.difficulty)" size="small">
                {{ getDifficultyText(course.difficulty) }}
              </el-tag>
              <h1>{{ course.courseName }}</h1>
              <p class="course-desc">{{ course.description }}</p>
            </div>
            <div class="course-stats">
              <div class="stat-item">
                <span class="stat-value">{{ course.questionCount || 0 }}</span>
                <span class="stat-label">题目总数</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ course.completedQuestions || 0 }}</span>
                <span class="stat-label">已学习</span>
              </div>
            </div>
          </div>
          
          <div class="progress-section" v-if="course.isStarted">
            <span>学习进度</span>
            <el-progress :percentage="parseFloat(course.progress) || 0" :stroke-width="10" />
          </div>
        </el-card>

        <!-- 题目列表 -->
        <el-card class="question-list-card">
          <template #header>
            <div class="list-header">
              <span>题目列表 ({{ questions.length }}题)</span>
              <el-button type="primary" size="small" @click="startStudy" v-if="questions.length > 0">
                {{ course.isStarted ? '继续学习' : '开始学习' }}
              </el-button>
            </div>
          </template>

          <div v-if="loading" class="loading-container">
            <el-icon class="is-loading" :size="40"><Loading /></el-icon>
            <p>加载中...</p>
          </div>

          <el-table v-else :data="questions" style="width: 100%" @row-click="goToQuestion">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="title" label="题目标题" min-width="300">
              <template #default="{ row }">
                <div class="question-title-cell">
                  <span class="title-text">{{ row.title }}</span>
                  <el-tag v-if="row.isViewed" size="small" type="success">已学习</el-tag>
                  <el-tag v-if="row.isCollected" size="small" type="warning">已收藏</el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="difficulty" label="难度" width="80">
              <template #default="{ row }">
                <el-tag :type="getDifficultyType(row.difficulty)" size="small">
                  {{ getDifficultyText(row.difficulty) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button type="primary" link size="small">
                  学习
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-wrapper" v-if="total > pageSize">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="total"
              layout="prev, pager, next"
              @current-change="loadQuestions"
            />
          </div>
        </el-card>
      </el-col>

      <!-- 侧边栏 -->
      <el-col :span="6">
        <el-card class="sidebar-card">
          <h3>📖 课程信息</h3>
          <div class="info-item">
            <span>分类</span>
            <span>{{ course.categoryName || '未分类' }}</span>
          </div>
          <div class="info-item">
            <span>难度</span>
            <el-tag :type="getDifficultyType(course.difficulty)" size="small">
              {{ getDifficultyText(course.difficulty) }}
            </el-tag>
          </div>
          <div class="info-item">
            <span>题目数</span>
            <span>{{ course.questionCount || 0 }}</span>
          </div>
        </el-card>

        <el-card class="sidebar-card" v-if="course.isStarted">
          <h3>📊 学习进度</h3>
          <el-progress type="dashboard" :percentage="parseFloat(course.progress) || 0">
            <template #default="{ percentage }">
              <span class="percentage-value">{{ percentage }}%</span>
            </template>
          </el-progress>
          <p class="progress-text">已完成 {{ course.completedQuestions || 0 }} 题</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Loading } from '@element-plus/icons-vue'
import { courseApi, questionApi } from '../api/study'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const course = ref({
  id: null,
  courseName: '',
  description: '',
  difficulty: 1,
  questionCount: 0,
  categoryName: '',
  isStarted: false,
  progress: 0,
  completedQuestions: 0
})

const questions = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const getDifficultyText = (level) => {
  const texts = { 1: '入门', 2: '进阶', 3: '高级' }
  return texts[level] || '入门'
}

const getDifficultyType = (level) => {
  const types = { 1: 'success', 2: 'warning', 3: 'danger' }
  return types[level] || 'info'
}

const loadCourse = async () => {
  try {
    const data = await courseApi.getDetail(route.params.id)
    console.log('课程详情:', data)
    if (data) {
      course.value = data
    }
  } catch (error) {
    console.error('加载课程失败:', error)
    ElMessage.error('加载课程失败')
    router.push('/courses')
  }
}

const loadQuestions = async () => {
  try {
    loading.value = true
    const data = await questionApi.getByCourse(route.params.id, currentPage.value, pageSize.value)
    console.log('题目列表:', data)
    if (Array.isArray(data)) {
      questions.value = data
      total.value = data.length
    } else if (data && data.records) {
      questions.value = data.records
      total.value = data.total
    }
  } catch (error) {
    console.error('加载题目失败:', error)
    ElMessage.error('加载题目列表失败')
  } finally {
    loading.value = false
  }
}

const startStudy = () => {
  if (questions.value.length > 0) {
    // 找到第一个未学习的题目
    const unViewed = questions.value.find(q => !q.isViewed)
    if (unViewed) {
      router.push(`/question/${unViewed.id}`)
    } else {
      // 都学过了，从第一个开始
      router.push(`/question/${questions.value[0].id}`)
    }
  }
}

const goToQuestion = (row) => {
  router.push(`/question/${row.id}`)
}

const goBack = () => {
  router.push('/courses')
}

onMounted(() => {
  loadCourse()
  loadQuestions()
})
</script>

<style scoped>
.course-detail {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.back-nav {
  margin-bottom: 20px;
}

.course-info-card {
  margin-bottom: 20px;
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.course-title-section h1 {
  font-size: 24px;
  color: #303133;
  margin: 10px 0;
}

.course-desc {
  color: #909399;
  margin-top: 10px;
}

.course-stats {
  display: flex;
  gap: 30px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.progress-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.question-list-card {
  margin-bottom: 20px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.question-title-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-text {
  flex: 1;
}

.pagination-wrapper {
  margin-top: 20px;
  text-align: center;
}

.sidebar-card {
  margin-bottom: 20px;
}

.sidebar-card h3 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 15px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}

.info-item:last-child {
  border-bottom: none;
}

.percentage-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.progress-text {
  text-align: center;
  color: #909399;
  margin-top: 10px;
}

.loading-container {
  text-align: center;
  padding: 50px;
  color: #909399;
}
</style>
