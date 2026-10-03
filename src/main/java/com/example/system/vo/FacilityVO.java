package com.example.system.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 设施视图对象
 */
@Data
public class FacilityVO {
    private Long id;
    private String name;
    private Integer type;
    private String typeText;        // 设施类型文本
    private String location;
    private String description;
    private String imageUrl;
    private Integer capacity;
    private BigDecimal pricePerHour;
    private String openTime;
    private String closeTime;
    private Integer status;
    private String statusText;       // 状态文本
}