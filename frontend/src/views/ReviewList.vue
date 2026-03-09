<template>
  <div class="review-list">
    <el-card class="header-card">
      <template #header>
        <div class="card-header">
          <span>📚 错题本 / 复习列表</span>
          <el-tag type="info">共 {{ total }} 道题</el-tag>
        </div>
      </template>
      
      <el-form :inline="true">
        <el-form-item label="筛选课程">
          <el-select v-model="courseId" placeholder="全部课程" clearable @change="loadQuestions">
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.courseName"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>
    
    <div class="question-list" v-loading="loading">
      <el-empty v-if="questions.length === 0 && !loading" description="暂无需要复习的题目" />
      
      <div v-for="(q, index) in questions" :key="q.id" class="question-item">
        <div class="question-header">
          <span class="question-index">{{ (page - 1) * size + index + 1 }}</span>
          <span class="question-title" @click="goToQuestion(q.id)">{{ q.title }}</span>
          <el-tag :type="getDifficultyType(q.difficulty)" size="small">
            {{ getDifficultyText(q.difficulty) }}
          </el-tag>
        </div>
        
        <div class="question-meta">
          <span class="course-name">📖 {{ q.courseName }}</span>
          <span class="view-count">👁️ {{ q.viewCount }} 次浏览</span>
        </div>
        
        <div class="question-content" v-if="q.content">
          {{ truncate(q.content, 150) }}
        </div>
        
        <div class="question-footer">
          <div class="tags">
            <el-tag v-for="tag in q.tags" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
          </div>
          <el-button type="danger" size="small" @click="removeFromReview(q)">
            移出复习
          </el-button>
        </div>
      </div>
    </div>
    
    <div class="pagination" v-if="total > size">
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadQuestions"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { studyApi, courseApi } from '@/api/study'

const router = useRouter()

const loading = ref(false)
const questions = ref([])
const categories = ref([])
const courseId = ref(null)
const page = ref(1)
const size = ref(20)
const total = ref(0)

// 获取所有课程
const courses = computed(() => {
  return categories.value.flatMap(cat => cat.courses || [])
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
    const res = await studyApi.getReviewList(courseId.value, page.value, size.value)
    if (res.data) {
      questions.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

async function removeFromReview(question) {
  try {
    await ElMessageBox.confirm('确定要将此题移出错题本吗？', '提示', {
      type: 'warning'
    })
    const res = await studyApi.toggleReview(question.id)
    question.needReview = false
    ElMessage.success('已移出错题本')
    // 重新加载列表
    await loadQuestions()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
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
.review-list {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.header-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  border-left: 4px solid #f56c6c;
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
  background: #f56c6c;
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

.question-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
  padding-left: 40px;
  font-size: 13px;
  color: #909399;
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

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
