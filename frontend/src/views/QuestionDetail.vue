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
          <!-- 题目标题 -->
          <div class="question-header">
            <h1 class="question-title">{{ question.title }}</h1>
            <div class="question-meta">
              <el-tag :type="getDifficultyType(question.difficulty)" size="small">
                {{ getDifficultyText(question.difficulty) }}
              </el-tag>
              <el-tag v-for="tag in question.tags" :key="tag" size="small" effect="plain">
                {{ tag }}
              </el-tag>
            </div>
          </div>

          <!-- 题目内容 -->
          <div class="question-content">
            <h3>📝 题目描述</h3>
            <p>{{ question.content }}</p>
          </div>

          <!-- 答案区域 -->
          <div class="answer-section">
            <div class="answer-header" @click="showAnswer = !showAnswer">
              <h3>💡 查看答案</h3>
              <el-icon :class="{ 'is-rotate': showAnswer }"><ArrowDown /></el-icon>
            </div>
            <el-collapse-transition>
              <div v-show="showAnswer" class="answer-content">
                <div class="answer-title">答案</div>
                <div class="markdown-body" v-html="renderMarkdown(question.answer)"></div>
                
                <div class="analysis-title">解析</div>
                <div class="markdown-body" v-html="renderMarkdown(question.analysis)"></div>
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
            <el-button 
              v-if="question.prevId" 
              @click="goToQuestion(question.prevId)"
              :disabled="!question.prevId"
            >
              ← 上一题
            </el-button>
            <div v-else></div>
            <el-button 
              v-if="question.nextId" 
              @click="goToQuestion(question.nextId)"
              type="primary"
            >
              下一题 →
            </el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 侧边栏 -->
      <el-col :span="6">
        <el-card class="sidebar-card">
          <h3>📖 {{ courseName }}</h3>
          <div class="course-progress">
            <el-progress :percentage="progress" :stroke-width="8" />
            <p>已完成 {{ completedQuestions }}/{{ totalQuestions }} 题</p>
          </div>
        </el-card>

        <el-card class="sidebar-card">
          <h3>📊 本题信息</h3>
          <div class="info-item">
            <span>浏览次数</span>
            <span>{{ question.viewCount }}</span>
          </div>
          <div class="info-item">
            <span>收藏次数</span>
            <span>{{ question.collectCount }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowDown } from '@element-plus/icons-vue'
import { questionApi, courseApi } from '../api/study'
import { marked } from 'marked'

const route = useRoute()
const router = useRouter()

const question = ref({
  id: null,
  title: '',
  content: '',
  answer: '',
  analysis: '',
  difficulty: 2,
  tags: [],
  viewCount: 0,
  collectCount: 0,
  isViewed: false,
  isCollected: false,
  prevId: null,
  nextId: null
})

const courseName = ref('')
const progress = ref(0)
const completedQuestions = ref(0)
const totalQuestions = ref(0)
const showAnswer = ref(false)

const getDifficultyText = (level) => {
  const texts = { 1: '简单', 2: '中等', 3: '困难' }
  return texts[level] || '未知'
}

const getDifficultyType = (level) => {
  const types = { 1: 'success', 2: 'warning', 3: 'danger' }
  return types[level] || 'info'
}

const renderMarkdown = (text) => {
  if (!text) return ''
  return marked(text)
}

const loadQuestion = async () => {
  try {
    const res = await questionApi.getDetail(route.params.id)
    if (res.data.code === 200) {
      question.value = res.data.data
      courseName.value = res.data.data.courseName
      
      // 加载课程进度
      if (res.data.data.courseId) {
        const courseRes = await courseApi.getDetail(res.data.data.courseId)
        if (courseRes.data.code === 200) {
          progress.value = parseFloat(courseRes.data.data.progress) || 0
          completedQuestions.value = courseRes.data.data.completedQuestions || 0
          totalQuestions.value = courseRes.data.data.questionCount || 0
        }
      }
      
      // 重置答案显示状态
      showAnswer.value = false
    } else {
      ElMessage.error('题目不存在')
      router.back()
    }
  } catch (error) {
    ElMessage.error('加载失败')
    router.back()
  }
}

const toggleCollect = async () => {
  try {
    const res = await questionApi.toggleCollect(question.value.id)
    if (res.data.code === 200) {
      question.value.isCollected = res.data.data
      question.value.collectCount += res.data.data ? 1 : -1
      ElMessage.success(res.data.data ? '收藏成功' : '取消收藏')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const goToQuestion = (id) => {
  router.push(`/question/${id}`)
}

const goBack = () => {
  if (question.value.courseId) {
    router.push(`/course/${question.value.courseId}`)
  } else {
    router.push('/courses')
  }
}

onMounted(() => {
  loadQuestion()
})

// 监听路由变化
router.afterEach((to) => {
  if (to.params.id && to.name === 'question') {
    loadQuestion()
  }
})
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

.question-content p {
  color: #606266;
  line-height: 1.8;
  font-size: 16px;
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

.markdown-body {
  line-height: 1.8;
  color: #606266;
}

.markdown-body :deep(h1), .markdown-body :deep(h2), .markdown-body :deep(h3) {
  color: #303133;
  margin: 20px 0 10px;
}

.markdown-body :deep(code) {
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
}

.markdown-body :deep(pre) {
  background: #282c34;
  color: #abb2bf;
  padding: 15px;
  border-radius: 8px;
  overflow-x: auto;
}

.markdown-body :deep(pre code) {
  background: transparent;
  padding: 0;
}

.markdown-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 15px 0;
}

.markdown-body :deep(th), .markdown-body :deep(td) {
  border: 1px solid #dcdfe6;
  padding: 10px;
  text-align: left;
}

.markdown-body :deep(th) {
  background: #f5f7fa;
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
</style>
