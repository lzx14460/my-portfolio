package com.example.system.dto;


import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class CheckTimeDTO {
    @NotNull
    private Long facilityId;
    @NotNull
    private String reservationDate;  // 格式 yyyy-MM-dd
    @NotNull
    private String startTime;        // 格式 HH:mm:ss
    @NotNull
    private String endTime;
}