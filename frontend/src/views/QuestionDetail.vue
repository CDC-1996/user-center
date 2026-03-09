<template>
  <div class="question-detail">
    <!-- 返回按钮 -->
    <div class="back-nav">
      <el-button @click="goBack" text>
        <el-icon><ArrowLeft /></el-icon>
        返回课程
      </el-button>
    </div>

    <el-row :gutter="20">
      <!-- 主内容区 -->
      <el-col :span="18">
        <el-card class="question-card">
          <!-- 加载中 -->
          <div v-if="loading" class="loading-container">
            <el-icon class="is-loading" :size="40"><Loading /></el-icon>
            <p>加载中...</p>
          </div>
          
          <template v-else>
            <!-- 题目标题 -->
            <div class="question-header">
              <h1 class="question-title">{{ question.title }}</h1>
              <div class="question-meta">
                <el-tag :type="getDifficultyType(question.difficulty)" size="small">
                  {{ getDifficultyText(question.difficulty) }}
                </el-tag>
              </div>
            </div>

            <!-- 题目内容 -->
            <div class="question-content">
              <h3>📝 题目描述</h3>
              <div class="content-text" v-html="formatContent(question.content)"></div>
            </div>

            <!-- 答案区域 -->
            <div class="answer-section">
              <div class="answer-header" @click="showAnswer = !showAnswer">
                <h3>💡 {{ showAnswer ? '隐藏答案' : '查看答案' }}</h3>
                <el-icon :class="{ 'is-rotate': showAnswer }"><ArrowDown /></el-icon>
              </div>
              <el-collapse-transition>
                <div v-show="showAnswer" class="answer-content">
                  <div class="answer-title">答案</div>
                  <div class="content-text" v-html="formatContent(question.answer)"></div>
                  
                  <div class="analysis-title">解析</div>
                  <div class="content-text" v-html="formatContent(question.analysis)"></div>
                </div>
              </el-collapse-transition>
            </div>

            <!-- 操作按钮 -->
            <div class="action-buttons">
              <el-button 
                :type="question.isCollected ? 'warning' : 'default'"
                @click="toggleCollect"
              >
                {{ question.isCollected ? '⭐ 已收藏' : '☆ 收藏' }}
              </el-button>
            </div>

            <!-- 上下题导航 -->
            <div class="question-nav">
              <el-button @click="goPrev" :disabled="!hasPrev">
                ← 上一题
              </el-button>
              <el-button @click="goNext" type="primary">
                下一题 →
              </el-button>
            </div>
          </template>
        </el-card>
      </el-col>

      <!-- 侧边栏 -->
      <el-col :span="6">
        <el-card class="sidebar-card">
          <h3>📖 {{ courseName || '当前课程' }}</h3>
          <div class="course-progress">
            <el-progress :percentage="progress" :stroke-width="8" />
            <p>已完成 {{ completedQuestions }}/{{ totalQuestions }} 题</p>
          </div>
        </el-card>

        <el-card class="sidebar-card">
          <h3>📊 本题信息</h3>
          <div class="info-item">
            <span>浏览次数</span>
            <span>{{ question.viewCount || 0 }}</span>
          </div>
          <div class="info-item">
            <span>收藏次数</span>
            <span>{{ question.collectCount || 0 }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowDown, Loading } from '@element-plus/icons-vue'
import { questionApi, courseApi } from '../api/study'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const question = ref({
  id: null,
  title: '',
  content: '',
  answer: '',
  analysis: '',
  difficulty: 2,
  viewCount: 0,
  collectCount: 0,
  isViewed: false,
  isCollected: false
})

const courseName = ref('')
const courseId = ref(null)
const progress = ref(0)
const completedQuestions = ref(0)
const totalQuestions = ref(0)
const showAnswer = ref(false)
const allQuestions = ref([])
const currentIndex = ref(-1)

const getDifficultyText = (level) => {
  const texts = { 1: '简单', 2: '中等', 3: '困难' }
  return texts[level] || '中等'
}

const getDifficultyType = (level) => {
  const types = { 1: 'success', 2: 'warning', 3: 'danger' }
  return types[level] || 'info'
}

const formatContent = (text) => {
  if (!text) return ''
  // 简单的换行处理
  return text.replace(/\n/g, '<br>').replace(/```(\w*)\n([\s\S]*?)```/g, '<pre><code>$2</code></pre>')
}

