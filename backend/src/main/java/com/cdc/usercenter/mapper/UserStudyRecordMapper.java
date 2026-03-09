package com.cdc.usercenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdc.usercenter.entity.UserStudyRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户学习记录Mapper
 */
@Mapper
public interface UserStudyRecordMapper extends BaseMapper<UserStudyRecord> {
    
    /**
     * 统计用户某日学习时长
     */
    @Select("SELECT IFNULL(SUM(study_time), 0) FROM user_study_record " +
            "WHERE user_id = #{userId} AND DATE(created_at) = #{date}")
    Long sumStudyTimeByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    /**
     * 统计用户总学习时长
     */
    @Select("SELECT IFNULL(SUM(study_time), 0) FROM user_study_record WHERE user_id = #{userId}")
    Long sumStudyTimeByUserId(@Param("userId") Long userId);
    
    /**
     * 获取用户某月有学习记录的日期
     */
    @Select("SELECT DISTINCT DATE(created_at) FROM user_study_record " +
            "WHERE user_id = #{userId} AND DATE(created_at) >= #{start} AND DATE(created_at) <= #{end}")
    List<LocalDate> selectDistinctStudyDates(@Param("userId") Long userId, 
                                              @Param("start") LocalDate start, 
                                              @Param("end") LocalDate end);
}
