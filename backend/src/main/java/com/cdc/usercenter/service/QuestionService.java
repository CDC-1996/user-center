package com.cdc.usercenter.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdc.usercenter.entity.Course;
import com.cdc.usercenter.entity.Question;
import com.cdc.usercenter.entity.UserStudyRecord;
import com.cdc.usercenter.mapper.CourseMapper;
import com.cdc.usercenter.mapper.QuestionMapper;
import com.cdc.usercenter.mapper.UserStudyRecordMapper;
import com.cdc.usercenter.vo.QuestionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 题目服务
 */
@Service
@RequiredArgsConstructor
public class QuestionService {
    
    private final QuestionMapper questionMapper;
    private final CourseMapper courseMapper;
    private final UserStudyRecordMapper studyRecordMapper;
    
    /**
     * 获取课程下的题目列表
     */
    public Page<QuestionVO> getQuestionsByCourse(Long courseId, Long userId, int page, int size) {
        Page<Question> pageParam = new Page<>(page, size);
        
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<Question>()
            .eq(Question::getCourseId, courseId)
            .eq(Question::getStatus, 1)
            .orderByAsc(Question::getSort)
            .orderByAsc(Question::getId);
        
        Page<Question> questionPage = questionMapper.selectPage(pageParam, wrapper);
        
        // 获取用户学习记录
        Map<Long, UserStudyRecord> recordMap = null;
        if (userId != null) {
            LambdaQueryWrapper<UserStudyRecord> recordWrapper = new LambdaQueryWrapper<UserStudyRecord>()
                .eq(UserStudyRecord::getUserId, userId)
                .eq(UserStudyRecord::getCourseId, courseId);
            recordMap = studyRecordMapper.selectList(recordWrapper).stream()
                .collect(Collectors.toMap(UserStudyRecord::getQuestionId, r -> r));
        }
        
        // 获取课程名称
        Course course = courseMapper.selectById(courseId);
        String courseName = course != null ? course.getCourseName() : "";
        
        // 转换
        Page<QuestionVO> voPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        Map<Long, UserStudyRecord> finalRecordMap = recordMap;
        voPage.setRecords(questionPage.getRecords().stream()
            .map(q -> toQuestionVO(q, courseName, finalRecordMap != null ? finalRecordMap.get(q.getId()) : null))
            .collect(Collectors.toList()));
        
        return voPage;
    }
    
    /**
     * 获取题目详情
     */
    public QuestionVO getQuestionDetail(Long questionId, Long userId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null || question.getStatus() != 1) {
            return null;
        }
        
        // 增加浏览次数
        question.setViewCount(question.getViewCount() + 1);
        questionMapper.updateById(question);
        
        // 获取课程名称
        Course course = courseMapper.selectById(question.getCourseId());
        String courseName = course != null ? course.getCourseName() : "";
        
        // 获取用户学习记录
        UserStudyRecord record = null;
        if (userId != null) {
            record = studyRecordMapper.selectOne(
                new LambdaQueryWrapper<UserStudyRecord>()
                    .eq(UserStudyRecord::getUserId, userId)
                    .eq(UserStudyRecord::getQuestionId, questionId)
            );
            
            // 如果没有记录，创建一个（标记为已查看）
            if (record == null) {
                record = new UserStudyRecord();
                record.setUserId(userId);
                record.setCourseId(question.getCourseId());
                record.setQuestionId(questionId);
                record.setIsViewed(1);
                record.setIsCollected(0);
                record.setStudyTime(0);
                studyRecordMapper.insert(record);
            } else if (record.getIsViewed() == 0) {
                // 更新为已查看
                record.setIsViewed(1);
                studyRecordMapper.updateById(record);
            }
        }
        
        QuestionVO vo = toQuestionVO(question, courseName, record);
        
        // 获取上一个和下一个题目ID
        Long prevId = getPrevQuestionId(question.getCourseId(), questionId);
        Long nextId = getNextQuestionId(question.getCourseId(), questionId);
        vo.setPrevId(prevId);
        vo.setNextId(nextId);
        
