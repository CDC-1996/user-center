package com.cdc.usercenter.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cdc.usercenter.entity.Course;
import com.cdc.usercenter.entity.UserCourseProgress;
import com.cdc.usercenter.entity.UserStudyRecord;
import com.cdc.usercenter.mapper.CourseCategoryMapper;
import com.cdc.usercenter.mapper.CourseMapper;
import com.cdc.usercenter.mapper.UserCourseProgressMapper;
import com.cdc.usercenter.mapper.UserStudyRecordMapper;
import com.cdc.usercenter.vo.StudyProgressVO;
import com.cdc.usercenter.vo.StudyStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学习服务
 */
@Service
@RequiredArgsConstructor
public class StudyService {
    
    private final UserStudyRecordMapper studyRecordMapper;
    private final UserCourseProgressMapper progressMapper;
    private final CourseMapper courseMapper;
    private final CourseCategoryMapper categoryMapper;
    
    /**
     * 获取用户学习统计
     */
    public StudyStatsVO getStudyStats(Long userId) {
        StudyStatsVO stats = new StudyStatsVO();
        
        // 总体统计
        LambdaQueryWrapper<UserStudyRecord> totalWrapper = new LambdaQueryWrapper<UserStudyRecord>()
            .eq(UserStudyRecord::getUserId, userId);
        Long totalCount = studyRecordMapper.selectCount(totalWrapper);
        
        // 今日统计
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LambdaQueryWrapper<UserStudyRecord> todayWrapper = new LambdaQueryWrapper<UserStudyRecord>()
            .eq(UserStudyRecord::getUserId, userId)
            .ge(UserStudyRecord::getCreatedAt, todayStart);
        Long todayCount = studyRecordMapper.selectCount(todayWrapper);
        
        // 课程统计
        LambdaQueryWrapper<UserCourseProgress> progressWrapper = new LambdaQueryWrapper<UserCourseProgress>()
            .eq(UserCourseProgress::getUserId, userId);
        Long courseCount = progressMapper.selectCount(progressWrapper);
        
        // 已完成课程数（进度100%）
        LambdaQueryWrapper<UserCourseProgress> completedWrapper = new LambdaQueryWrapper<UserCourseProgress>()
            .eq(UserCourseProgress::getUserId, userId)
            .eq(UserCourseProgress::getProgress, new BigDecimal("100.00"));
        Long completedCount = progressMapper.selectCount(completedWrapper);
        
        stats.setTotalQuestions(totalCount.intValue());
        stats.setTodayQuestions(todayCount.intValue());
        stats.setTotalTime(totalCount.intValue() * 5); // 假设每题5分钟
        stats.setTodayTime(todayCount.intValue() * 5);
        stats.setTotalCourses(courseCount.intValue());
        stats.setCompletedCourses(completedCount.intValue());
        
        return stats;
    }
    
    /**
     * 获取用户学习进度列表
     */
    public List<StudyProgressVO> getStudyProgress(Long userId) {
        // 获取用户所有课程进度
        List<UserCourseProgress> progressList = progressMapper.selectList(
            new LambdaQueryWrapper<UserCourseProgress>()
                .eq(UserCourseProgress::getUserId, userId)
                .orderByDesc(UserCourseProgress::getLastStudyTime)
        );
        
        if (progressList.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取课程信息
        List<Long> courseIds = progressList.stream()
            .map(UserCourseProgress::getCourseId)
            .collect(Collectors.toList());
        List<Course> courses = courseMapper.selectBatchIds(courseIds);
        Map<Long, Course> courseMap = courses.stream()
            .collect(Collectors.toMap(Course::getId, c -> c));
        
        // 获取分类信息
        List<Long> categoryIds = courses.stream()
            .map(Course::getCategoryId)
            .distinct()
            .collect(Collectors.toList());
        Map<Long, String> categoryNameMap = categoryMapper.selectBatchIds(categoryIds).stream()
            .collect(Collectors.toMap(c -> c.getId(), c -> c.getCategoryName()));
        
        // 组装结果
        List<StudyProgressVO> result = new ArrayList<>();
        for (UserCourseProgress progress : progressList) {
            Course course = courseMap.get(progress.getCourseId());
            if (course == null) continue;
            
            StudyProgressVO vo = new StudyProgressVO();
            vo.setCourseId(course.getId());
            vo.setCourseName(course.getCourseName());
            vo.setCategoryName(categoryNameMap.get(course.getCategoryId()));
            vo.setTotalQuestions(progress.getTotalQuestions());
            vo.setCompletedQuestions(progress.getCompletedQuestions());
            vo.setProgress(progress.getProgress());
            vo.setLastStudyTime(progress.getLastStudyTime());
            
            result.add(vo);
        }
        
        return result;
    }
    
    /**
     * 更新用户课程进度
     */
    public void updateCourseProgress(Long userId, Long courseId) {
        // 获取课程总题目数
        Long totalQuestions = studyRecordMapper.selectCount(
            new LambdaQueryWrapper<UserStudyRecord>()
                .eq(UserStudyRecord::getUserId, userId)
                .eq(UserStudyRecord::getCourseId, courseId)
        );
        
        // 获取课程实际题目总数
        Course course = courseMapper.selectById(courseId);
        if (course == null) return;
        
        // 计算进度
        BigDecimal progress = BigDecimal.ZERO;
        if (course.getQuestionCount() > 0) {
            progress = new BigDecimal(totalQuestions)
                .multiply(new BigDecimal("100"))
                .divide(new BigDecimal(course.getQuestionCount()), 2, RoundingMode.HALF_UP);
        }
        
        // 更新或创建进度记录
        UserCourseProgress userProgress = progressMapper.selectOne(
            new LambdaQueryWrapper<UserCourseProgress>()
                .eq(UserCourseProgress::getUserId, userId)
                .eq(UserCourseProgress::getCourseId, courseId)
        );
        
        if (userProgress == null) {
            userProgress = new UserCourseProgress();
            userProgress.setUserId(userId);
            userProgress.setCourseId(courseId);
            userProgress.setTotalQuestions(course.getQuestionCount());
            userProgress.setCompletedQuestions(totalQuestions.intValue());
            userProgress.setProgress(progress);
            userProgress.setLastStudyTime(LocalDateTime.now());
            progressMapper.insert(userProgress);
        } else {
            userProgress.setCompletedQuestions(totalQuestions.intValue());
            userProgress.setProgress(progress);
            userProgress.setLastStudyTime(LocalDateTime.now());
            progressMapper.updateById(userProgress);
        }
    }
}
