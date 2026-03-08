package com.cdc.usercenter.dto;

import lombok.Data;

/**
 * 用户更新请求
 */
@Data
public class UserUpdateDTO {
    private String nickname;
    
    private String avatar;
    
    private Integer gender;
    
    private String phone;
    
    private String email;
}
