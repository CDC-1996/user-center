<template>
  <div class="course-list">
    <div class="page-header">
      <h1>📚 Java学习中心</h1>
      <p>系统学习Java核心技术，助力面试通关</p>
    </div>

    <!-- 学习统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-number">{{ stats.totalQuestions }}</div>
          <div class="stat-label">已学习题目</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-number">{{ stats.todayQuestions }}</div>
          <div class="stat-label">今日学习</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-number">{{ stats.totalCourses }}</div>
          <div class="stat-label">学习课程</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-number">{{ Math.floor(stats.totalTime / 60) }}</div>
          <div class="stat-label">学习时长(分)</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 课程分类 -->
    <div v-for="category in categories" :key="category.id" class="category-section">
      <h2 class="category-title">
        <span class="category-icon">{{ getCategoryIcon(category.categoryCode) }}</span>
        {{ category.categoryName }}
      </h2>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="course in category.courses" :key="course.id">
          <el-card shadow="hover" class="course-card" @click="goToCourse(course.id)">
            <div class="course-header">
              <span class="difficulty-tag" :class="getDifficultyClass(course.difficulty)">
                {{ getDifficultyText(course.difficulty) }}
              </span>
              <span class="question-count">{{ course.questionCount }}题</span>
            </div>
            <h3 class="course-name">{{ course.courseName }}</h3>
            <p class="course-desc">{{ course.description }}</p>
            <div class="course-progress" v-if="course.isStarted">
              <el-progress :percentage="course.progress" :stroke-width="6" />
              <span class="progress-text">已学习 {{ course.completedQuestions }}/{{ course.questionCount }}</span>
            </div>
            <div class="course-progress" v-else>
              <span class="not-started">未开始学习</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { courseApi, studyApi } from '../api/study'

const router = useRouter()
const categories = ref([])
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
    'distributed': '🌐'
  }
  return icons[code] || '📖'
}

const getDifficultyText = (level) => {
  const texts = { 1: '入门', 2: '进阶', 3: '高级' }
  return texts[level] || '未知'
}

const getDifficultyClass = (level) => {
  const classes = { 1: 'easy', 2: 'medium', 3: 'hard' }
  return classes[level] || ''
}

const goToCourse = (courseId) => {
  router.push(`/course/${courseId}`)
}

const loadData = async () => {
  try {
    const [catRes, statsRes] = await Promise.all([
      courseApi.getCategories(),
      studyApi.getStats()
    ])
    if (catRes.data.code === 200) {
      categories.value = catRes.data.data
    }
    if (statsRes.data.code === 200) {
      stats.value = statsRes.data.data
    }
  } catch (error) {
    if (error.response?.status !== 401) {
      ElMessage.error('加载数据失败')
    }
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
}

.category-icon {
  margin-right: 10px;
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
</style>
