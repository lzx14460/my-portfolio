package com.example.system.dto;

import lombok.Data;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class HealthRecordDTO {

    private String recordDate;  // 记录日期

    @NotNull(message = "身高不能为空")
    @DecimalMin(value = "50.0", message = "身高不能低于50cm")
    @DecimalMax(value = "250.0", message = "身高不能高于250cm")
    private BigDecimal height;

    @NotNull(message = "体重不能为空")
    @DecimalMin(value = "10.0", message = "体重不能低于10kg")
    @DecimalMax(value = "300.0", message = "体重不能高于300kg")
    private BigDecimal weight;
}