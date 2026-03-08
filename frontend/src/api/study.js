import request from './request'

// 课程API
export const courseApi = {
  // 获取所有分类及课程
  getCategories() {
    return request.get('/v1/course/categories')
  },

  // 获取课程详情
  getDetail(courseId) {
    return request.get(`/v1/course/${courseId}`)
  }
}

// 题目API
export const questionApi = {
  // 获取课程下的题目列表
  getByCourse(courseId, page = 1, size = 20) {
    return request.get(`/v1/question/course/${courseId}`, {
      params: { page, size }
    })
  },

  // 获取题目详情
  getDetail(questionId) {
    return request.get(`/v1/question/${questionId}`)
  },

  // 收藏/取消收藏
  toggleCollect(questionId) {
    return request.post(`/v1/question/${questionId}/collect`)
  },

  // 获取收藏列表
  getCollects(page = 1, size = 20) {
    return request.get('/v1/question/collects', {
      params: { page, size }
    })
  }
}

// 学习记录API
export const studyApi = {
  // 获取学习统计
  getStats() {
    return request.get('/v1/study/stats')
  },

  // 获取学习进度
  getProgress() {
    return request.get('/v1/study/progress')
  }
}
