package com.example.system.mapper;

import com.example.system.entity.SportsFacility;
import com.example.system.vo.FacilityStatisticsVO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface SportsFacilityMapper {

    /**
     * 根据ID查询设施
     */
    SportsFacility selectById(Long id);

    /**
     * 查询所有设施
     */
    List<SportsFacility> selectAll();

    /**
     * 根据类型查询设施
     */
    List<SportsFacility> selectByType(Integer type);

    /**
     * 分页查询设施
     */
    List<SportsFacility> selectByPage(@Param("start") int start, @Param("size") int size);

    /**
     * 查询设施总数
     */
    int selectCount();

//    /**
//     * 条件查询设施
//     */
//    List<SportsFacility> selectByCondition(SportsFacility facility);

    /**
     * 查询可预约的设施
     */
    List<SportsFacility> selectAvailableFacilities(@Param("reservationDate") String reservationDate,
                                                   @Param("startTime") String startTime,
                                                   @Param("endTime") String endTime);

    /**
     * 查询单个设施统计 - 返回自定义VO，不需要@MapKey
     */
    FacilityStatisticsVO selectFacilityStatistics(@Param("facilityId") Long facilityId,
                                                  @Param("startDate") String startDate,
                                                  @Param("endDate") String endDate);

    /**
     * 查询所有设施统计 - 返回多个Map，需要用@MapKey指定key字段
     */
    @MapKey("facilityId")
    List<Map<String, Object>> selectAllFacilitiesStatistics(@Param("startDate") String startDate,
                                                            @Param("endDate") String endDate);

    /**
     * 插入设施
     */
    int insert(SportsFacility facility);

    /**
     * 更新设施
     */
    int update(SportsFacility facility);

//    /**
//     * 更新设施状态
//     */
//    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 删除设施
     */
    int deleteById(Long id);
}