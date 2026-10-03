package com.example.system.service.impl;

import com.example.system.dto.SportRecordDTO;
import com.example.system.entity.SportRecord;
import com.example.system.entity.SportsFacility;
import com.example.system.mapper.SportRecordMapper;
import com.example.system.mapper.SportsFacilityMapper;
import com.example.system.service.SportRecordService;
import com.example.system.vo.SportRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SportRecordServiceImpl implements SportRecordService {

    @Autowired
    private SportRecordMapper sportRecordMapper;

    @Autowired
    private SportsFacilityMapper facilityMapper;

    private String getSportTypeText(Integer type) {
        switch (type) {
            case 1: return "篮球";
            case 2: return "足球";
            case 3: return "羽毛球";
            case 4: return "跑步";
            case 5: return "健身";
            case 6: return "游泳";
            default: return "其他";
        }
    }

    private String formatDuration(Integer minutes) {
        if (minutes == null || minutes == 0) return "0分钟";
        int hours = minutes / 60;
        int mins = minutes % 60;
        if (hours == 0) return mins + "分钟";
        if (mins == 0) return hours + "小时";
        return hours + "小时" + mins + "分钟";
    }

    private int estimateCalories(Integer sportType, Integer duration) {
        int[] caloriesPerMinute = {0, 8, 7, 6, 10, 6, 8};
        int index = sportType != null && sportType <= 6 ? sportType : 0;
        return caloriesPerMinute[index] * duration;
    }

    @Override
    @Transactional
    public SportRecordVO addSportRecord(Long userId, SportRecordDTO dto) {
        SportRecord record = new SportRecord();
        record.setUserId(userId);
        record.setFacilityId(dto.getFacilityId());
        record.setSportType(dto.getSportType());
        record.setDuration(dto.getDuration());
        record.setCalories(dto.getCalories() != null ? dto.getCalories() : estimateCalories(dto.getSportType(), dto.getDuration()));
        record.setHeartRateAvg(dto.getHeartRateAvg());
        record.setSportDate(dto.getSportDate() != null ? dto.getSportDate() : java.time.LocalDate.now().toString());
        record.setRemark(dto.getRemark());

        sportRecordMapper.insert(record);
        log.info("添加运动记录: userId={}, sportType={}, duration={}", userId, dto.getSportType(), dto.getDuration());

        return convertToVO(record);
    }

    @Override
    public List<SportRecordVO> getSportRecords(Long userId) {
        List<SportRecord> records = sportRecordMapper.selectByUserId(userId);
        return records.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleteSportRecord(Long id, Long userId) {
        SportRecord record = sportRecordMapper.selectById(id);
        if (record == null) return false;
        if (!record.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除他人的运动记录");
        }
        return sportRecordMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public void syncFromReservation(Long userId, Long facilityId, String sportDate, Integer duration) {
        try {
            SportRecord existing = null; // 检查是否已有记录
            if (existing == null) {
                SportRecord record = new SportRecord();
                record.setUserId(userId);
                record.setFacilityId(facilityId);
                record.setSportType(getSportTypeByFacilityId(facilityId));
                record.setDuration(duration);
                record.setCalories(estimateCalories(record.getSportType(), duration));
                record.setSportDate(sportDate);
                sportRecordMapper.insert(record);
                log.info("同步预约到运动记录: userId={}, facilityId={}", userId, facilityId);
            }
        } catch (Exception e) {
            log.error("同步运动记录失败", e);
        }
    }

    private Integer getSportTypeByFacilityId(Long facilityId) {
        SportsFacility facility = facilityMapper.selectById(facilityId);
        if (facility == null) return 4;
        switch (facility.getType()) {
            case 1: return 1;
            case 2: return 2;
            case 3: return 3;
            case 4: return 4;
            case 5: return 5;
            case 6: return 6;
            default: return 4;
        }
    }

    private SportRecordVO convertToVO(SportRecord record) {
        if (record == null) return null;
        SportRecordVO vo = new SportRecordVO();
        BeanUtils.copyProperties(record, vo);
        vo.setSportTypeText(getSportTypeText(record.getSportType()));
        vo.setDurationText(formatDuration(record.getDuration()));
        if (record.getFacilityId() != null) {
            SportsFacility facility = facilityMapper.selectById(record.getFacilityId());
            if (facility != null) {
                vo.setFacilityName(facility.getName());
            }
        }
        return vo;
    }
}