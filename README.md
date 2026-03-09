# 用户中心系统

基于 Spring Boot 3 + MySQL + Redis 的用户中心服务

## 🛠️ 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.5 | 后端框架 |
| MySQL | 8.0 | 数据存储 |
| Redis | 7.x | 缓存/Session |
| MyBatis Plus | 3.5.5 | ORM框架 |
| JWT | 0.12.5 | Token认证 |
| Vue3 + Element Plus | - | 前端框架 |

## 📁 项目结构

```
user-center/
├── backend/                    # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/cdc/usercenter/
│   │   │   │   ├── config/         # 配置类
│   │   │   │   ├── controller/     # 控制器
│   │   │   │   ├── service/        # 服务层
│   │   │   │   ├── mapper/         # 数据访问层
│   │   │   │   ├── entity/         # 实体类
│   │   │   │   ├── dto/            # 数据传输对象
│   │   │   │   ├── vo/             # 视图对象
│   │   │   │   ├── security/       # 安全相关
│   │   │   │   └── common/         # 公共类
│   │   │   └── resources/
│   │   │       └── application.yml # 配置文件
│   │   └── test/
│   └── pom.xml
├── frontend/                   # 前端项目
│   └── src/
│       ├── views/              # 页面
│       ├── components/         # 组件
│       ├── api/                # API接口
│       ├── router/             # 路由
│       └── store/              # 状态管理
├── docker/                     # Docker配置
│   ├── mysql/init/             # MySQL初始化脚本
│   ├── redis/                  # Redis配置
│   └── es/                     # ES配置(可选)
└── docker-compose.yml          # Docker编排文件
```

## 🚀 快速开始

### 1. 启动基础设施

```bash
# 进入项目目录
cd user-center

# 启动 MySQL + Redis
docker compose up -d mysql redis

# 查看服务状态
docker compose ps
```

### 2. 启动后端服务

```bash
cd backend
mvn spring-boot:run
```

### 3. 启动前端服务

```bash
cd frontend
npm install
npm run dev
```

## 📊 数据库设计

### 核心表

| 表名 | 说明 |
|------|------|
| sys_user | 用户表 |
| sys_oauth | 第三方登录绑定表 |
| sys_role | 角色表 |
| sys_permission | 权限表 |
| sys_user_role | 用户角色关联表 |
| sys_role_permission | 角色权限关联表 |
| sys_login_log | 登录日志表 |

## 🔌 API接口

### 用户模块

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/v1/user/register | 用户注册 |
| POST | /api/v1/user/login | 用户登录 |
| POST | /api/v1/user/logout | 用户登出 |
| GET | /api/v1/user/info | 获取用户信息 |
| PUT | /api/v1/user/info | 更新用户信息 |

### OAuth模块

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/oauth/github | GitHub登录 |
| GET | /api/v1/oauth/github/callback | GitHub回调 |
| GET | /api/v1/oauth/wechat | 微信登录 |
| GET | /api/v1/oauth/qq | QQ登录 |

## ⚙️ 配置说明

### 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| MYSQL_HOST | MySQL地址 | localhost |
| MYSQL_PASSWORD | MySQL密码 | root123456 |
| REDIS_HOST | Redis地址 | localhost |
| REDIS_PASSWORD | Redis密码 | redis123456 |

### OAuth配置

需要在各平台申请应用并配置：

1. **GitHub**: https://github.com/settings/developers
2. **微信**: https://open.weixin.qq.com (需企业资质)
3. **QQ**: https://connect.qq.com (需企业资质)

## 📝 开发进度

- [x] 项目结构搭建
- [x] Docker环境配置
- [x] 数据库设计
- [x] 后端代码开发
- [x] 前端页面开发
- [x] 学习模块开发
- [ ] 测试与部署

## 📖 学习模块

### 功能列表

| 功能 | 说明 | 状态 |
|------|------|------|
| 课程浏览 | 按分类浏览课程 | ✅ |
| 题目学习 | 查看题目、答案、解析 | ✅ |
| 随机刷题 | 按条件随机抽题练习 | ✅ |
| 收藏题目 | 收藏重点题目 | ✅ |
| 错题本 | 标记需要复习的题目 | ✅ |
| 学习进度 | 跟踪课程学习进度 | ✅ |
| 学习统计 | 每日/每周学习数据 | ✅ |
| 继续学习 | 从上次位置继续 | ✅ |

### 学习模块API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /v1/course/categories | 获取课程分类列表 |
| GET | /v1/course/{id} | 获取课程详情 |
| GET | /v1/question/course/{courseId} | 获取课程题目列表 |
| GET | /v1/question/{id} | 获取题目详情 |
| POST | /v1/question/{id}/collect | 收藏/取消收藏 |
| GET | /v1/study/stats | 获取学习统计 |
| GET | /v1/study/progress | 获取学习进度 |
| GET | /v1/study/random | 随机刷题 |
| GET | /v1/study/search | 搜索题目 |
| POST | /v1/study/{id}/review | 加入/移出错题本 |
| GET | /v1/study/review | 获取错题本列表 |
| POST | /v1/study/time | 记录学习时长 |

### 学习模块数据库表

| 表名 | 说明 |
|------|------|
| course_category | 课程分类表 |
| course | 课程表 |
| question | 题目表 |
| user_study_record | 用户学习记录表 |
| user_course_progress | 用户课程进度表 |
| user_study_stats | 用户学习统计表 |

## 🚀 快速体验

```bash
# 1. 启动基础设施
docker compose up -d mysql redis

# 2. 启动后端
cd backend && java -jar target/user-center-1.0.0.jar

# 3. 启动前端
cd frontend && npm run dev
```

访问: http://localhost:5173

## 📚 文档

- [总体设计文档](docs/design.md)

---

🤖 由 OpenClaw 管理
