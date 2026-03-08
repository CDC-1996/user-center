package com.cdc.usercenter.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 课程VO
 */
@Data
public class CourseVO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String courseName;
    private String courseCode;
    private String description;
    private String coverImage;
    private Integer difficulty;
    private Integer questionCount;
    
    // 用户相关
    private BigDecimal progress;
    private Integer completedQuestions;
    private Boolean isStarted;
}
