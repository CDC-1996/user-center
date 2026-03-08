package com.cdc.usercenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdc.usercenter.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PermissionMapper extends BaseMapper<SysPermission> {
}
