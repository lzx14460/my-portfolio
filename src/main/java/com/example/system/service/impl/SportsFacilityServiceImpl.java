package com.example.system.service.impl;

import com.example.system.entity.SportsFacility;
import com.example.system.mapper.SportsFacilityMapper;
import com.example.system.service.SportsFacilityService;
import com.example.system.vo.FacilityStatisticsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SportsFacilityServiceImpl implements SportsFacilityService {

    @Autowired
    private SportsFacilityMapper facilityMapper;

    /**
     * 根据ID查询设施
     */
    @Override
    public SportsFacility getFacilityById(Long id) {
        return facilityMapper.selectById(id);
    }

    /**
     * 获取所有设施列表
     */
    @Override
    public List<SportsFacility> getAllFacilities() {
        return facilityMapper.selectAll();
    }

    /**
     * 根据类型查询设施
     */
    @Override
    public List<SportsFacility> getFacilitiesByType(Integer type) {
        return facilityMapper.selectByType(type);
    }

    /**
     * 分页查询设施列表
     */
    @Override
    public List<SportsFacility> getFacilitiesByPage(int pageNum, int pageSize) {
        int start = (pageNum - 1) * pageSize;
        return facilityMapper.selectByPage(start, pageSize);
    }

    /**
     * 获取设施总数
     */
    @Override
    public int getTotalCount() {
        return facilityMapper.selectCount();
    }

//    @Override
//    public List<SportsFacility> getFacilitiesByCondition(SportsFacility facility) {
//        return facilityMapper.selectByCondition(facility);
//    }

    /**
     * 查询指定时间段内可预约的设施
     */
    @Override
    public List<SportsFacility> getAvailableFacilities(String reservationDate,
                                                       String startTime,
                                                       String endTime) {
        return facilityMapper.selectAvailableFacilities(reservationDate, startTime, endTime);
    }

    /**
     * 获取单个设施的预约统计数据
     */
    @Override
    public FacilityStatisticsVO getFacilityStatistics(Long facilityId, String startDate, String endDate) {
        // 直接返回 Mapper 返回的 FacilityStatisticsVO
        return facilityMapper.selectFacilityStatistics(facilityId, startDate, endDate);
    }

    /**
     * 获取所有设施的预约统计（按预约数降序）
     */
    @Override
    public List<Map<String, Object>> getAllFacilitiesStatistics(String startDate, String endDate) {
        return facilityMapper.selectAllFacilitiesStatistics(startDate, endDate);
    }

    /**
     * 添加新设施（含默认值设置）
     */
    @Override
    @Transactional
    public int addFacility(SportsFacility facility) {
        // 设置默认值
        if (facility.getStatus() == null) {
            facility.setStatus(1);
        }
        if (facility.getOpenTime() == null) {
            facility.setOpenTime("08:00:00");
        }
        if (facility.getCloseTime() == null) {
            facility.setCloseTime("22:00:00");
        }
        if (facility.getCapacity() == null) {
            facility.setCapacity(10);
        }
        if (facility.getPricePerHour() == null) {
            facility.setPricePerHour(java.math.BigDecimal.valueOf(0));
        }

        int result = facilityMapper.insert(facility);
        log.info("添加设施成功: {}", facility.getName());
        return result;
    }

    /**
     * 更新设施信息
     */
    @Override
    @Transactional
    public int  updateFacility(SportsFacility facility) {
        int result = facilityMapper.update(facility);
        log.info("更新设施成功: id={}", facility.getId());
        return result;
    }

//    @Override
//    @Transactional
//    public int updateFacilityStatus(Long id, Integer status) {
//        int result = facilityMapper.updateStatus(id, status);
//        log.info("更新设施状态: id={}, status={}", id, status);
//        return result;
//    }

    /**
     * 删除设施
     */
    @Override
    @Transactional
    public int deleteFacility(Long id) {
        int result = facilityMapper.deleteById(id);
        log.info("删除设施: id={}", id);
        return result;
    }
}