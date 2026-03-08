package com.cdc.usercenter.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 第三方登录绑定实体
 */
@Data
@TableName("sys_oauth")
public class SysOauth {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String platform;
    
    private String openid;
    
    private String unionid;
    
    private String nickname;
    
    private String avatar;
    
    private String accessToken;
    
    private String refreshToken;
    
    private LocalDateTime expiresAt;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
