package com.cdc.usercenter.vo;

import lombok.Data;

/**
 * 题目VO
 */
@Data
public class QuestionVO {
    private Long id;
    private Long courseId;
    private String courseName;
    private String title;
    private String content;
    private String answer;
    private String analysis;
    private Integer difficulty;
    private String[] tags;
    private Integer viewCount;
    private Integer collectCount;
    
    // 用户相关
    private Boolean isViewed;
    private Boolean isCollected;
    
    // 导航
    private Long prevId;
    private Long nextId;
}
