package com.cdc.usercenter.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdc.usercenter.entity.*;
import com.cdc.usercenter.mapper.*;
import com.cdc.usercenter.vo.QuestionVO;
import com.cdc.usercenter.vo.StudyProgressVO;
import com.cdc.usercenter.vo.StudyStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
    private final QuestionMapper questionMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    
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
        
        // 今日学习时长
        Long todayStudyTime = studyRecordMapper.sumStudyTimeByUserIdAndDate(userId, LocalDate.now());
        
        // 总学习时长
        Long totalStudyTime = studyRecordMapper.sumStudyTimeByUserId(userId);
        
        // 课程统计
        LambdaQueryWrapper<UserCourseProgress> progressWrapper = new LambdaQueryWrapper<UserCourseProgress>()
            .eq(UserCourseProgress::getUserId, userId);
        Long courseCount = progressMapper.selectCount(progressWrapper);
        
        // 已完成课程数（进度100%）
        LambdaQueryWrapper<UserCourseProgress> completedWrapper = new LambdaQueryWrapper<UserCourseProgress>()
            .eq(UserCourseProgress::getUserId, userId)
            .eq(UserCourseProgress::getProgress, new BigDecimal("100.00"));
        Long completedCount = progressMapper.selectCount(completedWrapper);
        
        // 收藏题目数
        Long collectCount = studyRecordMapper.selectCount(
            new LambdaQueryWrapper<UserStudyRecord>()
                .eq(UserStudyRecord::getUserId, userId)
                .eq(UserStudyRecord::getIsCollected, 1)
        );
        
        // 错题本数量
        Long reviewCount = studyRecordMapper.selectCount(
            new LambdaQueryWrapper<UserStudyRecord>()
                .eq(UserStudyRecord::getUserId, userId)
                .eq(UserStudyRecord::getNeedReview, 1)
        );
        
        stats.setTotalQuestions(totalCount.intValue());
        stats.setTodayQuestions(todayCount.intValue());
        stats.setTotalTime(totalStudyTime != null ? totalStudyTime.intValue() / 60 : 0); // 转换为分钟
        stats.setTodayTime(todayStudyTime != null ? todayStudyTime.intValue() / 60 : 0);
        stats.setTotalCourses(courseCount.intValue());
        stats.setCompletedCourses(completedCount.intValue());
        stats.setCollectCount(collectCount.intValue());
        stats.setReviewCount(reviewCount.intValue());
        
        return stats;
    }
    
    /**
     * 获取每日学习统计
     */
    public Map<LocalDate, StudyStatsVO> getDailyStats(Long userId, int days) {
        Map<LocalDate, StudyStatsVO> result = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            StudyStatsVO dayStats = new StudyStatsVO();
            
            // 当日学习题数
            Long questionCount = studyRecordMapper.selectCount(
                new LambdaQueryWrapper<UserStudyRecord>()
                    .eq(UserStudyRecord::getUserId, userId)
                    .ge(UserStudyRecord::getCreatedAt, date.atStartOfDay())
                    .lt(UserStudyRecord::getCreatedAt, date.plusDays(1).atStartOfDay())
            );
            
            // 当日学习时长
            Long studyTime = studyRecordMapper.sumStudyTimeByUserIdAndDate(userId, date);
            
            dayStats.setTodayQuestions(questionCount.intValue());
            dayStats.setTodayTime(studyTime != null ? studyTime.intValue() / 60 : 0);
            
            result.put(date, dayStats);
        }
        
        return result;
    }
    
    /**
     * 获取用户学习进度列表
     */
    public List<StudyProgressVO> getStudyProgress(Long userId) {
        List<UserCourseProgress> progressList = progressMapper.selectList(
            new LambdaQueryWrapper<UserCourseProgress>()
                .eq(UserCourseProgress::getUserId, userId)
                .orderByDesc(UserCourseProgress::getLastStudyTime)
        );
        
        if (progressList.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Long> courseIds = progressList.stream()
            .map(UserCourseProgress::getCourseId)
            .collect(Collectors.toList());
        List<Course> courses = courseMapper.selectBatchIds(courseIds);
        Map<Long, Course> courseMap = courses.stream()
            .collect(Collectors.toMap(Course::getId, c -> c));
        
        List<Long> categoryIds = courses.stream()
            .map(Course::getCategoryId)
            .distinct()
            .collect(Collectors.toList());
        Map<Long, String> categoryNameMap = categoryMapper.selectBatchIds(categoryIds).stream()
            .collect(Collectors.toMap(CourseCategory::getId, CourseCategory::getCategoryName));
        
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
     * 记录学习时长
     */
    @Transactional
    public void recordStudyTime(Long userId, Long questionId, Integer studyTime) {
        Question question = questionMapper.selectById(questionId);
        if (question == null) return;
        
        UserStudyRecord record = studyRecordMapper.selectOne(
            new LambdaQueryWrapper<UserStudyRecord>()
                .eq(UserStudyRecord::getUserId, userId)
                .eq(UserStudyRecord::getQuestionId, questionId)
        );
        
        if (record != null) {
            record.setStudyTime(record.getStudyTime() + studyTime);
            studyRecordMapper.updateById(record);
        }
        
        // 更新课程进度
        updateCourseProgress(userId, question.getCourseId());
    }
    
    /**
     * 随机刷题
     */
    public List<QuestionVO> getRandomQuestions(Long userId, Long courseId, Long categoryId, Integer difficulty, int count) {
        // 构建查询条件
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<Question>()
            .eq(Question::getStatus, 1)
            .last("ORDER BY RAND() LIMIT " + count);
        
        if (courseId != null) {
            wrapper.eq(Question::getCourseId, courseId);
        }
        
        if (difficulty != null) {
            wrapper.eq(Question::getDifficulty, difficulty);
        }
        
        if (categoryId != null) {
            // 获取分类下所有课程ID
            List<Long> courseIds = courseMapper.selectList(
                new LambdaQueryWrapper<Course>()
                    .eq(Course::getCategoryId, categoryId)
                    .eq(Course::getStatus, 1)
            ).stream().map(Course::getId).collect(Collectors.toList());
            
            if (courseIds.isEmpty()) {
                return new ArrayList<>();
            }
            wrapper.in(Question::getCourseId, courseIds);
        }
        
        List<Question> questions = questionMapper.selectList(wrapper);
        
        // 获取用户学习记录
        Map<Long, UserStudyRecord> recordMap = null;
        if (userId != null && !questions.isEmpty()) {
            List<Long> questionIds = questions.stream().map(Question::getId).collect(Collectors.toList());
            recordMap = studyRecordMapper.selectList(
                new LambdaQueryWrapper<UserStudyRecord>()
                    .eq(UserStudyRecord::getUserId, userId)
                    .in(UserStudyRecord::getQuestionId, questionIds)
            ).stream().collect(Collectors.toMap(UserStudyRecord::getQuestionId, r -> r));
        }
        
        // 获取课程名称映射
        Map<Long, String> courseNameMap = getCourseNameMap(questions);
        
        return questions.stream()
            .map(q -> toQuestionVO(q, courseNameMap.get(q.getCourseId()), 
                recordMap != null ? recordMap.get(q.getId()) : null, false))
            .collect(Collectors.toList());
    }
    
    /**
     * 搜索题目
     */
    public Page<QuestionVO> searchQuestions(Long userId, String keyword, Long courseId, int page, int size) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<Question>()
            .eq(Question::getStatus, 1)
            .and(w -> w
                .like(Question::getTitle, keyword)
                .or().like(Question::getContent, keyword)
                .or().like(Question::getTags, keyword)
            )
            .orderByDesc(Question::getViewCount);
        
        if (courseId != null) {
            wrapper.eq(Question::getCourseId, courseId);
        }
        
        Page<Question> pageParam = new Page<>(page, size);
        Page<Question> questionPage = questionMapper.selectPage(pageParam, wrapper);
        
        // 获取用户学习记录
        Map<Long, UserStudyRecord> recordMap = null;
        if (userId != null && !questionPage.getRecords().isEmpty()) {
            List<Long> questionIds = questionPage.getRecords().stream()
                .map(Question::getId).collect(Collectors.toList());
            recordMap = studyRecordMapper.selectList(
                new LambdaQueryWrapper<UserStudyRecord>()
                    .eq(UserStudyRecord::getUserId, userId)
                    .in(UserStudyRecord::getQuestionId, questionIds)
            ).stream().collect(Collectors.toMap(UserStudyRecord::getQuestionId, r -> r));
        }
        
        Map<Long, String> courseNameMap = getCourseNameMap(questionPage.getRecords());
        Map<Long, UserStudyRecord> finalRecordMap = recordMap;
        
        Page<QuestionVO> voPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        voPage.setRecords(questionPage.getRecords().stream()
            .map(q -> toQuestionVO(q, courseNameMap.get(q.getCourseId()), 
                finalRecordMap != null ? finalRecordMap.get(q.getId()) : null, false))
            .collect(Collectors.toList()));
        
        return voPage;
    }
    
    /**
     * 标记/取消标记需要复习
     */
    @Transactional
    public boolean toggleReview(Long userId, Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            return false;
        }
        
        UserStudyRecord record = studyRecordMapper.selectOne(
            new LambdaQueryWrapper<UserStudyRecord>()
                .eq(UserStudyRecord::getUserId, userId)
                .eq(UserStudyRecord::getQuestionId, questionId)
        );
        
        if (record == null) {
            record = new UserStudyRecord();
            record.setUserId(userId);
            record.setCourseId(question.getCourseId());
            record.setQuestionId(questionId);
            record.setIsViewed(1);
            record.setIsCollected(0);
            record.setNeedReview(1);
            record.setStudyTime(0);
            studyRecordMapper.insert(record);
            return true;
        } else {
            int newStatus = record.getNeedReview() == 1 ? 0 : 1;
            record.setNeedReview(newStatus);
            studyRecordMapper.updateById(record);
            return newStatus == 1;
        }
    }
    
    /**
     * 获取错题本/复习列表
     */
    public Page<QuestionVO> getReviewList(Long userId, Long courseId, int page, int size) {
        LambdaQueryWrapper<UserStudyRecord> recordWrapper = new LambdaQueryWrapper<UserStudyRecord>()
            .eq(UserStudyRecord::getUserId, userId)
            .eq(UserStudyRecord::getNeedReview, 1)
            .orderByDesc(UserStudyRecord::getUpdatedAt);
        
        if (courseId != null) {
            recordWrapper.eq(UserStudyRecord::getCourseId, courseId);
        }
        
        Page<UserStudyRecord> recordPage = new Page<>(page, size);
        Page<UserStudyRecord> records = studyRecordMapper.selectPage(recordPage, recordWrapper);
        
        if (records.getRecords().isEmpty()) {
            return new Page<>(page, size, 0);
        }
        
        Map<Long, UserStudyRecord> recordMap = records.getRecords().stream()
            .collect(Collectors.toMap(UserStudyRecord::getQuestionId, r -> r));
        
        List<Question> questions = questionMapper.selectBatchIds(recordMap.keySet());
        Map<Long, String> courseNameMap = getCourseNameMap(questions);
        
        Page<QuestionVO> voPage = new Page<>(records.getCurrent(), records.getSize(), records.getTotal());
        voPage.setRecords(questions.stream()
            .map(q -> toQuestionVO(q, courseNameMap.get(q.getCourseId()), recordMap.get(q.getId()), false))
            .collect(Collectors.toList()));
        
        return voPage;
    }
    
    /**
     * 获取继续学习的题目
     */
    public QuestionVO getContinueQuestion(Long userId) {
        // 获取最近学习的课程
        UserCourseProgress lastProgress = progressMapper.selectOne(
            new LambdaQueryWrapper<UserCourseProgress>()
                .eq(UserCourseProgress::getUserId, userId)
                .lt(UserCourseProgress::getProgress, new BigDecimal("100.00"))
                .orderByDesc(UserCourseProgress::getLastStudyTime)
                .last("LIMIT 1")
        );
        
        if (lastProgress == null) {
            return null;
        }
        
        // 获取该课程下一个未学习的题目
        UserStudyRecord lastStudy = studyRecordMapper.selectOne(
            new LambdaQueryWrapper<UserStudyRecord>()
                .eq(UserStudyRecord::getUserId, userId)
                .eq(UserStudyRecord::getCourseId, lastProgress.getCourseId())
                .orderByDesc(UserStudyRecord::getUpdatedAt)
                .last("LIMIT 1")
        );
        
        Question nextQuestion;
        if (lastStudy != null) {
            // 获取下一题
            nextQuestion = questionMapper.selectOne(
                new LambdaQueryWrapper<Question>()
                    .eq(Question::getCourseId, lastProgress.getCourseId())
                    .eq(Question::getStatus, 1)
                    .gt(Question::getId, lastStudy.getQuestionId())
                    .orderByAsc(Question::getId)
                    .last("LIMIT 1")
            );
        } else {
            // 获取第一题
            nextQuestion = questionMapper.selectOne(
                new LambdaQueryWrapper<Question>()
                    .eq(Question::getCourseId, lastProgress.getCourseId())
                    .eq(Question::getStatus, 1)
                    .orderByAsc(Question::getId)
                    .last("LIMIT 1")
            );
        }
        
        if (nextQuestion == null) {
            return null;
        }
        
        Course course = courseMapper.selectById(nextQuestion.getCourseId());
        return toQuestionVO(nextQuestion, course != null ? course.getCourseName() : "", null, true);
    }
    
    /**
     * 获取学习日历
     */
    public List<LocalDate> getStudyCalendar(Long userId, String month) {
        YearMonth yearMonth = YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        
        return studyRecordMapper.selectDistinctStudyDates(userId, start, end);
    }
    
    /**
     * 更新用户课程进度
     */
    public void updateCourseProgress(Long userId, Long courseId) {
        Long totalQuestions = studyRecordMapper.selectCount(
            new LambdaQueryWrapper<UserStudyRecord>()
                .eq(UserStudyRecord::getUserId, userId)
                .eq(UserStudyRecord::getCourseId, courseId)
                .eq(UserStudyRecord::getIsViewed, 1)
        );
        
        Course course = courseMapper.selectById(courseId);
        if (course == null) return;
        
        BigDecimal progress = BigDecimal.ZERO;
        if (course.getQuestionCount() > 0) {
            progress = new BigDecimal(totalQuestions)
                .multiply(new BigDecimal("100"))
                .divide(new BigDecimal(course.getQuestionCount()), 2, RoundingMode.HALF_UP);
        }
        
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
    
    private Map<Long, String> getCourseNameMap(List<Question> questions) {
        if (questions.isEmpty()) return new HashMap<>();
        
        List<Long> courseIds = questions.stream()
            .map(Question::getCourseId)
            .distinct()
            .collect(Collectors.toList());
        
        return courseMapper.selectBatchIds(courseIds).stream()
            .collect(Collectors.toMap(Course::getId, Course::getCourseName));
    }
    
    private QuestionVO toQuestionVO(Question question, String courseName, UserStudyRecord record, boolean includeAnswer) {
        QuestionVO vo = new QuestionVO();
        vo.setId(question.getId());
        vo.setCourseId(question.getCourseId());
        vo.setCourseName(courseName);
        vo.setTitle(question.getTitle());
        vo.setContent(question.getContent());
        
        if (includeAnswer) {
            vo.setAnswer(question.getAnswer());
            vo.setAnalysis(question.getAnalysis());
        }
        
        vo.setDifficulty(question.getDifficulty());
        if (question.getTags() != null && !question.getTags().isEmpty()) {
            vo.setTags(question.getTags().split(","));
        }
        vo.setViewCount(question.getViewCount());
        vo.setCollectCount(question.getCollectCount());
        
        if (record != null) {
            vo.setIsViewed(record.getIsViewed() == 1);
            vo.setIsCollected(record.getIsCollected() == 1);
            vo.setNeedReview(record.getNeedReview() == 1);
        } else {
            vo.setIsViewed(false);
            vo.setIsCollected(false);
            vo.setNeedReview(false);
        }
        
        return vo;
    }
}
