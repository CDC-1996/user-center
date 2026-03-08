package com.cdc.usercenter.vo;

import lombok.Data;
import java.util.List;

/**
 * 课程分类VO
 */
@Data
public class CategoryVO {
    private Long id;
    private String categoryName;
    private String categoryCode;
    private String icon;
    private Integer sort;
    private List<CourseVO> courses;
}
