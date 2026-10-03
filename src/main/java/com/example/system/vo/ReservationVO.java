package com.example.system.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 预约视图对象
 */
@Data
public class ReservationVO {
    private Long id;
    private Long userId;
    private String userName;        // 用户姓名
    private String userPhone;       // 新增：用户手机号
    private Long facilityId;
    private String facilityName;    // 设施名称
    private String facilityType;    // 设施类型文本
    private String reservationDate;
    private String startTime;
    private String endTime;
    private BigDecimal duration;
    private BigDecimal totalPrice;
    private Integer status;          // 状态码
    private String statusText;      // 状态文本
    private String statusColor;     // 前端标签颜色
    private String orderNo;
    private String remark;
    private Date createTime;
}