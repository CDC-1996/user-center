package com.cdc.usercenter.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cdc.usercenter.common.BusinessException;
import com.cdc.usercenter.common.ResultCode;
import com.cdc.usercenter.entity.SysOauth;
import com.cdc.usercenter.entity.SysUser;
import com.cdc.usercenter.mapper.OauthMapper;
import com.cdc.usercenter.mapper.UserMapper;
import com.cdc.usercenter.service.OAuthService;
import com.cdc.usercenter.service.TokenService;
import com.cdc.usercenter.vo.LoginVO;
import com.cdc.usercenter.vo.OAuthUserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * OAuth服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthServiceImpl implements OAuthService {
    
    @Value("${oauth.github.client-id}")
    private String githubClientId;
    
    @Value("${oauth.github.client-secret}")
    private String githubClientSecret;
    
    @Value("${oauth.github.redirect-uri}")
    private String githubRedirectUri;
    
    private final OauthMapper oauthMapper;
    private final UserMapper userMapper;
    private final TokenService tokenService;
    
    @Override
    public String getGithubAuthUrl() {
        return String.format(
            "https://github.com/login/oauth/authorize?client_id=%s&redirect_uri=%s&scope=user",
            githubClientId, githubRedirectUri
        );
    }
    
    @Override
    @Transactional
    public LoginVO githubCallback(String code, String ip) {
        // 获取access token
        String tokenUrl = String.format(
            "https://github.com/login/oauth/access_token?client_id=%s&client_secret=%s&code=%s&redirect_uri=%s",
            githubClientId, githubClientSecret, code, githubRedirectUri
        );
        
        String tokenResponse = HttpUtil.post(tokenUrl, "");
        log.info("GitHub token response: {}", tokenResponse);
        
        // 解析access token
        String accessToken = parseAccessToken(tokenResponse);
        if (accessToken == null) {
            throw new BusinessException(ResultCode.OAUTH_ERROR);
        }
        
        // 获取用户信息
        String userUrl = "https://api.github.com/user";
        String userResponse = HttpUtil.createGet(userUrl)
            .header("Authorization", "Bearer " + accessToken)
            .execute()
            .body();
        
        JSONObject userJson = JSONUtil.parseObj(userResponse);
        String openid = userJson.getStr("id");
        String nickname = userJson.getStr("login");
        String avatar = userJson.getStr("avatar_url");
        String email = userJson.getStr("email");
        
        // 查找或创建用户
        SysUser user = findOrCreateUser("github", openid, nickname, avatar, email);
        
        // 生成token
        String jwtAccessToken = tokenService.generateAccessToken(user);
        String jwtRefreshToken = tokenService.generateRefreshToken(user);
        tokenService.saveTokenToRedis(user.getId(), jwtRefreshToken);
        
        // 返回登录信息
        LoginVO vo = new LoginVO();
        vo.setAccessToken(jwtAccessToken);
        vo.setRefreshToken(jwtRefreshToken);
        vo.setExpiresIn(7200L);
        
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        vo.setUser(userVO);
        
        return vo;
    }
    
    @Override
    public String getWechatAuthUrl() {
        // TODO: 实现微信授权URL
        throw new BusinessException("微信登录暂未开放");
    }
    
    @Override
    public LoginVO wechatCallback(String code, String ip) {
        throw new BusinessException("微信登录暂未开放");
    }
    
    @Override
    public String getQQAuthUrl() {
        // TODO: 实现QQ授权URL
        throw new BusinessException("QQ登录暂未开放");
    }
    
    @Override
    public LoginVO qqCallback(String code, String ip) {
        throw new BusinessException("QQ登录暂未开放");
    }
    
    @Override
    public OAuthUserVO getOAuthUser(String platform, String code) {
        return null;
    }
    
    private String parseAccessToken(String response) {
        // 格式: access_token=xxx&scope=xxx&token_type=xxx
        String[] parts = response.split("&");
        for (String part : parts) {
            if (part.startsWith("access_token=")) {
                return part.substring("access_token=".length());
            }
        }
        return null;
    }
    
    @Transactional
    public SysUser findOrCreateUser(String platform, String openid, String nickname, String avatar, String email) {
        // 查找已绑定的用户
        SysOauth oauth = oauthMapper.selectOne(
            new LambdaQueryWrapper<SysOauth>()
                .eq(SysOauth::getPlatform, platform)
                .eq(SysOauth::getOpenid, openid)
        );
        
        if (oauth != null) {
            return userMapper.selectById(oauth.getUserId());
        }
        
        // 创建新用户
        SysUser user = new SysUser();
        user.setUsername(platform + "_" + openid.substring(0, 8));
        user.setPassword(""); // OAuth用户无需密码
        user.setNickname(nickname);
        user.setAvatar(avatar);
        user.setEmail(email);
        user.setStatus(1);
        user.setGender(0);
        userMapper.insert(user);
        
        // 创建绑定
        SysOauth newOauth = new SysOauth();
        newOauth.setUserId(user.getId());
        newOauth.setPlatform(platform);
        newOauth.setOpenid(openid);
        newOauth.setNickname(nickname);
        newOauth.setAvatar(avatar);
        oauthMapper.insert(newOauth);
        
        return user;
    }
}
