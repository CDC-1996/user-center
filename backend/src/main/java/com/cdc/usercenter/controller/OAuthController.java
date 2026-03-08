package com.cdc.usercenter.controller;

import com.cdc.usercenter.common.Result;
import com.cdc.usercenter.service.OAuthService;
import com.cdc.usercenter.vo.LoginVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * OAuth控制器
 */
@RestController
@RequestMapping("/v1/oauth")
@RequiredArgsConstructor
public class OAuthController {
    
    private final OAuthService oAuthService;
    
    /**
     * GitHub登录
     */
    @GetMapping("/github")
    public void githubLogin(HttpServletResponse response) throws IOException {
        String authUrl = oAuthService.getGithubAuthUrl();
        response.sendRedirect(authUrl);
    }
    
    /**
     * GitHub回调
     */
    @GetMapping("/github/callback")
    public Result<LoginVO> githubCallback(
            @RequestParam String code,
            HttpServletRequest request) {
        String ip = getClientIp(request);
        LoginVO loginVO = oAuthService.githubCallback(code, ip);
        return Result.success("GitHub登录成功", loginVO);
    }
    
    /**
     * 微信登录
     */
    @GetMapping("/wechat")
    public void wechatLogin(HttpServletResponse response) throws IOException {
        String authUrl = oAuthService.getWechatAuthUrl();
        response.sendRedirect(authUrl);
    }
    
    /**
     * 微信回调
     */
    @GetMapping("/wechat/callback")
    public Result<LoginVO> wechatCallback(
            @RequestParam String code,
            HttpServletRequest request) {
        String ip = getClientIp(request);
        LoginVO loginVO = oAuthService.wechatCallback(code, ip);
        return Result.success("微信登录成功", loginVO);
    }
    
    /**
     * QQ登录
     */
    @GetMapping("/qq")
    public void qqLogin(HttpServletResponse response) throws IOException {
        String authUrl = oAuthService.getQQAuthUrl();
        response.sendRedirect(authUrl);
    }
    
    /**
     * QQ回调
     */
    @GetMapping("/qq/callback")
    public Result<LoginVO> qqCallback(
            @RequestParam String code,
            HttpServletRequest request) {
        String ip = getClientIp(request);
        LoginVO loginVO = oAuthService.qqCallback(code, ip);
        return Result.success("QQ登录成功", loginVO);
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
