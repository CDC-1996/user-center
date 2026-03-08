package com.cdc.usercenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdc.usercenter.entity.SysOauth;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OauthMapper extends BaseMapper<SysOauth> {
}
