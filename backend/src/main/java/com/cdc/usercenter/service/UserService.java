package com.cdc.usercenter.service;

import com.cdc.usercenter.dto.UserLoginDTO;
import com.cdc.usercenter.dto.UserRegisterDTO;
import com.cdc.usercenter.dto.UserUpdateDTO;
import com.cdc.usercenter.entity.SysUser;
import com.cdc.usercenter.vo.LoginVO;
import com.cdc.usercenter.vo.UserVO;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 用户注册
     */
    UserVO register(UserRegisterDTO dto);
    
    /**
     * 用户登录
     */
    LoginVO login(UserLoginDTO dto, String ip);
    
    /**
     * 用户登出
     */
    void logout(Long userId);
    
    /**
     * 获取用户信息
     */
    UserVO getUserInfo(Long userId);
    
    /**
     * 更新用户信息
     */
    UserVO updateUserInfo(Long userId, UserUpdateDTO dto);
    
    /**
     * 根据ID获取用户
     */
    SysUser getById(Long userId);
    
    /**
     * 根据用户名获取用户
     */
    SysUser getByUsername(String username);
}
