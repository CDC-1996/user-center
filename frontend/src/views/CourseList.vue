<template>
  <div class="course-list">
    <div class="page-header">
      <h1>📚 面试学习中心</h1>
      <p>系统学习核心技术，助力面试通关</p>
    </div>

    <!-- 学习统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-number">{{ stats.totalQuestions || 0 }}</div>
          <div class="stat-label">已学习题目</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-number">{{ stats.todayQuestions || 0 }}</div>
          <div class="stat-label">今日学习</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-number">{{ stats.totalCourses || 0 }}</div>
          <div class="stat-label">学习课程</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-number">{{ Math.floor((stats.totalTime || 0) / 60) }}</div>
          <div class="stat-label">学习时长(分)</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 课程分类 -->
    <div v-for="category in categories" :key="category.id" class="category-section">
      <h2 class="category-title">
        <span class="category-icon">{{ getCategoryIcon(category.categoryCode) }}</span>
        {{ category.categoryName }}
        <span class="course-count">{{ category.courses?.length || 0 }}门课程</span>
      </h2>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="course in category.courses" :key="course.id">
          <el-card shadow="hover" class="course-card" @click="goToCourse(course.id)">
            <div class="course-header">
              <span class="difficulty-tag" :class="getDifficultyClass(course.difficulty)">
                {{ getDifficultyText(course.difficulty) }}
              </span>
              <span class="question-count">{{ course.questionCount || 0 }}题</span>
            </div>
            <h3 class="course-name">{{ course.courseName }}</h3>
            <p class="course-desc">{{ course.description }}</p>
            <div class="course-progress" v-if="course.isStarted">
              <el-progress :percentage="course.progress || 0" :stroke-width="6" />
              <span class="progress-text">已学习 {{ course.completedQuestions || 0 }}/{{ course.questionCount || 0 }}</span>
            </div>
            <div class="course-progress" v-else>
              <span class="not-started">未开始学习</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 空状态 -->
    <el-empty v-if="!loading && categories.length === 0" description="暂无课程数据" />
    
    <!-- 加载中 -->
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      <p>加载中...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { courseApi, studyApi } from '../api/study'

const router = useRouter()
const categories = ref([])
const loading = ref(true)
const stats = ref({
  totalQuestions: 0,
  todayQuestions: 0,
  totalCourses: 0,
  totalTime: 0
})

const getCategoryIcon = (code) => {
  const icons = {
    'java-basic': '☕',
    'java-concurrent': '🔄',
    'java-jvm': '🖥️',
    'spring': '🌱',
    'mysql': '🗄️',
    'redis': '⚡',
    'distributed': '🌐',
    'network': '🔌',
    'os': '💾',
    'linux': '🐧',
    'system-design': '🏗️',
    'frontend-basic': '🎨',
    'frontend-framework': '⚛️'
  }
  return icons[code] || '📖'
}

const getDifficultyText = (level) => {
  const texts = { 1: '入门', 2: '进阶', 3: '高级' }
  return texts[level] || '入门'
}

const getDifficultyClass = (level) => {
  const classes = { 1: 'easy', 2: 'medium', 3: 'hard' }
  return classes[level] || 'easy'
}

const goToCourse = (courseId) => {
  router.push(`/course/${courseId}`)
}

const loadData = async () => {
  loading.value = true
  try {
    // 加载课程分类
    const catData = await courseApi.getCategories()
    console.log('课程数据:', catData)
    if (Array.isArray(catData)) {
      categories.value = catData
    }
    
    // 尝试加载学习统计（可能需要登录）
    try {
      const statsData = await studyApi.getStats()
      if (statsData) {
        stats.value = statsData
      }
    } catch (e) {
      console.log('学习统计加载失败，可能未登录')
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.course-list {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  color: #303133;
  margin-bottom: 10px;
}

.page-header p {
  color: #909399;
}

.stats-row {
  margin-bottom: 30px;
}

.stat-card {
  text-align: center;
  padding: 20px 0;
}

.stat-number {
  font-size: 36px;
  font-weight: bold;
  color: #409EFF;
}

.stat-label {
  color: #909399;
  margin-top: 5px;
}

.category-section {
  margin-bottom: 40px;
}

.category-title {
  font-size: 20px;
  color: #303133;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #409EFF;
  display: flex;
  align-items: center;
}

.category-icon {
  margin-right: 10px;
}

.course-count {
  font-size: 14px;
  color: #909399;
  margin-left: auto;
  font-weight: normal;
}

.course-card {
  cursor: pointer;
  margin-bottom: 20px;
  transition: transform 0.3s;
}

.course-card:hover {
  transform: translateY(-5px);
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.difficulty-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.difficulty-tag.easy {
  background: #f0f9eb;
  color: #67c23a;
}

.difficulty-tag.medium {
  background: #fdf6ec;
  color: #e6a23c;
}

.difficulty-tag.hard {
  background: #fef0f0;
  color: #f56c6c;
}

.question-count {
  color: #909399;
  font-size: 14px;
}

.course-name {
  font-size: 16px;
  color: #303133;
  margin-bottom: 10px;
}

.course-desc {
  color: #909399;
  font-size: 14px;
  line-height: 1.5;
  height: 42px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.course-progress {
  margin-top: 15px;
}

.progress-text {
  font-size: 12px;
  color: #909399;
}

.not-started {
  color: #c0c4cc;
  font-size: 14px;
}

.loading-container {
  text-align: center;
  padding: 50px;
  color: #909399;
}
</style>
