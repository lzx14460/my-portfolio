package com.example.system.mapper;

import com.example.system.entity.SportRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SportRecordMapper {

    int insert(SportRecord record);

    List<SportRecord> selectByUserId(@Param("userId") Long userId);

    SportRecord selectById(@Param("id") Long id);

    int deleteById(@Param("id") Long id);
}