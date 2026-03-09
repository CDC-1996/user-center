package com.cdc.usercenter.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户学习记录实体
 */
@Data
@TableName("user_study_record")
public class UserStudyRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long courseId;
    
    private Long questionId;
    
    private Integer isViewed;
    
    private Integer isCollected;
    
    private Integer needReview;  // 是否需要复习（错题本）
    
    private Integer studyTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
