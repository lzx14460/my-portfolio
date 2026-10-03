package com.example.system.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 体育设施实体类
 */
@Data
public class SportsFacility {
    private Long id;
    private String name;
    private Integer type;        // 1-篮球场，2-足球场，3-羽毛球场，4-乒乓球场，5-健身房，6-游泳馆
    private String location;
    private String description;
    private String imageUrl;
    private Integer capacity;
    private BigDecimal pricePerHour;
    private Integer maxDuration;
    private String openTime;     // HH:mm
    private String closeTime;    // HH:mm
    private Integer status;      // 0-停用，1-正常
    private Date createTime;
    private Date updateTime;
}