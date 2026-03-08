package com.cdc.usercenter.service;

import com.cdc.usercenter.vo.LoginVO;
import com.cdc.usercenter.vo.OAuthUserVO;

/**
 * OAuth服务接口
 */
public interface OAuthService {
    
    /**
     * 获取GitHub授权URL
     */
    String getGithubAuthUrl();
    
    /**
     * GitHub登录回调
     */
    LoginVO githubCallback(String code, String ip);
    
    /**
     * 获取微信授权URL
     */
    String getWechatAuthUrl();
    
    /**
     * 微信登录回调
     */
    LoginVO wechatCallback(String code, String ip);
    
    /**
     * 获取QQ授权URL
     */
    String getQQAuthUrl();
    
    /**
     * QQ登录回调
     */
    LoginVO qqCallback(String code, String ip);
    
    /**
     * 根据平台和OpenID获取用户信息
     */
    OAuthUserVO getOAuthUser(String platform, String code);
}
