package com.cdc.usercenter.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdc.usercenter.common.Result;
import com.cdc.usercenter.service.QuestionService;
import com.cdc.usercenter.vo.QuestionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 题目控制器
 */
@RestController
@RequestMapping("/v1/question")
@RequiredArgsConstructor
public class QuestionController {
    
    private final QuestionService questionService;
    
    /**
     * 获取课程下的题目列表
     */
    @GetMapping("/course/{courseId}")
    public Result<Page<QuestionVO>> getQuestionsByCourse(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Long userId) {
        Page<QuestionVO> questions = questionService.getQuestionsByCourse(courseId, userId, page, size);
        return Result.success(questions);
    }
    
    /**
     * 获取题目详情
     */
    @GetMapping("/{id}")
    public Result<QuestionVO> getQuestionDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        QuestionVO question = questionService.getQuestionDetail(id, userId);
        if (question == null) {
            return Result.error(404, "题目不存在");
        }
        return Result.success(question);
    }
    
    /**
     * 收藏/取消收藏题目
     */
    @PostMapping("/{id}/collect")
    public Result<Boolean> toggleCollect(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        boolean collected = questionService.toggleCollect(id, userId);
        return Result.success(collected ? "收藏成功" : "取消收藏", collected);
    }
    
    /**
     * 获取收藏的题目列表
     */
    @GetMapping("/collects")
    public Result<Page<QuestionVO>> getCollectedQuestions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Long userId) {
        Page<QuestionVO> questions = questionService.getCollectedQuestions(userId, page, size);
        return Result.success(questions);
    }
}
