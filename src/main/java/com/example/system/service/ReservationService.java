package com.example.system.service;

import com.example.system.entity.Reservation;
import com.example.system.vo.SportRecordVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ReservationService {

    Reservation createReservation(Reservation reservation);

    boolean cancelReservation(Long id, Long userId, Integer userRole);

//    boolean payReservation(Long id, Long userId);

    boolean checkInReservation(Long id, Integer userRole);

    Reservation getReservationById(Long id);

    List<Reservation> getReservationsByUserId(Long userId);

    List<Reservation> getReservationsByUserIdWithPage(Long userId, int pageNum, int pageSize);

    int getCountByUserId(Long userId);

    boolean checkTimeConflict(Long facilityId, String reservationDate,

                              String startTime, String endTime, Long excludeId);
    /**
     * 获取今日预约总数
     */
    int getTodayReservationCount();
    /**
     * 按订单号支付
     * @param orderNo 订单号
     * @param userId 当前用户ID
     * @param paymentMethod 支付方式
     * @return 是否成功
     */
    boolean payByOrderNo(String orderNo, Long userId, Integer paymentMethod);
    /**
     * 获取所有预约（管理员）
     */
    List<Reservation> getAllReservations();

    /**
     * 根据ID删除预约（管理员）
     */
    boolean deleteReservationById(Long id);
    /**
     * 获取指定设施在指定日期的预约列表
     */
    List<Reservation> getReservationsByFacilityAndDate(Long facilityId, String date);
    /**
     * 获取每日预约趋势
     */
    List<Map<String, Object>> getDailyTrend(String startDate, String endDate);

    /**
     * 获取各时段利用率
     */
    List<Map<String, Object>> getHourUtilization(String startDate, String endDate);
    /**
     * 获取场馆利用率排行
     */
    List<Map<String, Object>> getFacilityRanking(String startDate, String endDate);


    void autoExpireReservations();
}