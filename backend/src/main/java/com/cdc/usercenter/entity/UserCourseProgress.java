package com.cdc.usercenter.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户课程进度实体
 */
@Data
@TableName("user_course_progress")
public class UserCourseProgress {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long courseId;
    
    private Integer totalQuestions;
    
    private Integer completedQuestions;
    
    private BigDecimal progress;
    
    private LocalDateTime lastStudyTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
