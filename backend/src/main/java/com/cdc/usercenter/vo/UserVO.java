package com.cdc.usercenter.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户信息返回
 */
@Data
public class UserVO {
    private Long id;
    
    private String username;
    
    private String phone;
    
    private String email;
    
    private String nickname;
    
    private String avatar;
    
    private Integer gender;
    
    private Integer status;
    
    private LocalDateTime createdAt;
}
