package com.cdc.usercenter.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cdc.usercenter.entity.Course;
import com.cdc.usercenter.entity.CourseCategory;
import com.cdc.usercenter.entity.UserCourseProgress;
import com.cdc.usercenter.mapper.CourseCategoryMapper;
import com.cdc.usercenter.mapper.CourseMapper;
import com.cdc.usercenter.mapper.UserCourseProgressMapper;
import com.cdc.usercenter.vo.CategoryVO;
import com.cdc.usercenter.vo.CourseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 课程服务
 */
@Service
@RequiredArgsConstructor
public class CourseService {
    
    private final CourseCategoryMapper categoryMapper;
    private final CourseMapper courseMapper;
    private final UserCourseProgressMapper progressMapper;
    
    /**
     * 获取所有分类及课程
     */
    public List<CategoryVO> getAllCategoriesWithCourses(Long userId) {
        // 获取所有分类
        List<CourseCategory> categories = categoryMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CourseCategory>()
                .eq(CourseCategory::getStatus, 1)
                .orderByAsc(CourseCategory::getSort)
        );
        
        // 获取所有课程
        List<Course> courses = courseMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Course>()
                .eq(Course::getStatus, 1)
                .orderByAsc(Course::getSort)
        );
        
        // 获取用户进度
        Map<Long, UserCourseProgress> progressMap = null;
        if (userId != null) {
            List<UserCourseProgress> progressList = progressMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserCourseProgress>()
                    .eq(UserCourseProgress::getUserId, userId)
            );
            progressMap = progressList.stream()
                .collect(Collectors.toMap(UserCourseProgress::getCourseId, p -> p));
        }
        
        // 按分类分组课程
        Map<Long, List<Course>> courseMap = courses.stream()
            .collect(Collectors.groupingBy(Course::getCategoryId));
        
        // 组装结果
        List<CategoryVO> result = new ArrayList<>();
        Map<Long, UserCourseProgress> finalProgressMap = progressMap;
        for (CourseCategory category : categories) {
            CategoryVO vo = new CategoryVO();
            vo.setId(category.getId());
            vo.setCategoryName(category.getCategoryName());
            vo.setCategoryCode(category.getCategoryCode());
            vo.setIcon(category.getIcon());
            vo.setSort(category.getSort());
            
            List<Course> categoryCourses = courseMap.getOrDefault(category.getId(), new ArrayList<>());
            List<CourseVO> courseVOList = categoryCourses.stream()
                .map(c -> toCourseVO(c, finalProgressMap != null ? finalProgressMap.get(c.getId()) : null))
                .collect(Collectors.toList());
            vo.setCourses(courseVOList);
            
            result.add(vo);
        }
        
        return result;
    }
    
    /**
     * 获取课程详情
     */
    public CourseVO getCourseById(Long courseId, Long userId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null || course.getStatus() != 1) {
            return null;
        }
        
        UserCourseProgress progress = null;
        if (userId != null) {
            progress = progressMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserCourseProgress>()
                    .eq(UserCourseProgress::getUserId, userId)
                    .eq(UserCourseProgress::getCourseId, courseId)
            );
        }
        
        CourseVO vo = toCourseVO(course, progress);
        
        // 获取分类名称
        CourseCategory category = categoryMapper.selectById(course.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getCategoryName());
        }
        
        return vo;
    }
    
    private CourseVO toCourseVO(Course course, UserCourseProgress progress) {
        CourseVO vo = new CourseVO();
        vo.setId(course.getId());
        vo.setCategoryId(course.getCategoryId());
        vo.setCourseName(course.getCourseName());
        vo.setCourseCode(course.getCourseCode());
        vo.setDescription(course.getDescription());
        vo.setCoverImage(course.getCoverImage());
        vo.setDifficulty(course.getDifficulty());
        vo.setQuestionCount(course.getQuestionCount());
        
        if (progress != null) {
            vo.setProgress(progress.getProgress());
            vo.setCompletedQuestions(progress.getCompletedQuestions());
            vo.setIsStarted(true);
        } else {
            vo.setProgress(java.math.BigDecimal.ZERO);
            vo.setCompletedQuestions(0);
            vo.setIsStarted(false);
        }
        
        return vo;
    }
}
