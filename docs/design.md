# 用户中心系统 - 总体设计文档

## 1. 系统概述

用户中心是一个基于 Spring Boot 3 + Vue3 的学习平台，提供用户管理、OAuth 登录、课程学习、题目练习等核心功能。

## 2. 技术架构

### 2.1 技术栈

| 层级 | 技术选型 | 说明 |
|------|----------|------|
| 前端框架 | Vue3 + Element Plus | 现代化 UI 组件库 |
| 构建工具 | Vite | 快速开发构建 |
| 后端框架 | Spring Boot 3.2.5 | 最新 LTS 版本 |
| ORM | MyBatis Plus 3.5.5 | 简化 CRUD 操作 |
| 数据库 | MySQL 8.0 | 主数据存储 |
| 缓存 | Redis 7.x | Session/Token 缓存 |
| 认证 | JWT (jjwt 0.12.5) | 无状态 Token 认证 |

### 2.2 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        前端层 (Vue3)                         │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │ 登录注册  │ │ 用户中心  │ │ 课程列表  │ │ 题目练习  │       │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
└─────────────────────────┬───────────────────────────────────┘
                          │ HTTP/REST API
┌─────────────────────────┴───────────────────────────────────┐
│                     后端层 (Spring Boot 3)                   │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │  User    │ │  OAuth   │ │  Course  │ │  Study   │       │
│  │Controller│ │Controller│ │Controller│ │Controller│       │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘       │
│       │            │            │            │              │
│  ┌────┴─────┐ ┌────┴─────┐ ┌────┴─────┐ ┌────┴─────┐       │
│  │  User    │ │  OAuth   │ │  Course  │ │  Study   │       │
│  │ Service  │ │ Service  │ │ Service  │ │ Service  │       │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘       │
│       │            │            │            │              │
│  ┌────┴────────────┴────────────┴────────────┴─────┐       │
│  │              Security (JWT + Redis)             │       │
│  └─────────────────────────────────────────────────┘       │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────┴───────────────────────────────────┐
│                        数据层                                │
│  ┌─────────────────────┐  ┌─────────────────────┐          │
│  │      MySQL 8.0      │  │      Redis 7.x      │          │
│  │  用户/课程/题目数据   │  │  Token/Session 缓存  │          │
│  └─────────────────────┘  └─────────────────────┘          │
└─────────────────────────────────────────────────────────────┘
```

## 3. 模块设计

### 3.1 用户模块 (User)

**功能职责：**
- 用户注册/登录/登出
- 用户信息查询/更新
- 登录日志记录

**API 设计：**

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /v1/user/register | 用户注册 | 否 |
| POST | /v1/user/login | 用户登录 | 否 |
| POST | /v1/user/logout | 用户登出 | 是 |
| GET | /v1/user/info | 获取用户信息 | 是 |
| PUT | /v1/user/info | 更新用户信息 | 是 |

### 3.2 OAuth 模块

**功能职责：**
- 第三方登录授权 (GitHub/微信/QQ)
- OAuth 用户绑定/解绑

**API 设计：**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /v1/oauth/github | GitHub 登录 |
| GET | /v1/oauth/github/callback | GitHub 回调 |
| GET | /v1/oauth/wechat | 微信登录 |
| GET | /v1/oauth/qq | QQ 登录 |

### 3.3 课程模块 (Course)

**功能职责：**
- 课程分类管理
- 课程列表/详情查询
- 用户学习进度追踪

**API 设计：**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /v1/course/categories | 获取所有分类及课程 |
| GET | /v1/course/{id} | 获取课程详情 |

### 3.4 学习模块 (Study)

**功能职责：**
- 学习统计 (总题数/今日学习/学习时长)
- 学习进度管理
- 题目收藏功能

**API 设计：**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /v1/study/stats | 获取学习统计 |
| GET | /v1/study/progress | 获取学习进度列表 |

### 3.5 题目模块 (Question)

**功能职责：**
- 题目列表/详情查询
- 题目收藏/取消收藏
- 收藏列表查询

**API 设计：**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /v1/question/course/{courseId} | 获取课程题目列表 |
| GET | /v1/question/{id} | 获取题目详情 |
| POST | /v1/question/{id}/collect | 收藏/取消收藏 |
| GET | /v1/question/collects | 获取收藏列表 |

## 4. 数据库设计

### 4.1 核心表结构

```
┌─────────────────┐     ┌─────────────────┐
│    sys_user     │     │   sys_oauth     │
├─────────────────┤     ├─────────────────┤
│ id (PK)         │◄────│ user_id (FK)    │
│ username        │     │ platform        │
│ password        │     │ openid          │
│ nickname        │     │ nickname        │
│ avatar          │     └─────────────────┘
│ status          │
└─────────────────┘

┌─────────────────┐     ┌─────────────────┐
│ course_category │     │     course      │
├─────────────────┤     ├─────────────────┤
│ id (PK)         │◄────│ category_id(FK) │
│ category_name   │     │ course_name     │
│ category_code   │     │ description     │
│ sort            │     │ difficulty      │
└─────────────────┘     │ question_count  │
                        └────────┬────────┘
                                 │
