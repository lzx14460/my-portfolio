package com.example.system.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class HealthRecordVO {
    private Long id;
    private BigDecimal height;
    private BigDecimal weight;
    private LocalDate recordDate;  // 记录日期
}