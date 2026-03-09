<template>
  <div class="random-practice">
    <el-card class="filter-card">
      <template #header>
        <div class="card-header">
          <span>🎲 随机刷题</span>
          <el-button type="primary" @click="loadQuestions">
            <el-icon><Refresh /></el-icon>
            换一批
          </el-button>
        </div>
      </template>
      
      <el-form :inline="true" class="filter-form">
        <el-form-item label="分类">
          <el-select v-model="filters.categoryId" placeholder="全部分类" clearable style="width: 150px">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.categoryName"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="课程">
          <el-select v-model="filters.courseId" placeholder="全部课程" clearable style="width: 200px">
            <el-option
              v-for="course in courseList"
              :key="course.id"
              :label="course.courseName"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="难度">
          <el-select v-model="filters.difficulty" placeholder="全部难度" clearable style="width: 120px">
            <el-option label="简单" :value="1" />
            <el-option label="中等" :value="2" />
            <el-option label="困难" :value="3" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="数量">
          <el-select v-model="filters.count" style="width: 100px">
            <el-option label="10题" :value="10" />
            <el-option label="20题" :value="20" />
            <el-option label="30题" :value="30" />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>
    
    <div class="question-list" v-loading="loading">
      <el-empty v-if="questions.length === 0 && !loading" description="暂无题目，换个条件试试" />
      
      <div v-for="(q, index) in questions" :key="q.id" class="question-item">
        <div class="question-header">
          <span class="question-index">{{ index + 1 }}</span>
          <span class="question-title" @click="goToQuestion(q.id)">{{ q.title }}</span>
          <el-tag :type="getDifficultyType(q.difficulty)" size="small">
            {{ getDifficultyText(q.difficulty) }}
          </el-tag>
          <span class="course-name">{{ q.courseName }}</span>
        </div>
        
        <div class="question-content" v-if="q.content">
          {{ truncate(q.content, 100) }}
        </div>
        
        <div class="question-footer">
          <div class="tags">
            <el-tag v-for="tag in q.tags" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
          </div>
          <div class="actions">
            <el-button 
              :type="q.isCollected ? 'warning' : 'default'" 
              size="small"
              @click="toggleCollect(q)"
            >
              {{ q.isCollected ? '已收藏' : '收藏' }}
            </el-button>
            <el-button 
              :type="q.needReview ? 'danger' : 'default'" 
              size="small"
              @click="toggleReview(q)"
            >
              {{ q.needReview ? '已加入复习' : '加入复习' }}
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { courseApi, studyApi, questionApi } from '@/api/study'

const router = useRouter()

const loading = ref(false)
const categories = ref([])
const questions = ref([])

const filters = ref({
  categoryId: null,
  courseId: null,
  difficulty: null,
  count: 10
})

// 获取所有课程列表
const courseList = computed(() => {
  if (!filters.value.categoryId) {
    return categories.value.flatMap(cat => cat.courses || [])
  }
  const cat = categories.value.find(c => c.id === filters.value.categoryId)
  return cat?.courses || []
})

// 监听分类变化，清空课程选择
watch(() => filters.value.categoryId, () => {
  filters.value.courseId = null
})

onMounted(async () => {
  await loadCategories()
  await loadQuestions()
})

async function loadCategories() {
  try {
    const res = await courseApi.getCategories()
    if (res.data) {
      categories.value = res.data
    }
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

async function loadQuestions() {
  loading.value = true
  try {
    const params = {
      count: filters.value.count
    }
    if (filters.value.categoryId) params.categoryId = filters.value.categoryId
    if (filters.value.courseId) params.courseId = filters.value.courseId
    if (filters.value.difficulty) params.difficulty = filters.value.difficulty
    
    const res = await studyApi.getRandomQuestions(params)
    if (res.data) {
      questions.value = res.data
    }
  } catch (e) {
    ElMessage.error('加载题目失败')
  } finally {
    loading.value = false
  }
}

async function toggleCollect(question) {
  try {
    const res = await questionApi.toggleCollect(question.id)
    question.isCollected = res.data
    ElMessage.success(res.message)
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

async function toggleReview(question) {
  try {
    const res = await studyApi.toggleReview(question.id)
    question.needReview = res.data
    ElMessage.success(res.message)
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

function goToQuestion(id) {
  router.push(`/question/${id}`)
}

function getDifficultyType(d) {
  const types = { 1: 'success', 2: 'warning', 3: 'danger' }
  return types[d] || 'info'
}

function getDifficultyText(d) {
  const texts = { 1: '简单', 2: '中等', 3: '困难' }
  return texts[d] || '未知'
}

function truncate(str, len) {
  if (!str) return ''
  return str.length > len ? str.substring(0, len) + '...' : str
}
</script>

<style scoped>
.random-practice {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.filter-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-form {
  margin-bottom: 0;
}

.question-list {
  min-height: 300px;
}

.question-item {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  transition: all 0.3s;
}

.question-item:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.question-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.question-index {
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 500;
}

.question-title {
  flex: 1;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  color: #333;
}

.question-title:hover {
  color: #409eff;
}

.course-name {
  color: #909399;
  font-size: 13px;
}

.question-content {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 12px;
  padding-left: 40px;
}

.question-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-left: 40px;
}

.tags {
  display: flex;
  gap: 6px;
}

.actions {
  display: flex;
  gap: 8px;
}
</style>