┌─────────────────┐              │
│    question     │              │
├─────────────────┤              │
│ id (PK)         │◄─────────────┘
│ course_id (FK)  │
│ title           │     ┌─────────────────┐
│ content         │     │user_study_record│
│ answer          │     ├─────────────────┤
│ analysis        │     │ user_id (FK)    │
│ difficulty      │     │ course_id (FK)  │
│ tags            │     │ question_id(FK) │
└─────────────────┘     │ is_viewed       │
                        │ is_collected    │
┌─────────────────┐     └─────────────────┘
│user_course_     │
│    progress     │
├─────────────────┤
│ user_id (FK)    │
│ course_id (FK)  │
│ progress        │
│ completed_quests│
└─────────────────┘
```

### 4.2 表清单

| 表名 | 说明 | 数据量预估 |
|------|------|-----------|
| sys_user | 用户表 | 10万+ |
| sys_oauth | OAuth绑定表 | 10万+ |
| sys_role | 角色表 | <100 |
| sys_permission | 权限表 | <1000 |
| sys_user_role | 用户角色关联 | 10万+ |
| sys_role_permission | 角色权限关联 | 1万+ |
| sys_login_log | 登录日志 | 100万+ |
| course_category | 课程分类 | <100 |
| course | 课程表 | 1000+ |
| question | 题目表 | 10万+ |
| user_course_progress | 用户课程进度 | 100万+ |
| user_study_record | 用户学习记录 | 1000万+ |

## 5. 安全设计

### 5.1 认证流程

```
┌────────┐     ┌────────┐     ┌────────┐
│ Client │     │ Server │     │ Redis  │
└───┬────┘     └───┬────┘     └───┬────┘
    │  1.登录请求    │              │
    │──────────────►│              │
    │               │ 2.验证密码    │
    │               │──────────►   │
    │               │              │
    │               │ 3.生成JWT    │
    │               │──────────►   │ 4.存储RefreshToken
    │               │              │◄───────
    │  5.返回Token  │              │
    │◄──────────────│              │
    │               │              │
    │  6.请求API    │              │
    │──────────────►│              │
    │               │ 7.验证JWT    │
    │               │──────────►   │
    │  8.返回数据   │              │
    │◄──────────────│              │
```

### 5.2 Token 设计

| Token 类型 | 有效期 | 存储位置 | 用途 |
|-----------|--------|---------|------|
| Access Token | 2小时 | 客户端 | API 认证 |
| Refresh Token | 7天 | Redis + 客户端 | 刷新 Access Token |

### 5.3 权限控制

- 使用 Spring Security + JWT 进行认证
- 基于角色的权限控制 (RBAC)
- 接口级别的权限注解

## 6. 前端设计

### 6.1 页面结构

```
/                    → 用户中心 (需登录)
/login               → 登录页
/register            → 注册页
/courses             → 课程列表
/course/:id          → 课程详情
/question/:id        → 题目详情
```

### 6.2 组件设计

```
src/
├── api/              # API 接口层
│   ├── index.js      # 用户 API
│   └── study.js      # 学习相关 API
├── views/            # 页面组件
│   ├── Login.vue     # 登录页
│   ├── Register.vue  # 注册页
│   ├── UserCenter.vue # 用户中心
│   ├── CourseList.vue # 课程列表
│   ├── CourseDetail.vue # 课程详情
│   └── QuestionDetail.vue # 题目详情
├── router/           # 路由配置
└── App.vue           # 根组件
```

## 7. 部署架构

### 7.1 容器化部署

```yaml
services:
  mysql:
    image: mysql:8.0
    ports: ["3306:3306"]
    
  redis:
    image: redis:7
    ports: ["6379:6379"]
    
  backend:
    build: ./backend
    ports: ["8080:8080"]
    depends_on: [mysql, redis]
    
  frontend:
    build: ./frontend
    ports: ["80:80"]
    depends_on: [backend]
```

### 7.2 生产环境建议

- 使用 Nginx 反向代理
- MySQL 主从复制
- Redis Sentinel 高可用
- 后端多实例 + 负载均衡

## 8. 开发进度

- [x] 项目结构搭建
- [x] Docker 环境配置
- [x] 数据库设计
- [x] 后端代码开发
  - [x] 用户模块
  - [x] OAuth 模块
  - [x] 课程模块
  - [x] 学习模块
  - [x] 题目模块
- [x] 前端页面开发
  - [x] 登录/注册页
  - [x] 用户中心页
  - [x] 课程列表/详情
  - [x] 题目详情页
- [x] 测试与部署

## 9. 后续规划

- [ ] 微信/QQ OAuth 接入 (需企业资质)
- [ ] 管理后台
- [ ] 题目录入系统
- [ ] 学习数据分析
- [ ] 移动端适配

---

**文档版本:** v1.0  
**更新时间:** 2026-03-09  
**维护者:** OpenClaw
