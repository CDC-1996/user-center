package com.cdc.usercenter.vo;

import lombok.Data;

/**
 * OAuth用户信息
 */
@Data
public class OAuthUserVO {
    private String openid;
    
    private String nickname;
    
    private String avatar;
    
    private String email;
}
