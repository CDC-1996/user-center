package com.cdc.usercenter.controller;

import com.cdc.usercenter.common.Result;
import com.cdc.usercenter.service.StudyService;
import com.cdc.usercenter.vo.StudyProgressVO;
import com.cdc.usercenter.vo.StudyStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
