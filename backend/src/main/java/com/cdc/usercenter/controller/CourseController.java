package com.cdc.usercenter.controller;

import com.cdc.usercenter.common.Result;
import com.cdc.usercenter.service.CourseService;
import com.cdc.usercenter.vo.CategoryVO;
import com.cdc.usercenter.vo.CourseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程控制器
 */
@RestController
@RequestMapping("/v1/course")
@RequiredArgsConstructor
public class CourseController {
    
    private final CourseService courseService;
    
    /**
     * 获取所有分类及课程
     */
    @GetMapping("/categories")
    public Result<List<CategoryVO>> getAllCategories(@AuthenticationPrincipal Long userId) {
        List<CategoryVO> categories = courseService.getAllCategoriesWithCourses(userId);
        return Result.success(categories);
    }
    
    /**
     * 获取课程详情
     */
    @GetMapping("/{id}")
    public Result<CourseVO> getCourseDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        CourseVO course = courseService.getCourseById(id, userId);
        if (course == null) {
            return Result.error(404, "课程不存在");
        }
        return Result.success(course);
    }
}
