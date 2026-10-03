package com.example.system.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class ApproveApplyDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "请选择审批结果")
    private Integer status;  // 2-通过，3-拒绝

    private String remark;   // 审批备注
}