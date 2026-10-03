package com.example.system.service;

import com.example.system.entity.HealthRecord;
import com.example.system.vo.HealthRecordVO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface HealthRecordService {

    /**
     * 保存健康数据（当天）- 兼容旧接口
     */
    HealthRecord saveRecord(Long userId, BigDecimal height, BigDecimal weight);

    /**
     * 保存健康数据（支持自定义日期）
     * @param userId 用户ID
     * @param recordDate 记录日期
     * @param height 身高
     * @param weight 体重
     */
    HealthRecord saveRecord(Long userId, LocalDate recordDate, BigDecimal height, BigDecimal weight);

    /**
     * 获取用户的所有健康记录
     */
    List<HealthRecordVO> getRecordsByUserId(Long userId);
}