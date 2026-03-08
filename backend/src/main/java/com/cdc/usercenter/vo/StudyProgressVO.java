package com.cdc.usercenter.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学习进度VO
 */
@Data
public class StudyProgressVO {
    private Long courseId;
    private String courseName;
    private String categoryName;
    private Integer totalQuestions;
    private Integer completedQuestions;
    private BigDecimal progress;
    private LocalDateTime lastStudyTime;
}
