package com.cdc.usercenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdc.usercenter.entity.SysLoginLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginLogMapper extends BaseMapper<SysLoginLog> {
}
