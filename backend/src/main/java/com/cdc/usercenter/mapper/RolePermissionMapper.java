package com.cdc.usercenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdc.usercenter.entity.SysRolePermission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RolePermissionMapper extends BaseMapper<SysRolePermission> {
}
