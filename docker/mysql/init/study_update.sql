-- 学习模块数据库更新脚本
-- 添加错题本功能相关字段
-- 创建时间: 2026-03-09

USE user_center;

-- 1. 添加 need_review 字段到 user_study_record 表
ALTER TABLE `user_study_record` 
ADD COLUMN `need_review` TINYINT DEFAULT 0 COMMENT '是否需要复习(0否1是)' AFTER `is_collected`;

-- 2. 添加索引优化查询性能
ALTER TABLE `user_study_record` 
ADD INDEX `idx_need_review` (`user_id`, `need_review`);

-- 3. 更新收藏表，添加复习标记索引
ALTER TABLE `user_study_record`
ADD INDEX `idx_user_collected` (`user_id`, `is_collected`);

-- 4. 添加学习统计表（按日汇总）
CREATE TABLE IF NOT EXISTS `user_study_stats` (
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

-- 5. 添加课程学习进度索引
ALTER TABLE `user_course_progress`
ADD INDEX `idx_last_study` (`user_id`, `last_study_time`);
