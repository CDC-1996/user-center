package com.cdc.usercenter.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_login_log")
public class SysLoginLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String username;
    
    private String loginIp;
    
    private String loginLocation;
    
    private LocalDateTime loginTime;
    
    private Integer loginType;
    
    private Integer status;
    
    private String message;
}