const hasPrev = computed(() => currentIndex.value > 0)

const loadQuestion = async () => {
  loading.value = true
  showAnswer.value = false
  
  try {
    const data = await questionApi.getDetail(route.params.id)
    console.log('题目详情:', data)
    if (data) {
      question.value = data
      courseId.value = data.courseId
      courseName.value = data.courseName || ''
      
      // 加载课程信息和题目列表
      if (data.courseId) {
        await loadCourseInfo(data.courseId)
        await loadAllQuestions(data.courseId)
      }
    }
  } catch (error) {
    console.error('加载题目失败:', error)
    ElMessage.error('加载题目失败')
    router.back()
  } finally {
    loading.value = false
  }
}

const loadCourseInfo = async (cid) => {
  try {
    const data = await courseApi.getDetail(cid)
    if (data) {
      courseName.value = data.courseName
      progress.value = parseFloat(data.progress) || 0
      completedQuestions.value = data.completedQuestions || 0
      totalQuestions.value = data.questionCount || 0
    }
  } catch (e) {
    console.log('加载课程信息失败')
  }
}

const loadAllQuestions = async (cid) => {
  try {
    const data = await questionApi.getByCourse(cid)
    if (Array.isArray(data)) {
      allQuestions.value = data
      currentIndex.value = data.findIndex(q => q.id === parseInt(route.params.id))
    }
  } catch (e) {
    console.log('加载题目列表失败')
  }
}

const toggleCollect = async () => {
  try {
    const result = await questionApi.toggleCollect(question.value.id)
    question.value.isCollected = result
    question.value.collectCount += result ? 1 : -1
    ElMessage.success(result ? '收藏成功' : '取消收藏')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const goPrev = () => {
  if (currentIndex.value > 0) {
    const prevQ = allQuestions.value[currentIndex.value - 1]
    router.push(`/question/${prevQ.id}`)
  }
}

const goNext = () => {
  if (currentIndex.value < allQuestions.value.length - 1) {
    const nextQ = allQuestions.value[currentIndex.value + 1]
    router.push(`/question/${nextQ.id}`)
  } else {
    ElMessage.success('已完成本课程所有题目！')
  }
}

const goBack = () => {
  if (courseId.value) {
    router.push(`/course/${courseId.value}`)
  } else {
    router.push('/courses')
  }
}

onMounted(() => {
  loadQuestion()
})

// 监听路由变化
watch(() => route.params.id, (newId) => {
  if (newId) {
    loadQuestion()
  }
})

import { computed } from 'vue'
</script>

<style scoped>
.question-detail {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.back-nav {
  margin-bottom: 20px;
}

.question-card {
  margin-bottom: 20px;
}

.question-header {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.question-title {
  font-size: 24px;
  color: #303133;
  margin-bottom: 15px;
}

.question-meta {
  display: flex;
  gap: 10px;
}

.question-content {
  margin-bottom: 30px;
}

.question-content h3 {
  color: #303133;
  margin-bottom: 15px;
}

.content-text {
  color: #606266;
  line-height: 1.8;
  font-size: 16px;
}

.content-text :deep(pre) {
  background: #282c34;
  color: #abb2bf;
  padding: 15px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 10px 0;
}

.content-text :deep(code) {
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
}

.answer-section {
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 30px;
}

.answer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  cursor: pointer;
}

.answer-header h3 {
  color: #409EFF;
  margin: 0;
}

.answer-header .el-icon {
  transition: transform 0.3s;
}

.answer-header .el-icon.is-rotate {
  transform: rotate(180deg);
}

.answer-content {
  padding: 0 20px 20px;
}

.answer-title, .analysis-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 15px;
  padding-top: 15px;
}

.analysis-title {
  border-top: 1px dashed #dcdfe6;
}

.action-buttons {
  margin-bottom: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.question-nav {
  display: flex;
  justify-content: space-between;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.sidebar-card {
  margin-bottom: 20px;
}

.sidebar-card h3 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 15px;
}

.course-progress p {
  color: #909399;
  font-size: 14px;
  margin-top: 10px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}

.info-item:last-child {
  border-bottom: none;
}

.loading-container {
  text-align: center;
  padding: 50px;
  color: #909399;
}
</style>
