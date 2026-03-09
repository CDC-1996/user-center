<template>
  <div class="study-stats">
    <el-row :gutter="20">
      <!-- 今日统计 -->
      <el-col :span="6">
        <el-card class="stat-card today">
          <div class="stat-icon">📚</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.todayQuestions || 0 }}</div>
            <div class="stat-label">今日学习题目</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card time">
          <div class="stat-icon">⏱️</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.todayTime || 0 }}</div>
            <div class="stat-label">今日学习(分钟)</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card collect">
          <div class="stat-icon">⭐</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.collectCount || 0 }}</div>
            <div class="stat-label">收藏题目</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card review">
          <div class="stat-icon">📝</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.reviewCount || 0 }}</div>
            <div class="stat-label">错题本</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 总体统计 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>📊 学习概览</span>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="累计学习">{{ stats.totalQuestions || 0 }} 题</el-descriptions-item>
            <el-descriptions-item label="累计时长">{{ stats.totalTime || 0 }} 分钟</el-descriptions-item>
            <el-descriptions-item label="学习课程">{{ stats.totalCourses || 0 }} 门</el-descriptions-item>
            <el-descriptions-item label="完成课程">{{ stats.completedCourses || 0 }} 门</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      
      <!-- 每日学习图表 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>📈 最近7天学习</span>
            </div>
          </template>
          <div class="chart-container" ref="chartRef"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 课程进度 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>📖 课程进度</span>
          <el-tag type="info">{{ progress.length }} 门课程学习中</el-tag>
        </div>
      </template>
      
      <el-empty v-if="progress.length === 0" description="还没有学习任何课程" />
      
      <div v-else class="progress-list">
        <div v-for="item in progress" :key="item.courseId" class="progress-item">
          <div class="progress-info">
            <span class="course-name">{{ item.courseName }}</span>
            <span class="category-name">{{ item.categoryName }}</span>
          </div>
          <el-progress 
            :percentage="Number(item.progress) || 0" 
            :format="format"
            :stroke-width="12"
          />
          <div class="progress-detail">
            {{ item.completedQuestions }} / {{ item.totalQuestions }} 题
          </div>
        </div>
      </div>
    </el-card>
    
    <!-- 继续学习入口 -->
    <el-card style="margin-top: 20px;" v-if="continueQuestion">
      <template #header>
        <span>▶️ 继续学习</span>
      </template>
      <div class="continue-section">
        <div class="continue-info">
          <el-tag>{{ continueQuestion.courseName }}</el-tag>
          <span class="continue-title">{{ continueQuestion.title }}</span>
        </div>
        <el-button type="primary" @click="goToQuestion(continueQuestion.id)">
          继续学习
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { studyApi } from '@/api/study'

const router = useRouter()

const stats = ref({})
const progress = ref([])
const dailyStats = ref({})
const continueQuestion = ref(null)
const chartRef = ref(null)
let chartInstance = null

onMounted(async () => {
  await Promise.all([
    loadStats(),
    loadProgress(),
    loadDailyStats(),
    loadContinueQuestion()
  ])
})

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose()
  }
})

async function loadStats() {
  try {
    const res = await studyApi.getStats()
    if (res.data) {
      stats.value = res.data
    }
  } catch (e) {
    console.error('加载统计失败', e)
  }
}

async function loadProgress() {
  try {
    const res = await studyApi.getProgress()
    if (res.data) {
      progress.value = res.data
    }
  } catch (e) {
    console.error('加载进度失败', e)
  }
}

async function loadDailyStats() {
  try {
    const res = await studyApi.getDailyStats(7)
    if (res.data) {
      dailyStats.value = res.data
      renderChart()
    }
  } catch (e) {
    console.error('加载每日统计失败', e)
  }
}

async function loadContinueQuestion() {
  try {
    const res = await studyApi.getContinueQuestion()
    if (res.data) {
      continueQuestion.value = res.data
    }
  } catch (e) {
    console.error('加载继续学习失败', e)
  }
}

function format(percentage) {
  return percentage.toFixed(0) + '%'
}

function goToQuestion(id) {
  router.push(`/question/${id}`)
}

function renderChart() {
  // 简单的文本图表，实际项目可以使用echarts
  const dates = Object.keys(dailyStats.value).sort()
  const values = dates.map(d => dailyStats.value[d].todayQuestions || 0)
  
  if (chartRef.value) {
    const max = Math.max(...values, 1)
    const bars = dates.map((date, i) => {
      const height = (values[i] / max * 100)
      return `<div class="bar-item">
        <div class="bar" style="height: ${height}%">
          <span class="bar-value">${values[i]}</span>
        </div>
        <span class="bar-label">${date.slice(5)}</span>
      </div>`
    }).join('')
    
    chartRef.value.innerHTML = `<div class="simple-chart">${bars}</div>`
  }
}
</script>

<style scoped>
.study-stats {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  width: 100%;
}

.stat-icon {
  font-size: 36px;
  margin-right: 16px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.stat-card.today .stat-icon { color: #409eff; }
.stat-card.time .stat-icon { color: #67c23a; }
.stat-card.collect .stat-icon { color: #e6a23c; }
.stat-card.review .stat-icon { color: #f56c6c; }

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-list {
  display: grid;
  gap: 16px;
}

.progress-item {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.course-name {
  font-weight: 500;
}

.category-name {
  color: #909399;
  font-size: 13px;
}

.progress-detail {
  text-align: right;
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.continue-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.continue-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.continue-title {
  font-size: 16px;
}

.chart-container {
  height: 200px;
}

.simple-chart {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 100%;
  padding-top: 20px;
}

.bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.bar {
  width: 30px;
  background: linear-gradient(180deg, #409eff 0%, #79bbff 100%);
  border-radius: 4px 4px 0 0;
  position: relative;
  transition: height 0.3s;
}

.bar-value {
  position: absolute;
  top: -20px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 12px;
  color: #606266;
}

.bar-label {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
