package com.example.system.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;

@Data
public class SportRecordDTO {
    private Long facilityId;

    @NotNull(message = "运动类型不能为空")
    private Integer sportType;

    @NotNull(message = "运动时长不能为空")
    @Min(value = 1, message = "运动时长至少1分钟")
    private Integer duration;

    private Integer calories;
    private Integer heartRateAvg;
    private String sportDate;
    private String remark;
}