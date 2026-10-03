package com.example.system.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class PayRequest {
    @NotBlank
    private String orderNo;
    @NotNull
    private Integer paymentMethod;   // 1-微信 2-支付宝 3-校园卡
}