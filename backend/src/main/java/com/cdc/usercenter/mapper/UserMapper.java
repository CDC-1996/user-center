package com.cdc.usercenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdc.usercenter.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<SysUser> {
}
