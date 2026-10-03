package com.example.system.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 预约实体类
 */
@Data
public class Reservation {
    private Long id;
    private Long userId;
    private Long facilityId;
    private String reservationDate;  // 预约日期 yyyy-MM-dd
    private String startTime;        // 开始时间 HH:mm
    private String endTime;          // 结束时间 HH:mm
    private BigDecimal duration;     // 预约时长（小时）
    private BigDecimal totalPrice;   // 总金额
    private Integer status;          // 0-待支付，1-已支付，2-已使用，3-已取消，4-已过期
    private String orderNo;          // 订单号
    private String remark;           // 备注
    private Date createTime;
    private Date updateTime;

    // 关联字段（非数据库字段）
    private User user;
    private SportsFacility facility;
}