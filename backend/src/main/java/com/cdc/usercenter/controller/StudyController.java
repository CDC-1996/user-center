package com.cdc.usercenter.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdc.usercenter.common.Result;
import com.cdc.usercenter.service.StudyService;
import com.cdc.usercenter.vo.QuestionVO;
import com.cdc.usercenter.vo.StudyProgressVO;
import com.cdc.usercenter.vo.StudyStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 学习记录控制器
 */
@RestController
@RequestMapping("/v1/study")
@RequiredArgsConstructor
public class StudyController {
    
    private final StudyService studyService;
    
    /**
     * 获取学习统计
     */
    @GetMapping("/stats")
    public Result<StudyStatsVO> getStudyStats(@AuthenticationPrincipal Long userId) {
        StudyStatsVO stats = studyService.getStudyStats(userId);
        return Result.success(stats);
    }
    
    /**
     * 获取学习进度
     */
    @GetMapping("/progress")
    public Result<List<StudyProgressVO>> getStudyProgress(@AuthenticationPrincipal Long userId) {
        List<StudyProgressVO> progress = studyService.getStudyProgress(userId);
        return Result.success(progress);
    }
    
    /**
     * 获取每日学习统计（最近N天）
     */
    @GetMapping("/stats/daily")
    public Result<Map<LocalDate, StudyStatsVO>> getDailyStats(
            @RequestParam(defaultValue = "7") int days,
            @AuthenticationPrincipal Long userId) {
        Map<LocalDate, StudyStatsVO> stats = studyService.getDailyStats(userId, days);
        return Result.success(stats);
    }
    
    /**
     * 记录学习时长
     */
    @PostMapping("/time")
    public Result<String> recordStudyTime(
            @RequestParam Long questionId,
            @RequestParam Integer studyTime,
            @AuthenticationPrincipal Long userId) {
        studyService.recordStudyTime(userId, questionId, studyTime);
        return Result.success("记录成功");
    }
    
    /**
     * 随机刷题
     */
    @GetMapping("/random")
    public Result<List<QuestionVO>> getRandomQuestions(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(defaultValue = "10") int count,
            @AuthenticationPrincipal Long userId) {
        List<QuestionVO> questions = studyService.getRandomQuestions(userId, courseId, categoryId, difficulty, count);
        return Result.success(questions);
    }
    
    /**
     * 搜索题目
     */
    @GetMapping("/search")
    public Result<Page<QuestionVO>> searchQuestions(
            @RequestParam String keyword,
            @RequestParam(required = false) Long courseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Long userId) {
        Page<QuestionVO> questions = studyService.searchQuestions(userId, keyword, courseId, page, size);
        return Result.success(questions);
    }
    
    /**
     * 标记题目需要复习（加入错题本）
     */
    @PostMapping("/{questionId}/review")
    public Result<Boolean> toggleReview(
            @PathVariable Long questionId,
            @AuthenticationPrincipal Long userId) {
        boolean needReview = studyService.toggleReview(userId, questionId);
        return Result.success(needReview ? "已加入错题本" : "已从错题本移除", needReview);
    }
    
    /**
     * 获取错题本/复习列表
     */
    @GetMapping("/review")
    public Result<Page<QuestionVO>> getReviewList(
            @RequestParam(required = false) Long courseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Long userId) {
        Page<QuestionVO> questions = studyService.getReviewList(userId, courseId, page, size);
        return Result.success(questions);
    }
    
    /**
     * 获取继续学习的题目（上次学习的课程）
     */
    @GetMapping("/continue")
    public Result<QuestionVO> getContinueQuestion(@AuthenticationPrincipal Long userId) {
        QuestionVO question = studyService.getContinueQuestion(userId);
        return Result.success(question);
    }
    
    /**
     * 获取学习日历（哪天有学习记录）
     */
    @GetMapping("/calendar")
    public Result<List<LocalDate>> getStudyCalendar(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") String month,
            @AuthenticationPrincipal Long userId) {
        List<LocalDate> dates = studyService.getStudyCalendar(userId, month);
        return Result.success(dates);
    }
}
