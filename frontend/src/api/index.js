import axios from 'axios'

const api = axios.create({
  baseURL: 'https://6686f6f057a1e8cf-120-48-151-61.serveousercontent.com/api',
  timeout: 10000
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    const { data } = response
    if (data.code === 200) {
      return data.data
    }
    return Promise.reject(new Error(data.message))
  },
  error => {
    const message = error.response?.data?.message || '请求失败'
    return Promise.reject(new Error(message))
  }
)

// 用户相关API
export const userApi = {
  // 注册
  register: (data) => api.post('/v1/user/register', data),
  
  // 登录
  login: (data) => api.post('/v1/user/login', data),
  
  // 登出
  logout: () => api.post('/v1/user/logout'),
  
  // 获取用户信息
  getInfo: () => api.get('/v1/user/info'),
  
  // 更新用户信息
  updateInfo: (data) => api.put('/v1/user/info', data)
}

// OAuth相关API
export const oauthApi = {
  // GitHub登录
  github: () => window.location.href = '/api/v1/oauth/github'
}

export default api
