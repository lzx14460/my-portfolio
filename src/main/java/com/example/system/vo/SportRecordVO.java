package com.example.system.vo;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SportRecordVO {
    private Long id;
    private Long userId;
    private Long facilityId;
    private String facilityName;
    private Integer sportType;
    private String sportTypeText;
    private Integer duration;
    private String durationText;
    private Integer calories;
    private Integer heartRateAvg;
    private String sportDate;
    private String remark;
}