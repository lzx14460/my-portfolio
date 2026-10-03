package com.example.system.mapper;


import com.example.system.entity.Reservation;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReservationMapper {

    /**
     * 根据ID查询预约
     */
    Reservation selectById(Long id);

    /**
     * 查询用户的所有预约
     */
    List<Reservation> selectByUserId(Long userId);

    /**
     * 分页查询用户预约
     */
    List<Reservation> selectByUserIdWithPage(@Param("userId") Long userId,
                                             @Param("start") int start,
                                             @Param("size") int size);

    /**
     * 查询用户预约总数
     */
    int selectCountByUserId(Long userId);

    /**
     * 检查时间冲突
     */
    int checkTimeConflict(@Param("facilityId") Long facilityId,
                          @Param("reservationDate") String reservationDate,
                          @Param("startTime") String startTime,
                          @Param("endTime") String endTime,
                          @Param("excludeId") Long excludeId);

    /**
     * 插入预约
     */
    int insert(Reservation reservation);
    /**
     * 根据订单号查询预约
     */
    Reservation selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 更新预约状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    /**
     * 查询今日预约总数
     */
    int selectTodayCount();

//    /**
//     * 更新预约
//     */
//    int update(Reservation reservation);

//    /**
//     * 统计预约数据 - 返回单个Map对象，不需要@MapKey
//     */
//    @MapKey("statKey")
//    Map<String, Object> selectStatistics(@Param("startDate") String startDate,
//                                         @Param("endDate") String endDate);
    /**
     * 查询所有预约（关联用户和设施信息）
     */
    List<Reservation> selectAllWithUserAndFacility();

    /**
     * 根据ID删除预约
     */
    int deleteById(Long id);
    /**
     * 查询用户已使用的预约记录（运动记录）
     */
    List<Reservation> selectUsedByUserId(@Param("userId") Long userId);
    /**
     * 查询指定设施在指定日期的预约
     */
    List<Reservation> selectByFacilityAndDate(@Param("facilityId") Long facilityId,
                                              @Param("date") String date);
    /**
     * 查询每日预约趋势
     */
    List<Map<String, Object>> selectDailyTrend(@Param("startDate") String startDate,
                                               @Param("endDate") String endDate);

    /**
     * 查询各时段利用率
     */
    List<Map<String, Object>> selectHourUtilization(@Param("startDate") String startDate,
                                                    @Param("endDate") String endDate);

    /**
     * 查询场馆预约排行
     */
    List<Map<String, Object>> selectFacilityRanking(@Param("startDate") String startDate,
                                                    @Param("endDate") String endDate);

    /**
     * 查询指定设施在日期范围内的预约
     */
    List<Reservation> selectByFacilityAndDateRange(@Param("facilityId") Long facilityId,
                                                   @Param("startDate") String startDate,
                                                   @Param("endDate") String endDate);
}