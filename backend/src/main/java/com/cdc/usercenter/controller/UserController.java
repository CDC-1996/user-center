package com.cdc.usercenter.controller;

import com.cdc.usercenter.common.Result;
import com.cdc.usercenter.dto.UserLoginDTO;
import com.cdc.usercenter.dto.UserRegisterDTO;
import com.cdc.usercenter.dto.UserUpdateDTO;
import com.cdc.usercenter.service.UserService;
import com.cdc.usercenter.vo.LoginVO;
import com.cdc.usercenter.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody UserRegisterDTO dto) {
        UserVO user = userService.register(dto);
        return Result.success("注册成功", user);
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody UserLoginDTO dto, HttpServletRequest request) {
        String ip = getClientIp(request);
        LoginVO loginVO = userService.login(dto, ip);
        return Result.success("登录成功", loginVO);
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(@AuthenticationPrincipal Long userId) {
        userService.logout(userId);
        return Result.success("登出成功", null);
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<UserVO> getUserInfo(@AuthenticationPrincipal Long userId) {
        UserVO user = userService.getUserInfo(userId);
        return Result.success(user);
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    public Result<UserVO> updateUserInfo(
            @AuthenticationPrincipal Long userId,
            @RequestBody UserUpdateDTO dto) {
        UserVO user = userService.updateUserInfo(userId, dto);
        return Result.success("更新成功", user);
    }
    
    /**
     * 获取客户端IP
     */
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
