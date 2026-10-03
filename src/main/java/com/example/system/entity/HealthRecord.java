package com.example.system.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Data
public class HealthRecord {
    private Long id;
    private Long userId;
    private BigDecimal height;      // 身高(cm)
    private BigDecimal weight;      // 体重(kg)
    private LocalDate recordDate;   // 记录日期
    private Date createTime;
}