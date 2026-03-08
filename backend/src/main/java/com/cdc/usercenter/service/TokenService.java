package com.cdc.usercenter.service;

import com.cdc.usercenter.entity.SysUser;

/**
 * Token服务接口
 */
public interface TokenService {
    
    /**
     * 生成Access Token
     */
    String generateAccessToken(SysUser user);
    
    /**
     * 生成Refresh Token
     */
    String generateRefreshToken(SysUser user);
    
    /**
     * 验证Token
     */
    boolean validateToken(String token);
    
    /**
     * 从Token获取用户ID
     */
    Long getUserIdFromToken(String token);
    
    /**
     * 从Token获取用户名
     */
    String getUsernameFromToken(String token);
    
    /**
     * 刷新Token
     */
    String refreshToken(String refreshToken);
    
    /**
     * 将Token存入Redis
     */
    void saveTokenToRedis(Long userId, String token);
    
    /**
     * 从Redis获取Token
     */
    String getTokenFromRedis(Long userId);
    
    /**
     * 删除Redis中的Token
     */
    void deleteTokenFromRedis(Long userId);
}
