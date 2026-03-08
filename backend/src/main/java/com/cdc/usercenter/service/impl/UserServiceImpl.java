package com.cdc.usercenter.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cdc.usercenter.common.BusinessException;
import com.cdc.usercenter.common.ResultCode;
import com.cdc.usercenter.dto.UserLoginDTO;
import com.cdc.usercenter.dto.UserRegisterDTO;
import com.cdc.usercenter.dto.UserUpdateDTO;
import com.cdc.usercenter.entity.SysLoginLog;
import com.cdc.usercenter.entity.SysUser;
import com.cdc.usercenter.mapper.LoginLogMapper;
import com.cdc.usercenter.mapper.UserMapper;
import com.cdc.usercenter.service.TokenService;
import com.cdc.usercenter.service.UserService;
import com.cdc.usercenter.vo.LoginVO;
import com.cdc.usercenter.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    private final LoginLogMapper loginLogMapper;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional
    public UserVO register(UserRegisterDTO dto) {
        // 检查用户名是否存在
        if (getByUsername(dto.getUsername()) != null) {
            throw new BusinessException(ResultCode.USER_EXISTS);
        }
        
        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setStatus(1);
        user.setGender(0);
        
        userMapper.insert(user);
        
        return toUserVO(user);
    }
    
    @Override
    public LoginVO login(UserLoginDTO dto, String ip) {
        // 查找用户
        SysUser user = getByUsername(dto.getUsername());
        if (user == null) {
            saveLoginLog(null, dto.getUsername(), ip, 0, "用户不存在");
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        // 检查状态
        if (user.getStatus() != 1) {
            saveLoginLog(user.getId(), dto.getUsername(), ip, 0, "用户已禁用");
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        
        // 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            saveLoginLog(user.getId(), dto.getUsername(), ip, 0, "密码错误");
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        
        // 生成Token
        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);
        tokenService.saveTokenToRedis(user.getId(), refreshToken);
        
        // 记录登录日志
        saveLoginLog(user.getId(), dto.getUsername(), ip, 1, "登录成功");
        
        // 构建返回
        LoginVO vo = new LoginVO();
        vo.setAccessToken(accessToken);
        vo.setRefreshToken(refreshToken);
        vo.setExpiresIn(7200L);
        vo.setUser(toUserVO(user));
        
        return vo;
    }
    
    @Override
    public void logout(Long userId) {
        tokenService.deleteTokenFromRedis(userId);
    }
    
    @Override
    public UserVO getUserInfo(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return toUserVO(user);
    }
    
    @Override
    public UserVO updateUserInfo(Long userId, UserUpdateDTO dto) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        if (dto.getNickname() != null) user.setNickname(dto.getNickname());
        if (dto.getAvatar() != null) user.setAvatar(dto.getAvatar());
        if (dto.getGender() != null) user.setGender(dto.getGender());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        
        userMapper.updateById(user);
        return toUserVO(user);
    }
    
    @Override
    public SysUser getById(Long userId) {
        return userMapper.selectById(userId);
    }
    
    @Override
    public SysUser getByUsername(String username) {
        return userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
        );
    }
    
    private UserVO toUserVO(SysUser user) {
        UserVO vo = new UserVO();
        BeanUtil.copyProperties(user, vo);
        return vo;
    }
    
    private void saveLoginLog(Long userId, String username, String ip, Integer status, String message) {
        SysLoginLog log = new SysLoginLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setLoginIp(ip);
        log.setLoginTime(LocalDateTime.now());
        log.setLoginType(1);
        log.setStatus(status);
        log.setMessage(message);
        loginLogMapper.insert(log);
    }
}
