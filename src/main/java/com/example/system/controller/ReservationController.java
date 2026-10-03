package com.example.system.controller;

import com.example.system.common.PageResult;
import com.example.system.common.Result;
import com.example.system.dto.CheckTimeDTO;
import com.example.system.dto.CreateReservationDTO;
import com.example.system.dto.PayRequest;
import com.example.system.entity.Reservation;
import com.example.system.entity.SportsFacility;
import com.example.system.entity.User;
import com.example.system.service.ReservationService;
import com.example.system.service.SportsFacilityService;
import com.example.system.service.UserService;
import com.example.system.vo.ReservationVO;
import com.example.system.vo.SportRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/reservation")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private SportsFacilityService facilityService;

    @Autowired
    private UserService userService;

    /**
     * 创建预约
     */
    @PostMapping("/create")
    public Result<ReservationVO> createReservation(@Valid @RequestBody CreateReservationDTO dto,
                                                   HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }

        try {
            // 创建预约实体
            Reservation reservation = new Reservation();
            reservation.setUserId(userId);
            reservation.setFacilityId(dto.getFacilityId());
            reservation.setReservationDate(dto.getReservationDate());
            reservation.setStartTime(dto.getStartTime());
            reservation.setEndTime(dto.getEndTime());
            reservation.setRemark(dto.getRemark());

            // 调用服务创建预约
            Reservation created = reservationService.createReservation(reservation);

            // 转换为VO返回
            ReservationVO vo = convertToVO(created);
            return Result.success(vo);

        } catch (Exception e) {
            log.error("创建预约失败", e);
            return Result.error(e.getMessage());
        }
    }
    /**
     * 检测时间段是否已被预约（供前端预约前调用）
     */
    @PostMapping("/check")
    public Result<Void> checkTimeConflict(@RequestBody CheckTimeDTO dto) {
        boolean conflict = reservationService.checkTimeConflict(
                dto.getFacilityId(),
                dto.getReservationDate(),
                dto.getStartTime(),
                dto.getEndTime(),
                null  // 新建预约时排除ID为null
        );
        if (conflict) {
            return Result.error("该时间段已被预约，请选择其他时间");
        }
        return Result.success();
    }

    /**
     * 按订单号支付（替代原有的 /pay/{id}，更方便前端）
     */
    @PostMapping("/pay-by-order")
    public Result<Void> payByOrderNo(@RequestBody PayRequest payRequest, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }
        try {
            boolean success = reservationService.payByOrderNo(payRequest.getOrderNo(), userId, payRequest.getPaymentMethod());
            if (success) {
                return Result.success();
            } else {
                return Result.error("支付失败");
            }
        } catch (Exception e) {
            log.error("支付失败", e);
            return Result.error(e.getMessage());
        }
    }


    /**
     * 取消预约
     */
    @PutMapping("/cancel/{id}")
    public Result<Void> cancelReservation(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        Integer userRole = (Integer) session.getAttribute("userRole");

        if (userId == null) {
            return Result.error("请先登录");
        }

        try {
            boolean result = reservationService.cancelReservation(id, userId, userRole);
            if (result) {
                return Result.success();
            }
            return Result.error("取消失败");
        } catch (Exception e) {
            log.error("取消预约失败", e);
            return Result.error(e.getMessage());
        }
    }
    /**
     * 管理员获取所有预约列表
     */
    @GetMapping("/admin/list")
    public Result<List<ReservationVO>> getAllReservationsForAdmin(HttpSession session) {
        // 权限检查：仅场馆管理员(1)可访问
        Integer userRole = (Integer) session.getAttribute("userRole");
        if (userRole == null || userRole != 1) {
            return Result.error("权限不足，仅场馆管理员可访问");
        }

        List<Reservation> reservations = reservationService.getAllReservations();
        List<ReservationVO> voList = reservations.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }
    /**
     * 获取今日预约总数
     */
    @GetMapping("/today-count")
    public Result<Integer> getTodayReservationCount() {
        int count = reservationService.getTodayReservationCount();
        return Result.success(count);
    }

    /**
     * 管理员删除预约
     */
    @DeleteMapping("/admin/{id}")
    public Result<Void> deleteReservationByAdmin(@PathVariable Long id, HttpSession session) {
        // 权限检查：仅场馆管理员(1)可访问
        Integer userRole = (Integer) session.getAttribute("userRole");
        if (userRole == null || userRole != 1) {
            return Result.error("权限不足，仅场馆管理员可操作");
        }

        try {
            boolean result = reservationService.deleteReservationById(id);
            if (result) {
                log.info("管理员删除预约成功: id={}", id);
                return Result.success();
            } else {
                return Result.error("预约不存在");
            }
        } catch (Exception e) {
            log.error("删除预约失败: id={}", id, e);
            return Result.error(e.getMessage());
        }
    }


    /**
     * 签到使用（管理员）
     */
    @PutMapping("/checkin/{id}")
    public Result<Void> checkIn(@PathVariable Long id, HttpSession session) {
        Integer userRole = (Integer) session.getAttribute("userRole");
        if (userRole == null || userRole != 1) {
            return Result.error("只有场馆管理员可以签到");
        }

        try {
            boolean result = reservationService.checkInReservation(id, userRole);
            if (result) {
                return Result.success();
            }
            return Result.error("签到失败");
        } catch (Exception e) {
            log.error("签到失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取我的预约列表
     */
    @GetMapping("/my-list")
    public Result<List<ReservationVO>> getMyReservations(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }

        List<Reservation> reservations = reservationService.getReservationsByUserId(userId);
        List<ReservationVO> voList = reservations.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 分页获取我的预约
     */
    @GetMapping("/my-page")
    public Result<PageResult<ReservationVO>> getMyReservationsByPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }

        List<Reservation> reservations = reservationService.getReservationsByUserIdWithPage(
                userId, pageNum, pageSize);
        int total = reservationService.getCountByUserId(userId);

        List<ReservationVO> voList = reservations.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        PageResult<ReservationVO> pageResult = new PageResult<>(pageNum, pageSize, total, voList);
        return Result.success(pageResult);
    }

    /**
     * 获取预约详情
     */
    @GetMapping("/{id}")
    public Result<ReservationVO> getReservationById(@PathVariable Long id) {
        Reservation reservation = reservationService.getReservationById(id);
        if (reservation == null) {
            return Result.error("预约不存在");
        }
        return Result.success(convertToVO(reservation));
    }
    /**
     * 获取设施的预约情况
     */
    @GetMapping("/facility")
    public Result<List<ReservationVO>> getFacilityReservations(
            @RequestParam Long facilityId,
            @RequestParam String date) {

        List<Reservation> reservations = reservationService.getReservationsByFacilityAndDate(facilityId, date);
        List<ReservationVO> voList = reservations.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }
    /**
     * 获取预约趋势统计数据
     */
    @GetMapping("/statistics/trend")
    public Result<List<Map<String, Object>>> getTrendStatistics(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        List<Map<String, Object>> data = reservationService.getDailyTrend(startDate, endDate);
        return Result.success(data);
    }

    /**
     * 获取各时段利用率
     */
    @GetMapping("/statistics/hour-utilization")
    public Result<List<Map<String, Object>>> getHourUtilization(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        List<Map<String, Object>> data = reservationService.getHourUtilization(startDate, endDate);
        return Result.success(data);
    }

    /**
     * 获取场馆利用率排行
     */
    @GetMapping("/statistics/facility-ranking")
    public Result<List<Map<String, Object>>> getFacilityRanking(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        List<Map<String, Object>> data = reservationService.getFacilityRanking(startDate, endDate);
        return Result.success(data);
    }


    /**
     * 转换方法：Reservation -> ReservationVO
     */
    private ReservationVO convertToVO(Reservation reservation) {
        if (reservation == null) return null;

        ReservationVO vo = new ReservationVO();
        BeanUtils.copyProperties(reservation, vo);

        // 设置状态码
        vo.setStatus(reservation.getStatus());

        // 获取用户信息（包含手机号）
        if (reservation.getUser() != null) {
            vo.setUserName(reservation.getUser().getName());
            vo.setUserPhone(reservation.getUser().getPhone());  // 新增
        } else {
            User user = userService.getUserById(reservation.getUserId());
            if (user != null) {
                vo.setUserName(user.getName());
                vo.setUserPhone(user.getPhone());  // 新增
            }
        }

        // 获取设施信息
        if (reservation.getFacility() != null) {
            vo.setFacilityName(reservation.getFacility().getName());
            vo.setFacilityType(getFacilityTypeText(reservation.getFacility().getType()));
        } else {
            SportsFacility facility = facilityService.getFacilityById(reservation.getFacilityId());
            if (facility != null) {
                vo.setFacilityName(facility.getName());
                vo.setFacilityType(getFacilityTypeText(facility.getType()));
            }
        }

        // 状态转换
        if (reservation.getStatus() != null) {
            switch (reservation.getStatus()) {
                case 0:
                    vo.setStatusText("待支付");
                    vo.setStatusColor("warning");
                    break;
                case 1:
                    vo.setStatusText("已支付");
                    vo.setStatusColor("success");
                    break;
                case 2:
                    vo.setStatusText("已使用");
                    vo.setStatusColor("info");
                    break;
                case 3:
                    vo.setStatusText("已取消");
                    vo.setStatusColor("danger");
                    break;
                case 4:
                    vo.setStatusText("已过期");
                    vo.setStatusColor("danger");
                    break;
                default:
                    vo.setStatusText("未知");
                    vo.setStatusColor("info");
            }
        }

        return vo;
    }

    /**
     * 获取设施类型文本
     */
    private String getFacilityTypeText(Integer type) {
        if (type == null) return "未知";
        switch (type) {
            case 1: return "篮球场";
            case 2: return "足球场";
            case 3: return "羽毛球场";
            case 4: return "乒乓球场";
            case 5: return "健身房";
            case 6: return "游泳馆";
            default: return "其他";
        }
    }
}