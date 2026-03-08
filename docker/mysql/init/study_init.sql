-- 学习模块数据库初始化脚本
-- 创建时间: 2026-03-08

-- ----------------------------
-- 课程分类表
-- ----------------------------
DROP TABLE IF EXISTS `course_category`;
CREATE TABLE `course_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `category_code` VARCHAR(50) NOT NULL COMMENT '分类编码',
    `icon` VARCHAR(100) DEFAULT NULL COMMENT '图标',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态(0禁用1正常)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_code` (`category_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程分类表';

-- ----------------------------
-- 课程表
-- ----------------------------
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '课程ID',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `course_name` VARCHAR(100) NOT NULL COMMENT '课程名称',
    `course_code` VARCHAR(50) DEFAULT NULL COMMENT '课程编码',
    `description` TEXT COMMENT '课程描述',
    `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图',
    `difficulty` TINYINT DEFAULT 1 COMMENT '难度(1入门2进阶3高级)',
    `question_count` INT DEFAULT 0 COMMENT '题目数量',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态(0下架1上架)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

-- ----------------------------
-- 题目表
-- ----------------------------
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '题目ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `title` VARCHAR(255) NOT NULL COMMENT '题目标题',
    `content` TEXT COMMENT '题目内容(详细描述)',
    `answer` TEXT COMMENT '答案',
    `analysis` TEXT COMMENT '解析',
    `difficulty` TINYINT DEFAULT 2 COMMENT '难度(1简单2中等3困难)',
    `tags` VARCHAR(255) DEFAULT NULL COMMENT '标签(逗号分隔)',
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `collect_count` INT DEFAULT 0 COMMENT '收藏次数',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态(0禁用1正常)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_course_id` (`course_id`),
    KEY `idx_difficulty` (`difficulty`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目表';

-- ----------------------------
-- 用户学习记录表
-- ----------------------------
DROP TABLE IF EXISTS `user_study_record`;
CREATE TABLE `user_study_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `is_viewed` TINYINT DEFAULT 0 COMMENT '是否已查看(0否1是)',
    `is_collected` TINYINT DEFAULT 0 COMMENT '是否收藏(0否1是)',
    `study_time` INT DEFAULT 0 COMMENT '学习时长(秒)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_question` (`user_id`, `question_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户学习记录表';

-- ----------------------------
-- 用户课程进度表
-- ----------------------------
DROP TABLE IF EXISTS `user_course_progress`;
CREATE TABLE `user_course_progress` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '进度ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `total_questions` INT DEFAULT 0 COMMENT '总题数',
    `completed_questions` INT DEFAULT 0 COMMENT '已完成题数',
    `progress` DECIMAL(5,2) DEFAULT 0.00 COMMENT '进度百分比',
    `last_study_time` DATETIME DEFAULT NULL COMMENT '最后学习时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_course` (`user_id`, `course_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户课程进度表';

-- ----------------------------
-- 用户学习统计表(按日)
-- ----------------------------
DROP TABLE IF EXISTS `user_study_stats`;
CREATE TABLE `user_study_stats` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '统计ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `study_date` DATE NOT NULL COMMENT '学习日期',
    `study_time` INT DEFAULT 0 COMMENT '学习时长(秒)',
    `question_count` INT DEFAULT 0 COMMENT '答题数量',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `study_date`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_study_date` (`study_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户学习统计表';

-- ----------------------------
-- 初始化分类数据
-- ----------------------------
INSERT INTO `course_category` (`category_name`, `category_code`, `icon`, `sort`) VALUES
('Java基础', 'java-basic', 'java', 1),
('Java并发', 'java-concurrent', 'concurrent', 2),
('JVM', 'java-jvm', 'jvm', 3),
('Spring框架', 'spring', 'spring', 4),
('MySQL数据库', 'mysql', 'mysql', 5),
('Redis缓存', 'redis', 'redis', 6),
('分布式', 'distributed', 'distributed', 7);

-- ----------------------------
-- 初始化课程数据
-- ----------------------------
INSERT INTO `course` (`category_id`, `course_name`, `course_code`, `description`, `difficulty`) VALUES
-- Java基础
(1, 'Java集合框架', 'java-collection', '深入学习Java集合框架，包括List、Set、Map等常用集合类的原理和使用场景', 2),
(1, 'Java面向对象', 'java-oop', '掌握Java面向对象编程的核心概念：封装、继承、多态', 1),
(1, 'Java异常处理', 'java-exception', '理解Java异常体系，掌握最佳实践', 1),

-- Java并发
(2, 'Java线程基础', 'java-thread', '掌握Java线程的基本概念和使用方法', 2),
(2, 'Java锁机制', 'java-lock', '深入理解synchronized、ReentrantLock等锁机制', 3),
(2, 'Java线程池', 'java-threadpool', '掌握线程池的原理和最佳实践', 3),
(2, 'Java并发容器', 'java-concurrent-collection', '学习ConcurrentHashMap等并发容器的实现原理', 3),

-- JVM
(3, 'JVM内存模型', 'jvm-memory', '理解JVM内存结构，包括堆、栈、方法区等', 3),
(3, 'JVM垃圾回收', 'jvm-gc', '掌握垃圾回收算法和常见垃圾收集器', 3),
(3, 'JVM类加载机制', 'jvm-classloader', '理解类的加载、链接、初始化过程', 3),

-- Spring
(4, 'Spring核心原理', 'spring-core', '深入理解IOC和AOP的核心原理', 2),
(4, 'SpringBoot实战', 'springboot', '掌握SpringBoot自动配置原理和最佳实践', 2),
(4, 'Spring事务管理', 'spring-transaction', '理解Spring事务传播机制和实现原理', 3),

-- MySQL
(5, 'MySQL索引优化', 'mysql-index', '掌握MySQL索引原理和优化技巧', 3),
(5, 'MySQL事务与锁', 'mysql-transaction', '深入理解MySQL事务隔离级别和锁机制', 3),
(5, 'MySQL性能调优', 'mysql-performance', '学习SQL优化和数据库调优技巧', 3),

-- Redis
(6, 'Redis基础', 'redis-basic', '掌握Redis常用数据类型和基本命令', 2),
(6, 'Redis持久化', 'redis-persistence', '理解RDB和AOF持久化机制', 2),
(6, 'Redis集群', 'redis-cluster', '学习Redis主从复制、哨兵、集群架构', 3),

-- 分布式
(7, '分布式理论', 'distributed-theory', '理解CAP、BASE等分布式理论基础', 2),
(7, '消息队列', 'mq', '掌握消息队列的使用场景和常见问题', 2),
(7, '分布式锁', 'distributed-lock', '学习分布式锁的实现方案', 3);

-- 更新课程题目数量(初始为0，后续会更新)
UPDATE `course` SET `question_count` = 0;
