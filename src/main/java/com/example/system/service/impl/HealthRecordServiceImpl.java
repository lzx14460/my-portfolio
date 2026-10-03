package com.example.system.service.impl;

import com.example.system.entity.HealthRecord;
import com.example.system.mapper.HealthRecordMapper;
import com.example.system.service.HealthRecordService;
import com.example.system.vo.HealthRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HealthRecordServiceImpl implements HealthRecordService {

    @Autowired
    private HealthRecordMapper healthRecordMapper;

    /**
     * 保存健康数据（当天已有记录则更新，否则新增）- 兼容旧接口
     */
    @Override
    @Transactional
    public HealthRecord saveRecord(Long userId, BigDecimal height, BigDecimal weight) {
        return saveRecord(userId, LocalDate.now(), height, weight);
    }

    /**
     * 保存健康数据（支持自定义日期）
     * @param userId 用户ID
     * @param recordDate 记录日期
     * @param height 身高
     * @param weight 体重
     */
    @Override
    @Transactional
    public HealthRecord saveRecord(Long userId, LocalDate recordDate, BigDecimal height, BigDecimal weight) {
        // 检查指定日期是否已有记录
        HealthRecord existing = healthRecordMapper.selectTodayRecord(userId, recordDate.toString());

        if (existing != null) {
            // 更新已有记录
            existing.setHeight(height);
            existing.setWeight(weight);
            healthRecordMapper.update(existing);
            log.info("更新健康记录: userId={}, date={}, height={}, weight={}", userId, recordDate, height, weight);
            return existing;
        } else {
            // 创建新记录
            HealthRecord record = new HealthRecord();
            record.setUserId(userId);
            record.setHeight(height);
            record.setWeight(weight);
            record.setRecordDate(recordDate);
            healthRecordMapper.insert(record);
            log.info("新增健康记录: userId={}, date={}, height={}, weight={}", userId, recordDate, height, weight);
            return record;
        }
    }

    /**
     * 获取用户的所有健康记录（按日期升序）
     */
    @Override
    public List<HealthRecordVO> getRecordsByUserId(Long userId) {
        List<HealthRecord> records = healthRecordMapper.selectByUserId(userId);
        return records.stream().map(record -> {
            HealthRecordVO vo = new HealthRecordVO();
            BeanUtils.copyProperties(record, vo);
            // 确保日期字段正确转换
            if (record.getRecordDate() != null) {
                vo.setRecordDate(record.getRecordDate());
            }
            return vo;
        }).collect(Collectors.toList());
    }
}