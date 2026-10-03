package com.example.system.mapper;

import com.example.system.entity.HealthRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface HealthRecordMapper {

    /**
     * 插入健康记录
     */
    int insert(HealthRecord record);

    /**
     * 根据用户ID查询所有健康记录（按日期升序）
     */
    List<HealthRecord> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户指定日期的健康记录
     */
    HealthRecord selectByUserIdAndDate(@Param("userId") Long userId, @Param("recordDate") String recordDate);

    /**
     * 查询用户今天的健康记录
     */
    HealthRecord selectTodayRecord(@Param("userId") Long userId, @Param("today") String today);

    /**
     * 根据ID查询
     */
    HealthRecord selectById(@Param("id") Long id);

    /**
     * 更新健康记录
     */
    int update(HealthRecord record);

    /**
     * 根据ID删除健康记录
     */
    int deleteById(@Param("id") Long id);

    /**
     * 删除用户指定日期的健康记录
     */
    int deleteByUserIdAndDate(@Param("userId") Long userId, @Param("recordDate") String recordDate);
}