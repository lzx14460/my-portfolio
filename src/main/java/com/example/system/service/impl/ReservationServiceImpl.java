package com.example.system.service.impl;

import com.example.system.constant.ReservationStatus;
import com.example.system.entity.Reservation;
import com.example.system.entity.SportsFacility;
import com.example.system.mapper.ReservationMapper;
import com.example.system.mapper.SportsFacilityMapper;
import com.example.system   .service.ReservationService;
import com.example.system.service.SportRecordService;
import com.example.system.vo.SportRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReservationServiceImpl implements ReservationService {

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private SportsFacilityMapper facilityMapper;

    @Autowired
    private SportRecordService sportRecordService;
    /**
     * 创建预约（含时间冲突检测、时长计算、订单号生成）
     */
    @Override
    @Transactional
    public Reservation createReservation(Reservation reservation) {
        // 1. 获取设施信息
        SportsFacility facility = facilityMapper.selectById(reservation.getFacilityId());
        if (facility == null || facility.getStatus() != 1) {
            throw new RuntimeException("设施不存在或不可用");
        }

        // 2. 检查开放时间
        if (reservation.getStartTime().compareTo(facility.getOpenTime()) < 0 ||
                reservation.getEndTime().compareTo(facility.getCloseTime()) > 0) {
            throw new RuntimeException("预约时间不在开放时间内");
        }

        // 3. 计算时长和价格
        BigDecimal duration = calculateDuration(reservation.getStartTime(), reservation.getEndTime());
        if (duration.compareTo(BigDecimal.valueOf(0.5)) < 0) {
            throw new RuntimeException("预约时长至少30分钟");
        }

        // 4. 检查最长预约时间（从设施配置中读取，默认为4小时）
        int maxDuration = facility.getMaxDuration() != null ? facility.getMaxDuration() : 4;
        if (duration.compareTo(BigDecimal.valueOf(maxDuration)) > 0) {
            throw new RuntimeException("预约时长不能超过" + maxDuration + "小时");
        }

        reservation.setDuration(duration);
        BigDecimal totalPrice = facility.getPricePerHour().multiply(duration);
        reservation.setTotalPrice(totalPrice);

        // 5. 检查时间冲突
        boolean conflict = checkTimeConflict(
                reservation.getFacilityId(),
                reservation.getReservationDate(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                null
        );
        if (conflict) {
            throw new RuntimeException("该时间段已被预约");
        }

        // 6. 设置初始状态
        reservation.setStatus(ReservationStatus.PENDING_PAYMENT);

        // 7. 生成订单号
        reservation.setOrderNo(generateOrderNo());

        // 8. 插入数据库
        int result = reservationMapper.insert(reservation);
        if (result <= 0) {
            throw new RuntimeException("创建预约失败");
        }

        log.info("创建预约成功: userId={}, facilityId={}, maxDuration={}小时",
                reservation.getUserId(), reservation.getFacilityId(), maxDuration);
        return reservation;
    }
    /**
     * 取消预约（校验权限和状态）
     */
    @Override
    @Transactional
    public boolean cancelReservation(Long id, Long userId, Integer userRole) {
        Reservation reservation = reservationMapper.selectById(id);
        if (reservation == null) {
            throw new RuntimeException("预约不存在");
        }

        // 权限检查：只能取消自己的预约，或者是管理员
        if (!reservation.getUserId().equals(userId) && userRole != 1) {
            throw new RuntimeException("无权限取消他人预约");
        }

        // 状态检查
        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT &&
                reservation.getStatus() != ReservationStatus.PAID) {
            throw new RuntimeException("当前状态不可取消");
        }

        int result = reservationMapper.updateStatus(id, ReservationStatus.CANCELLED);
        log.info("取消预约成功: id={}", id);
        return result > 0;
    }

//    @Override
//    @Transactional
//    public boolean payReservation(Long id, Long userId) {
//        Reservation reservation = reservationMapper.selectById(id);
//        if (reservation == null) {
//            throw new RuntimeException("预约不存在");
//        }
//
//        if (!reservation.getUserId().equals(userId)) {
//            throw new RuntimeException("无权限支付他人订单");
//        }
//
//        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT) {
//            throw new RuntimeException("订单状态错误，无法支付");
//        }
//
//        int result = reservationMapper.updateStatus(id, ReservationStatus.PAID);
//        log.info("支付成功: id={}", id);
//        return result > 0;
//    }

    /**
     * 场馆管理员签到（将已支付预约标记为已使用）
     */
    @Override
    @Transactional
    public boolean checkInReservation(Long id, Integer userRole) {
        if (userRole != 1) {
            throw new RuntimeException("只有场馆管理员可以签到");
        }

        Reservation reservation = reservationMapper.selectById(id);
        if (reservation == null) {
            throw new RuntimeException("预约不存在");
        }

        if (reservation.getStatus() != ReservationStatus.PAID) {
            throw new RuntimeException("只有已支付的预约才能签到");
        }

        int result = reservationMapper.updateStatus(id, ReservationStatus.USED);

        // 同步到运动记录表
        if (result > 0) {
            sportRecordService.syncFromReservation(
                    reservation.getUserId(),
                    reservation.getFacilityId(),
                    reservation.getReservationDate(),
                    reservation.getDuration().intValue() * 60  // 转换为分钟
            );
        }

        log.info("签到成功: id={}", id);
        return result > 0;
    }

    /**
     * 根据ID查询预约
     */
    @Override
    public Reservation getReservationById(Long id) {
        return reservationMapper.selectById(id);
    }

    /**
     * 获取用户的所有预约列表
     */
    @Override
    public List<Reservation> getReservationsByUserId(Long userId) {
        return reservationMapper.selectByUserId(userId);
    }
    @Override
    public int getTodayReservationCount() {
        return reservationMapper.selectTodayCount();
    }
    /**
     * 分页获取用户的预约列表
     */
    @Override
    public List<Reservation> getReservationsByUserIdWithPage(Long userId, int pageNum, int pageSize) {
        int start = (pageNum - 1) * pageSize;
        return reservationMapper.selectByUserIdWithPage(userId, start, pageSize);
    }

    /**
     * 获取用户的预约总数
     */
    @Override
    public int getCountByUserId(Long userId) {
        return reservationMapper.selectCountByUserId(userId);
    }

    /**
     * 检测预约时间是否与现有预约冲突
     */
    @Override
    public boolean checkTimeConflict(Long facilityId, String reservationDate,
                                     String startTime, String endTime, Long excludeId) {
        int count = reservationMapper.checkTimeConflict(facilityId, reservationDate,
                startTime, endTime, excludeId);
        return count > 0;
    }
    /**
     * 获取所有预约（管理员用，关联用户和设施信息）
     */
    @Override
    public List<Reservation> getAllReservations() {
        return reservationMapper.selectAllWithUserAndFacility();
    }

    /**
     * 管理员根据ID删除预约
     */
    @Override
    @Transactional
    public boolean deleteReservationById(Long id) {
        // 先查询预约是否存在
        Reservation reservation = reservationMapper.selectById(id);
        if (reservation == null) {
            throw new RuntimeException("预约不存在");
        }
        // 物理删除
        int result = reservationMapper.deleteById(id);
        log.info("删除预约: id={}, orderNo={}", id, reservation.getOrderNo());
        return result > 0;
    }

//    @Override
//    public Map<String, Object> getStatistics(String startDate, String endDate) {
//        return reservationMapper.selectStatistics(startDate, endDate);
//    }

    /**
     * 定时任务：自动将超时未支付的预约设为过期
     */
    @Override
    @Transactional
    public void autoExpireReservations() {
        // 定时任务：将超时未支付的预约设为过期
        // 实际项目中可使用@Scheduled注解实现
        log.info("执行自动过期预约任务");
    }

    /**
     * 计算预约时长
     */
    private BigDecimal calculateDuration(String startTime, String endTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date start = sdf.parse(startTime);
            Date end = sdf.parse(endTime);
            long diff = end.getTime() - start.getTime();
            double hours = diff / (1000.0 * 60 * 60);
            return BigDecimal.valueOf(hours);
        } catch (Exception e) {
            throw new RuntimeException("时间格式错误");
        }
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = sdf.format(new Date());
        int random = (int) (Math.random() * 1000);
        return "ORD" + timestamp + String.format("%03d", random);
    }
    /**
     * 根据订单号支付预约
     */
    @Override
    @Transactional
    public boolean payByOrderNo(String orderNo, Long userId, Integer paymentMethod) {
        // 1. 根据订单号查询预约（需要在 Mapper 中添加 selectByOrderNo 方法）
        Reservation reservation = reservationMapper.selectByOrderNo(orderNo);
        if (reservation == null) {
            throw new RuntimeException("订单不存在");
        }
        // 2. 校验用户权限
        if (!reservation.getUserId().equals(userId)) {
            throw new RuntimeException("无权限支付他人订单");
        }
        // 3. 校验状态
        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT) {
            throw new RuntimeException("订单状态错误，无法支付");
        }
        // 4. 可选：校验是否超时（例如创建时间超过15分钟）
        // 5. 更新预约状态为已支付
        int result = reservationMapper.updateStatus(reservation.getId(), ReservationStatus.PAID);
        if (result <= 0) {
            throw new RuntimeException("支付失败，更新状态失败");
        }
        // 6. 插入支付记录（假设您有 PaymentRecordMapper，如果没有可以先注释）
        // PaymentRecord payment = new PaymentRecord();
        // payment.setReservationId(reservation.getId());
        // payment.setUserId(userId);
        // payment.setOrderNo(orderNo);
        // payment.setAmount(reservation.getTotalPrice());
        // payment.setPaymentMethod(paymentMethod);
        // payment.setPaymentStatus(1);
        // payment.setPaymentTime(LocalDateTime.now());
        // paymentRecordMapper.insert(payment);

        log.info("订单支付成功: orderNo={}, userId={}", orderNo, userId);
        return true;
    }
    @Override
    public List<Reservation> getReservationsByFacilityAndDate(Long facilityId, String date) {
        return reservationMapper.selectByFacilityAndDate(facilityId, date);
    }@Override
    public List<Map<String, Object>> getDailyTrend(String startDate, String endDate) {
        return reservationMapper.selectDailyTrend(startDate, endDate);
    }

    @Override
    public List<Map<String, Object>> getHourUtilization(String startDate, String endDate) {
        return reservationMapper.selectHourUtilization(startDate, endDate);
    }
    @Override
    public List<Map<String, Object>> getFacilityRanking(String startDate, String endDate) {
        List<Map<String, Object>> rankings = reservationMapper.selectFacilityRanking(startDate, endDate);

        // 计算总预约数用于百分比
        int totalCount = 0;
        for (Map<String, Object> item : rankings) {
            int count = ((Number) item.get("reservationCount")).intValue();
            totalCount += count;
        }

        for (Map<String, Object> item : rankings) {
            int count = ((Number) item.get("reservationCount")).intValue();
            // 获取设施信息
            Long facilityId = ((Number) item.get("facilityId")).longValue();
            SportsFacility facility = facilityMapper.selectById(facilityId);
            if (facility != null) {
                item.put("facilityName", facility.getName());
            }

            // 计算使用率（基于开放时间8:00-22:00，共14小时）
            int totalHours = 14;
            int totalDays = getDaysBetween(startDate, endDate);
            int maxReservations = totalDays * totalHours;
            double utilizationRate = Math.round((double) count / maxReservations * 1000) / 10.0;
            item.put("utilizationRate", utilizationRate);
            item.put("reservationCount", count);
        }

        // 按预约数降序排序
        rankings.sort((a, b) -> {
            int countA = ((Number) a.get("reservationCount")).intValue();
            int countB = ((Number) b.get("reservationCount")).intValue();
            return Integer.compare(countB, countA);
        });

        return rankings;
    }


    /**
     * 计算两个日期之间的天数
     */
    private int getDaysBetween(String startDate, String endDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            long diff = end.getTime() - start.getTime();
            return (int) (diff / (1000 * 60 * 60 * 24)) + 1;
        } catch (Exception e) {
            return 1;
        }
    }
}