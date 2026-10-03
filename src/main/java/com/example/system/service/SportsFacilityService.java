package com.example.system.service;

import com.example.system.entity.SportsFacility;
import com.example.system.vo.FacilityStatisticsVO;
import java.util.List;
import java.util.Map;

public interface SportsFacilityService {

    SportsFacility getFacilityById(Long id);

    List<SportsFacility> getAllFacilities();

    List<SportsFacility> getFacilitiesByType(Integer type);

    List<SportsFacility> getFacilitiesByPage(int pageNum, int pageSize);

    int getTotalCount();

//    List<SportsFacility> getFacilitiesByCondition(SportsFacility facility);

    List<SportsFacility> getAvailableFacilities(String reservationDate,
                                                String startTime,
                                                String endTime);

    /**
     * 查询单个设施统计 - 返回 FacilityStatisticsVO
     */
    FacilityStatisticsVO getFacilityStatistics(Long facilityId, String startDate, String endDate);

    List<Map<String, Object>> getAllFacilitiesStatistics(String startDate, String endDate);

    int addFacility(SportsFacility facility);

    int updateFacility(SportsFacility facility);

//    int updateFacilityStatus(Long id, Integer status);

    int deleteFacility(Long id);
}