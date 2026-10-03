package com.example.system.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 创建预约数据传输对象
 */
@Data
public class CreateReservationDTO {

    @NotNull(message = "设施ID不能为空")
    private Long facilityId;

    @NotNull(message = "预约日期不能为空")
    private String reservationDate;

    @NotNull(message = "开始时间不能为空")
    private String startTime;

    @NotNull(message = "结束时间不能为空")
    private String endTime;

    private String remark;

    private String orderNo;
    private BigDecimal totalPrice;
}