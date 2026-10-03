package com.example.system.entity;

import lombok.Data;
import java.util.Date;

/**
 * 运动健康记录实体类
 */
@Data
public class SportRecord {
    private Long id;
    private Long userId;
    private Long facilityId;
    private Integer sportType;    // 1-篮球，2-足球，3-羽毛球，4-跑步，5-健身，6-游泳
    private Integer duration;     // 运动时长（分钟）
    private Integer calories;     // 消耗卡路里
    private Integer heartRateAvg; // 平均心率
    private String sportDate;     // 运动日期 yyyy-MM-dd
    private String remark;
    private Date createTime;
    private Date updateTime;
}