        return vo;
    }
    
    /**
     * 收藏/取消收藏题目
     */
    public boolean toggleCollect(Long questionId, Long userId) {
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
            record.setIsCollected(1);
            record.setStudyTime(0);
            studyRecordMapper.insert(record);
            
            // 更新收藏数
            question.setCollectCount(question.getCollectCount() + 1);
            questionMapper.updateById(question);
            return true;
        } else {
            int newCollectStatus = record.getIsCollected() == 1 ? 0 : 1;
            record.setIsCollected(newCollectStatus);
            studyRecordMapper.updateById(record);
            
            // 更新收藏数
            if (newCollectStatus == 1) {
                question.setCollectCount(question.getCollectCount() + 1);
            } else {
                question.setCollectCount(Math.max(0, question.getCollectCount() - 1));
            }
            questionMapper.updateById(question);
            return newCollectStatus == 1;
        }
    }
    
    /**
     * 获取收藏的题目列表
     */
    public Page<QuestionVO> getCollectedQuestions(Long userId, int page, int size) {
        // 先获取收藏记录
        Page<UserStudyRecord> recordPage = new Page<>(page, size);
        LambdaQueryWrapper<UserStudyRecord> wrapper = new LambdaQueryWrapper<UserStudyRecord>()
            .eq(UserStudyRecord::getUserId, userId)
            .eq(UserStudyRecord::getIsCollected, 1)
            .orderByDesc(UserStudyRecord::getUpdatedAt);
        
        Page<UserStudyRecord> records = studyRecordMapper.selectPage(recordPage, wrapper);
        
        if (records.getRecords().isEmpty()) {
            return new Page<>(page, size, 0);
        }
        
        // 获取题目ID列表
        Map<Long, UserStudyRecord> recordMap = records.getRecords().stream()
            .collect(Collectors.toMap(UserStudyRecord::getQuestionId, r -> r));
        
        // 获取题目
        List<Question> questions = questionMapper.selectBatchIds(recordMap.keySet());
        
        // 获取课程名称映射
        List<Long> courseIds = questions.stream().map(Question::getCourseId).distinct().collect(Collectors.toList());
        Map<Long, String> courseNameMap = courseMapper.selectBatchIds(courseIds).stream()
            .collect(Collectors.toMap(Course::getId, Course::getCourseName));
        
        // 转换
        Page<QuestionVO> voPage = new Page<>(records.getCurrent(), records.getSize(), records.getTotal());
        voPage.setRecords(questions.stream()
            .map(q -> {
                QuestionVO vo = toQuestionVO(q, courseNameMap.get(q.getCourseId()), recordMap.get(q.getId()));
                vo.setAnswer(null); // 列表不返回答案
                vo.setAnalysis(null);
                return vo;
            })
            .collect(Collectors.toList()));
        
        return voPage;
    }
    
    private Long getPrevQuestionId(Long courseId, Long currentId) {
        Question prev = questionMapper.selectOne(
            new LambdaQueryWrapper<Question>()
                .eq(Question::getCourseId, courseId)
                .eq(Question::getStatus, 1)
                .lt(Question::getId, currentId)
                .orderByDesc(Question::getId)
                .last("LIMIT 1")
        );
        return prev != null ? prev.getId() : null;
    }
    
    private Long getNextQuestionId(Long courseId, Long currentId) {
        Question next = questionMapper.selectOne(
            new LambdaQueryWrapper<Question>()
                .eq(Question::getCourseId, courseId)
                .eq(Question::getStatus, 1)
                .gt(Question::getId, currentId)
                .orderByAsc(Question::getId)
                .last("LIMIT 1")
        );
        return next != null ? next.getId() : null;
    }
    
    private QuestionVO toQuestionVO(Question question, String courseName, UserStudyRecord record) {
        QuestionVO vo = new QuestionVO();
        vo.setId(question.getId());
        vo.setCourseId(question.getCourseId());
        vo.setCourseName(courseName);
        vo.setTitle(question.getTitle());
        vo.setContent(question.getContent());
        vo.setAnswer(question.getAnswer());
        vo.setAnalysis(question.getAnalysis());
        vo.setDifficulty(question.getDifficulty());
        if (question.getTags() != null && !question.getTags().isEmpty()) {
            vo.setTags(question.getTags().split(","));
        }
        vo.setViewCount(question.getViewCount());
        vo.setCollectCount(question.getCollectCount());
        
        if (record != null) {
            vo.setIsViewed(record.getIsViewed() == 1);
            vo.setIsCollected(record.getIsCollected() == 1);
        } else {
            vo.setIsViewed(false);
            vo.setIsCollected(false);
        }
        
        return vo;
    }
}
