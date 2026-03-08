package com.cdc.usercenter.vo;

import lombok.Data;

/**
 * 学习统计VO
 */
@Data
public class StudyStatsVO {
    // 今日统计
    private Integer todayTime;
    private Integer todayQuestions;
    
    // 总计统计
    private Integer totalTime;
    private Integer totalQuestions;
    private Integer totalCourses;
    private Integer completedCourses;
}
