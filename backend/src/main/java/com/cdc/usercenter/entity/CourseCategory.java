package com.cdc.usercenter.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 课程分类实体
 */
@Data
@TableName("course_category")
public class CourseCategory {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String categoryName;
    
    private String categoryCode;
    
    private String icon;
    
    private Integer sort;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
