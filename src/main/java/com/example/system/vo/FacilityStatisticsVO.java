package com.example.system.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 设施统计VO
 */
@Data
public class FacilityStatisticsVO {
    private Long facilityId;
    private String facilityName;
    private Integer facilityType;
    private Long totalReservations;      // 总预约数
    private BigDecimal totalRevenue;     // 总收入
    private Long paidReservations;       // 已支付数量
    private Long usedReservations;       // 已使用数量
    private Long cancelledReservations;  // 已取消数量
    private Long pendingReservations;    // 待支付数量
}